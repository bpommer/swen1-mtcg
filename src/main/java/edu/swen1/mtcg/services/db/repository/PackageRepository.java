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
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import static edu.swen1.mtcg.services.db.repository.CardDataRepository.getCardHashSet;
import static edu.swen1.mtcg.utils.HashGenerator.generateHash;

public class PackageRepository {

    private TransactionUnit transactionUnit;
    public PackageRepository(TransactionUnit transactionUnit) { this.transactionUnit = transactionUnit;}


    public Response registerPackage(JSONArray pack) {

        // Validate pack format and check for existing entry
        if(true) {
            System.out.println("Package validation failed");
            return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "");
        }

        // Get all card hashes in db and search in hashmap
        String[] deckHashes = new String[pack.length()];
        try {
            HashSet<String> cardHashes = getCardHashSet();

            // Handling if Hashmap is empty
            if(!cardHashes.isEmpty()) {
                for(int i = 0; i < pack.length(); i++) {

                    JSONObject tempJson = pack.getJSONObject(i);

                    if(cardHashes.contains(tempJson.getString("Id"))) {
                        return new Response(HttpStatus.CONFLICT,
                                ContentType.TEXT, "At least one card in the packages already exists");
                    }
                }
            }

        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error");
        }

        // Infer type, element and special with BattleCard model

        ArrayList<BattleCard> packList = new ArrayList<>();
        for(int i = 0; i < pack.length(); i++) {
            JSONObject tempJson = pack.getJSONObject(i);
            packList.add(new BattleCard(tempJson.getString("Id"),
                    tempJson.getString("Name"), tempJson.getFloat("Damage")));
        }

        // Register cards in db
        for(int i = 0; i < packList.size(); i++) {
            try {
                insertCard(packList.get(i));
            } catch (Exception e) {
                e.printStackTrace();
                throw new DbAccessException(e.getMessage());

            }

        }

        // Extract and save IDs as JSON array


        try {
            addPack(pack);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error");
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
