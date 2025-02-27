package edu.swen1.mtcg.services.db.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.services.db.models.BattleCard;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;

import edu.swen1.mtcg.services.db.dbaccess.DbAccessException;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.utils.BattleCardFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static edu.swen1.mtcg.services.db.repository.CardDataRepository.getCardHashSet;
import static edu.swen1.mtcg.utils.HashGenerator.generateHash;

public class PackageRepository {
    public final int CARD_COUNT = 5;
    private TransactionUnit transactionUnit;
    public PackageRepository(TransactionUnit transactionUnit) { this.transactionUnit = transactionUnit;}


    public Response registerPackage(JSONArray pack) {

        if(pack.length() != CARD_COUNT) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Pack does not contain exactly 5 cards");
        }

        ArrayList<String> cardIdList = new ArrayList<String>();
        for(int i = 0; i < pack.length(); i++) {
            JSONObject obj = pack.getJSONObject(i);
            cardIdList.add(obj.getString("Id"));
        }

        // Check if card ID exists in db
        try {
            String checkQuery = "SELECT id FROM card WHERE id IN (?, ?, ?, ?, ?)";
            PreparedStatement stmt = this.transactionUnit.prepareStatement(checkQuery);

            stmt.setString(1, cardIdList.get(0));
            stmt.setString(2, cardIdList.get(1));
            stmt.setString(3, cardIdList.get(2));
            stmt.setString(4, cardIdList.get(3));
            stmt.setString(5, cardIdList.get(4));
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                return new Response(HttpStatus.CONFLICT, ContentType.TEXT,
                        "At least one card in the packages already exists");
            }
        } catch(SQLException e) {
            throw new DbAccessException("Card ID query failed", e);
        }

        // Infer type, element and special from cards and register in db


        String registerQuery = """
                    INSERT INTO card VALUES (?,
                    ?, ?, ?, ?, ?, ?::jsonb)""";

        BattleCardFactory battleCardFactory = new BattleCardFactory();


        for(int i = 0; i < pack.length(); i++) {
            BattleCard newCard = battleCardFactory.buildBattleCard(pack.getJSONObject(i));

            try {
                PreparedStatement stmt = this.transactionUnit.prepareStatement(registerQuery);
                stmt.setString(1, newCard.getId());
                stmt.setInt(2, newCard.getTypeId());
                stmt.setString(3, newCard.getName());
                stmt.setFloat(4, newCard.getDamage());
                stmt.setInt(5, newCard.getElementId());
                if(newCard.getSpecialId() != null) {
                    stmt.setInt(6, newCard.getSpecialId());
                } else {
                    stmt.setNull(6, Types.INTEGER);
                }
                stmt.setString(7, newCard.toJSON().toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new DbAccessException(e);
            }
        }

        String insertPackQuery = "INSERT INTO pack VALUES (DEFAULT, ?::json, DEFAULT)";
        try {
            PreparedStatement stmt = this.transactionUnit.prepareStatement(insertPackQuery);
            stmt.setString(1, pack.toString());
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }
        return new Response(HttpStatus.CREATED, ContentType.TEXT, "Package and cards successfully created");


    }


    // Insert pack into db
    private void addPack(JSONArray pack) throws Exception {

        try {

            String insertQuery = "INSERT INTO pack (content) VALUES (?::json)";
            PreparedStatement stmt = this.transactionUnit.prepareStatement(insertQuery);
            stmt.setString(1, pack.toString());
            stmt.executeUpdate();
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Could not insert pack", e);
        }

    }

    // Register card in db
    private void insertCard(BattleCard card) {

        String query = "INSERT INTO card VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = this.transactionUnit.prepareStatement(query);

        try {
            stmt.setString(1, card.getId());
            stmt.setInt(2, card.getTypeId());
            stmt.setString(3, card.getName());
            stmt.setFloat(4, card.getDamage());
            stmt.setInt(5, card.getElementId());

            if(card.getSpecialId() != 0) {
                stmt.setInt(6, card.getSpecialId());
            }
            else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public Response buyPack(User user, int remainingCoins) {

        // Fetch pack from db
        String query = "SELECT * FROM pack WHERE sold = FALSE ORDER BY RANDOM() LIMIT 1";
        PreparedStatement stmt = this.transactionUnit.prepareStatement(query);
        int packId = -1;

        ResultSet result = null;
        JSONArray targetPack = null;

        try {
            result = stmt.executeQuery();
            if(result.next()) {
                packId = result.getInt(1);
                targetPack = new JSONArray(result.getString(2));

            } else {
                return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "No card package available for buying");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Add cards from pack to user stack
        JSONArray userStack = user.getStack();
        for(int i = 0; i < targetPack.length(); i++) {
            JSONObject tempJson = targetPack.getJSONObject(i);
            userStack.put(tempJson);
        }

        // Set chosen pack to sold
        String soldQuery = "UPDATE pack SET sold = TRUE WHERE id = ?";
        stmt = this.transactionUnit.prepareStatement(soldQuery);
        try {
            stmt.setInt(1, packId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }

        // Update user stack
        String updateUserQuery = "UPDATE profile SET coins = ?, stack = ?::json WHERE id = ?";
        stmt = this.transactionUnit.prepareStatement(updateUserQuery);
        try {
            stmt.setInt(1, remainingCoins);
            stmt.setString(2, userStack.toString());
            stmt.setInt(3, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }
        return new Response(HttpStatus.OK, ContentType.JSON, targetPack.toString());
    }


    public static boolean keyWhitelistCheck(JsonNode target, HashSet<String> whitelist) {

        HashSet<String> content = new HashSet<>();
        Iterator<String> keys = target.fieldNames();
        while(keys.hasNext()) {
            String key = keys.next();
            if(!whitelist.contains(key)) {
                return false;
            }
        }
        return true;
    }


}
