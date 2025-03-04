package edu.swen1.mtcg.services.db.repository;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.BattleCard;
import edu.swen1.mtcg.services.db.models.Card;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import org.json.JSONArray;
import org.json.JSONObject;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class CardDataRepository {

    private TransactionUnit transactionUnit;
    public CardDataRepository(TransactionUnit transactionUnit) {
        this.transactionUnit = transactionUnit;
    }
    private ResultSet cardData;
    private ResultSet tradeData;


    // Check if user exists
    private void getAllCardData() {

        String query = """
                SELECT c.id, ct.type, c.name,
                c.damage, e.name, st.special FROM card c
                INNER JOIN cardtype ct ON c.type = ct.id
                INNER JOIN element e ON e.id = c.element
                INNER JOIN special s ON s.id = c.special""";

        try (PreparedStatement stmt = this.transactionUnit.prepareStatement(query)) {
            this.cardData = stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Card getCardData(String id) {
        TransactionUnit tempUnit = new TransactionUnit();

        String query = """
                SELECT c.id, c.name, c.damage
                FROM card c
                WHERE c.id = ?::uuid""";

        try(PreparedStatement stmt = tempUnit.prepareStatement(query)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                Card card = new BattleCard(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getFloat(3)
                );

                return card;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }

    }


    public Response getTradeData() {
        String query = """
                SELECT t.id, t.owner, t.offer, ct.type, t.mindamage FROM trade t
                INNER JOIN cardtype ct ON t.type = ct.id""";
        try (PreparedStatement stmt = this.transactionUnit.prepareStatement(query))
        {
            ResultSet rs = stmt.executeQuery();
            JSONArray trades = new JSONArray();

            if(rs.next()) {
                do {
                    TradingDeal deal = new TradingDeal(rs.getString(1), rs.getString(3),
                            rs.getString(4), rs.getFloat(5));
                    trades.put(deal.toJSON());
                } while (rs.next());
            } else {
                return new Response(HttpStatus.NO_CONTENT, ContentType.TEXT, "No trades found");
            }

            return new Response(HttpStatus.OK, ContentType.JSON, trades.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error");
        }

    }




    // Generate Hashset for integrity checks
    public static HashSet<String> getCardHashSet() {

        String preQuery = "SELECT * FROM card";

        String query = "SELECT id FROM card";

        TransactionUnit tempUnit = new TransactionUnit();
        HashSet<String> cardSet = new HashSet<>();

        // Check if card table contains any entries and if not return empty Hashset
        try(tempUnit) {
            PreparedStatement stmt = tempUnit.prepareStatement(preQuery);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return cardSet;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        TransactionUnit newUnit = new TransactionUnit();



        try(newUnit) {
            PreparedStatement stmt = newUnit.prepareStatement(query);
            ResultSet res = stmt.executeQuery();
            if (res.next()) {

                do {
                    cardSet.add(res.getString(1));
                } while (res.next());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cardSet;
    }


    public static HashMap<String, Integer> getBattleProperties(String table) {

        TransactionUnit tempUnit = new TransactionUnit();
        HashMap<String, Integer> props = new HashMap<>();

        String query;
        if(table.equals("cardtype")) {
            query = "SELECT * FROM cardtype WHERE NOT type = 'Monster'";
        } else if(table.equals("specialtype")) {
            query = "SELECT * FROM specialtype";
        } else if(table.equals("element")) {
            query = "SELECT * FROM element WHERE NOT type = 'Normal'";
        } else {
            return null;
        }



        PreparedStatement stmt = tempUnit.prepareStatement(query);


        try {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                props.put(rs.getString(2), rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return props;
    }

    // Fetch cards from username
    public static JSONArray getUserCards(String username) {
        String query = "SELECT stack FROM profile WHERE username = ?";
        try (TransactionUnit tempUnit = new TransactionUnit()) {
            PreparedStatement stmt = tempUnit.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                String stack = rs.getString(1);
                JSONArray cards = new JSONArray(stack);
                return cards;
            } else {
                return new JSONArray();
            }
        } catch (Exception e) {
            return new JSONArray();
        }
    }



}
