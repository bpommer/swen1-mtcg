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

public class TradeRepository {
    private TransactionUnit transactionUnit;
    public TradeRepository(TransactionUnit transactionUnit) { this.transactionUnit = transactionUnit; }

    // Insert new trade
    public Response createNewTrade(User user, TradingDeal newDeal) {

        // Check if trade id already exists
        String preQuery = "SELECT t.*,  FROM trade t WHERE id = ?";
        PreparedStatement preparedStatement = transactionUnit.prepareStatement(preQuery);
        try {
            preparedStatement.setString(1, newDeal.getTradeid());
            ResultSet res1 = preparedStatement.executeQuery();
            if(res1.next()) {
                return new Response(HttpStatus.CONFLICT, ContentType.TEXT,
                        "A deal with this deal ID already exists.");
            }

        } catch (SQLException e) {
            throw new DbAccessException("Could not insert new trade", e);
        }



        // Check if user owns card by querying stack
        String objString = null;
        String ownerQuery = """
                SELECT jsonb_path_query(stack, ?::jsonpath)
                FROM profile
                WHERE id = ?
                LIMIT 1""";
        preparedStatement = transactionUnit.prepareStatement(ownerQuery);
        try {
            String jsonPath = String.format("$[*] ? (@.Id == \"%s\")", newDeal.getCardid());

            preparedStatement.setString(1, jsonPath);
            preparedStatement.setInt(2, newDeal.getOwnerId());
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

        //
        // Remove single card instance with matching ID from stack
        // and create new trade deal.
        //
        // - ls: Fetch all cards from stack by ID and give each card a unique row number
        // - fl: Filter out single card instance by ID from ls
        // - uc: Fetch filtered card from ls
        // - t: Insert trade with card received from uc
        // - Update user stack with either fl or empty array

        String removeQuery = """
                WITH ls AS (
                    SELECT
                        jsonb_array_elements(stack) AS st,
                        generate_series(1, jsonb_array_length(stack)) AS in
                    FROM profile
                    WHERE profile.id = ?
                    ORDER BY (stack ->>'Id')::UUID
                ), fl AS (
                    SELECT jsonb_agg(ls.st) AS newstack
                    FROM ls
                    WHERE ls.in != (
                        SELECT ls.in
                        FROM ls
                        WHERE (st->>'Id') = ?
                        ORDER BY ls.in
                        LIMIT 1
                    )
                ), uc AS (
                    SELECT ls.st AS usercard
                    FROM ls
                    WHERE ls.in = (
                        SELECT ls.in
                        FROM ls
                        WHERE (st->>'Id') = ?
                        ORDER BY ls.in
                        LIMIT 1
                    )
                    LIMIT 1
                ), t AS (
                    INSERT INTO trade VALUES (
                        ?, ?, ?,
                            (SELECT id FROM cardtype WHERE type = ?),
                            ?, (SELECT uc.usercard::jsonb FROM uc)
                        )
                )
                UPDATE profile
                SET stack = COALESCE(fl.newstack, '[]'::jsonb)
                FROM fl
                WHERE id = ?""";

        preparedStatement = transactionUnit.prepareStatement(removeQuery);
        try {
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setString(2, newDeal.getCardid());
            preparedStatement.setString(3, newDeal.getCardid());
            preparedStatement.setString(4, newDeal.getTradeid());
            preparedStatement.setInt(5, user.getId());
            preparedStatement.setString(6, newDeal.getCardid());
            preparedStatement.setString(7, newDeal.getType());
            preparedStatement.setFloat(8, newDeal.getMindamage());
            preparedStatement.setInt(9, user.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException("Could not create new trade", e);
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
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error");
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
    public Response executeTrade(User user, String cardid, String tradeId) {

        // Fetch trade from db if it exists
        String tradeQuery = """
                SELECT id, offer, ct.type, mindamage, owner, cardobject FROM trade t
                WHERE id = ?
                INNER JOIN cardtype ct
                ON t.type = ct.id""";
        PreparedStatement preparedStatement = transactionUnit.prepareStatement(tradeQuery);
        TradingDeal deal = null;
        ResultSet targetTrade = null;
        try {
            preparedStatement.setString(1, tradeId);
            targetTrade = preparedStatement.executeQuery();
            if (!targetTrade.next()) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "The provided deal ID was not found.");
            }

            // Check for trade with self
            if (user.getId() == targetTrade.getInt(5)) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT,
                        "The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck, or the user tries to trade with self");
            }

            deal = new TradingDeal(targetTrade.getString(1),
                    targetTrade.getString(2),
                    targetTrade.getString(3),
                    targetTrade.getFloat(4),
                    targetTrade.getInt(5));
            deal.setTargetCard(new JSONObject(targetTrade.getString(6)));


        } catch (SQLException e) {
            throw new DbAccessException(e.getMessage());
        }

        // Check if user owns offered card
        // and if stats match offer requirements
        // (also implicit check if card ID is registered in db)

        String checkQuery = """
                SELECT
                    p.id,
                    cards->>'Id' AS cid,
                    cards->>'Damage' AS dmg,
                    c.type,
                    cards
                FROM profile p
                JOIN LATERAL jsonb_array_elements(p.stack) AS cards ON true
                JOIN card c
                ON (cards->>'Id')::uuid = ?
                WHERE p.id = ?
                AND (cards->>'Damage')::float >= ?
                AND (cards->>'Id')::uuid = ?
                AND c.type = ?""";

        preparedStatement = transactionUnit.prepareStatement(checkQuery);
        ResultSet res = null;
        String foundCard = null;
        try {
            preparedStatement.setString(1, cardid);
            preparedStatement.setInt(2, user.getId());
            preparedStatement.setFloat(3, deal.getMindamage());
            preparedStatement.setString(4, cardid);
            preparedStatement.setString(5, deal.getType());

            res = preparedStatement.executeQuery();

            if (!res.next()) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT,
                        "The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck, or the user tries to trade with self");
            } else {
                foundCard = res.getString(5);
            }

        } catch (SQLException e) {
            throw new DbAccessException(e);
        }

        // Delete card from user stack and give requested card to user
        // - fl, uc: Filter card out of user deck
        // - tc: Fetch card and owner ID from trade
        // - oq: Give filtered card to trade owner
        // - dt: Delete trade from db
        // - Update user stack with filtered stack and card from trade
        String userCardQuery = """
                WITH ls AS (
                                SELECT
                                    jsonb_array_elements(stack) AS st,
                                    generate_series(1, jsonb_array_length(stack)) AS in
                                FROM profile
                                WHERE profile.id = ?
                                ORDER BY (stack ->>'Id')::UUID
                            ), fl AS (
                                SELECT jsonb_agg(ls.st) AS newstack
                                FROM ls
                                WHERE ls.in != (
                                    SELECT ls.in
                                    FROM ls
                                    WHERE (st->>'Id') = ?
                                    ORDER BY ls.in
                                    LIMIT 1
                                )
                            ), uc AS (
                                SELECT ls.st AS newstack
                                FROM ls
                                WHERE ls.in = (
                                    SELECT ls.in
                                    FROM ls
                                    WHERE (st->>'Id') = ?
                                    ORDER BY ls.in
                                    LIMIT 1
                                )
                            ), tc AS (
                                SELECT cardobject AS obj, owner AS cardowner
                                FROM trade
                                WHERE id = ?
                                LIMIT 1
                            ), oq AS (
                                UPDATE profile
                                SET stack = stack || (SELECT uc.newstack FROM uc)
                                WHERE id = (SELECT tc.cardowner FROM tc)
                            ), dt AS (
                                DELETE FROM trade
                                WHERE id = ?
                                AND owner = (SELECT tc.cardowner FROM tc)
                            )
                            UPDATE profile
                            SET stack = COALESCE(fl.newstack, '[]'::jsonb)  || (tc.obj::jsonb)
                            FROM fl, tc
                            WHERE id = ?""";
        preparedStatement = transactionUnit.prepareStatement(userCardQuery);

        try {
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setString(2, cardid);
            preparedStatement.setString(3, cardid);
            preparedStatement.setString(4, tradeId);
            preparedStatement.setString(5, tradeId);
            preparedStatement.setInt(6, user.getId());

            preparedStatement.executeUpdate();
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
            ResultSet res = null;
            preparedStatement.executeQuery();

            if(!res.next()) {
                return new Response(HttpStatus.NO_CONTENT, ContentType.TEXT, "No trading deals available");
            } else {

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
            }

        } catch (SQLException e) {
            throw new DbAccessException("Error getting trades listings");
        }








    }




}
