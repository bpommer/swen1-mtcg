package edu.swen1.mtcg.services.db.repository;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.DbAccessException;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import edu.swen1.mtcg.services.db.models.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TradeRepository {
    private TransactionUnit transactionUnit;
    public TradeRepository(TransactionUnit transactionUnit) { this.transactionUnit = transactionUnit; }

    // Insert new trade
    public Response createNewTrade(User user, TradingDeal newDeal) {

        // Check if trade id already exists
        String preQuery = "SELECT t.* FROM trade t WHERE t.id = ?";
        PreparedStatement preparedStatement = transactionUnit.prepareStatement(preQuery);
        try {
            preparedStatement.setString(1, newDeal.getTradeid());
            ResultSet res1 = preparedStatement.executeQuery();
            if(res1.next()) {
                return new Response(HttpStatus.CONFLICT, ContentType.TEXT,
                        "A deal with this deal ID already exists.");
            }

        } catch (SQLException e) {
            throw new DbAccessException("Could not query existing trades", e);
        }

        // Check if card is valid
        String typeQuery = "SELECT id FROM cardtype WHERE type ILIKE ? LIMIT 1";
        preparedStatement = transactionUnit.prepareStatement(typeQuery);
        int typeId = 0;

        try {
            preparedStatement.setString(1, newDeal.getType());
            ResultSet res1 = preparedStatement.executeQuery();
            if(res1.next()) {
                typeId = res1.getInt(1);
            } else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT,
                        "Invalid type for deal.");
            }

        } catch (SQLException e) {
            throw new DbAccessException("Could not query existing trades", e);
        }





        // Check if user owns card by querying stack
        String objString = null;
        String ownerQuery = """
                WITH ls AS (
                    SELECT
                        jsonb_array_elements(stack) AS st,
                        generate_series(1, jsonb_array_length(stack)) AS in
                    FROM profile
                    WHERE profile.id = ?
                    ORDER BY (stack ->>'Id')::UUID
                )
                SELECT ls.st AS usercard
                FROM ls
                WHERE (ls.st)->>'Id' = ?
                LIMIT 1""";
        preparedStatement = transactionUnit.prepareStatement(ownerQuery);
        try {
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setString(2, newDeal.getCardid());
            ResultSet res2 = preparedStatement.executeQuery();
            if(!res2.next()) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT,
                        "The deal contains a card that is not owned by the user or locked in the deck.");
            } else {
                objString = res2.getString(1);
            }
        } catch (SQLException e) {
            throw new DbAccessException("Error in ownership check", e);
        }


        // Remove single card instance with matching ID from stack
        // - ls: Fetch all cards from stack by ID and give each card a unique row number
        // - fl: Filter out single card instance by ID from ls
        // - Update user stack with either fl or empty array

        String removeQuery = """
                WITH ls AS (
                    SELECT
                        jsonb_array_elements(stack) AS st,
                        generate_series(1, jsonb_array_length(stack)) AS ix
                    FROM profile
                    WHERE profile.id = ?
                ),
                fl AS (
                    SELECT jsonb_agg(ls.st) AS newstack
                    FROM ls
                    WHERE ls.ix != (
                        SELECT ls.ix
                        FROM ls
                        WHERE (st->>'Id') = ?
                        LIMIT 1
                    )
                )
                UPDATE profile
                SET stack = COALESCE((SELECT newstack FROM fl), '[]'::jsonb)
                WHERE profile.id = ?""";

        preparedStatement = transactionUnit.prepareStatement(removeQuery);
        try {
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setString(2, newDeal.getCardid());
            preparedStatement.setInt(3, user.getId());


            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException("Error updating stack", e);
        }

        String tradeInsertQuery = """
                INSERT INTO TRADE (id, owner, offer, type, mindamage, cardobject)
                VALUES (?, ?, ?, ?,
                ?, ?::jsonb)""";

        preparedStatement = transactionUnit.prepareStatement(tradeInsertQuery);

        try {
            preparedStatement.setString(1, newDeal.getTradeid());
            preparedStatement.setInt(2, user.getId());
            preparedStatement.setObject(3, UUID.fromString(newDeal.getCardid()));
            preparedStatement.setInt(4, typeId);
            preparedStatement.setFloat(5, newDeal.getMindamage());
            preparedStatement.setString(6, objString);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException("Could not insert new trade", e);
        }
        return new Response(HttpStatus.CREATED, ContentType.TEXT, "Trading deal successfully created");
    }

    public Response deleteTrade(User user, String tradeId) {

        // Check if trade exists and belongs to user
        String searchQuery = """
                SELECT * FROM trade
                WHERE id = ?""";
        PreparedStatement preparedStatement = transactionUnit.prepareStatement(searchQuery);
        ResultSet res1 = null;

        try {
            preparedStatement.setString(1, tradeId);

            res1 = preparedStatement.executeQuery();
            if(!res1.next()) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "The provided deal ID was not found.");
            }

            // Check if user owns the trade offer
            if(user.getId() != res1.getInt(2)) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT,
                        "The deal does not belong to the provided user.");
            }

        } catch (SQLException e) {
            throw new DbAccessException("Could not query trade by ID", e);
        }

        // Delete trade and return card to owner
        String returnQuery = """
                WITH r AS (
                    SELECT cardobject AS c
                    FROM trade
                    WHERE id = ?
                    LIMIT 1
                ), dt AS (
                    DELETE FROM trade
                    WHERE id = ?
                    AND owner = ?
                )
                UPDATE profile
                SET stack = stack || r.c::jsonb
                FROM r
                WHERE id = ?""";

        preparedStatement = transactionUnit.prepareStatement(returnQuery);
        try {
            preparedStatement.setString(1, tradeId);
            preparedStatement.setString(2, tradeId);
            preparedStatement.setInt(3, user.getId());
            preparedStatement.setInt(4, user.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }

        return new Response(HttpStatus.OK, ContentType.TEXT, "Trading deal successfully deleted");

    }
    public Response executeTrade(User user, JSONObject card, String tradeId) {

        // Give user card from trade
        String userUpdateQuery = """
                WITH d AS (
                    SELECT cardobject AS c
                    FROM trade
                    WHERE id = ?
                    LIMIT 1
                )
                UPDATE profile
                SET stack = ?::jsonb || d.c::jsonb
                FROM d
                WHERE id = ?
                """;
        try {
            PreparedStatement stmt = transactionUnit.prepareStatement(userUpdateQuery);
            stmt.setString(1, tradeId);
            stmt.setString(2, user.getStack().toString());
            stmt.setInt(3, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }

        // Give trade owner card from user
        String ownerQuery = """
                WITH d AS (
                    SELECT owner AS o
                    FROM trade
                    WHERE id = ?
                    LIMIT 1
                )
                UPDATE profile
                SET stack = stack || ?::jsonb
                WHERE id = (SELECT o FROM d)
                """;
        try {
            PreparedStatement stmt = transactionUnit.prepareStatement(ownerQuery);
            stmt.setString(1, tradeId);
            stmt.setString(2, card.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }

        // Delete trade
        String delQuery = "DELETE FROM trade WHERE id = ?";
        try {
            PreparedStatement stmt = transactionUnit.prepareStatement(delQuery);
            stmt.setString(1, tradeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }


        return new Response(HttpStatus.OK, ContentType.TEXT,
                "Trading deal successfully executed.");
    }


    // Fetch all trades from db
    public Response getTradeListings() {
        String query = """
        SELECT t.id, t.offer, ct.type, t.mindamage FROM trade t
        INNER JOIN cardtype ct ON ct.id = t.type""";

        try {
            PreparedStatement preparedStatement = transactionUnit.prepareStatement(query);
            ResultSet res = preparedStatement.executeQuery();

            if (res.next()) {

                JSONArray jsonArray = new JSONArray();

                do {
                    TradingDeal deal = new TradingDeal(
                            res.getString(1),
                            res.getString(2),
                            res.getString(3),
                            res.getFloat(4)
                    );

                    jsonArray.put(deal.toJSON());
                } while (res.next());

                return new Response(HttpStatus.OK, ContentType.JSON, jsonArray.toString());
            } else {
                return new Response(HttpStatus.NO_CONTENT, ContentType.TEXT, "No trading deals available");
            }

        } catch (SQLException e) {
            throw new DbAccessException("Error getting trades listings");
        }


    }

    public static TradingDeal getTradingDeal(String dealId) {
        String query = """
                SELECT t.id, t.offer, ct.type, t.mindamage, t.owner
                FROM trade t
                JOIN cardtype ct
                ON ct.id = t.type
                WHERE t.id = ?
                LIMIT 1""";

        try {
            TransactionUnit tempUnit = new TransactionUnit();
            PreparedStatement stmt = tempUnit.prepareStatement(query);
            stmt.setString(1, dealId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new TradingDeal(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getFloat(4),
                        rs.getInt(5)
                );
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }






}
