package edu.swen1.mtcg.services.db.repository;

import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import org.json.JSONArray;
import org.json.JSONObject;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

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

    private void getTradeData() {
        String query = """
                SELECT t.id, t.owner, t.offer, ct.type, t.mindamage FROM trade t
                INNER JOIN cardtype ct ON t.type = ct.id""";
        try (PreparedStatement stmt = this.transactionUnit.prepareStatement(query))
        {
            this.cardData = stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // Generate Hashset for integrity checks
    public static HashMap<String, String> getCardHashMap() throws Exception {

        String preQuery = "SELECT COUNT(*) FROM card";

        String query = "SELECT id, hash FROM card";

        TransactionUnit tempUnit = new TransactionUnit();
        HashMap<String, String> cardMap = new HashMap<>();

        // Check if card table contains any entries and if not return empty Hashset
        try(tempUnit) {
            PreparedStatement stmt = tempUnit.prepareStatement(preQuery);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if(rs.getInt(1) == 0) {
                    return cardMap;
                }
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
                    cardMap.put(res.getString(1), res.getString(2));
                } while (res.next());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cardMap;
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





    public JSONArray getTrades() throws SQLException {

        getTradeData();
        JSONArray trades = new JSONArray();
        while(tradeData.next()) {
            JSONObject tempTrade = new JSONObject();
            tempTrade.put("Id", tradeData.getString(1));
            tempTrade.put("CardToTrade", tradeData.getString(3));
            tempTrade.put("Type", tradeData.getString(4));
            tempTrade.put("MinimumDamage", tradeData.getFloat(5));
            trades.put(tempTrade);
        }
        return trades;
    }




}
