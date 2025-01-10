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
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import static edu.swen1.mtcg.services.db.repository.CardDataRepository.getCardHashMap;
import static edu.swen1.mtcg.utils.HashGenerator.generateHash;

public class PackageRepository {

    private TransactionUnit transactionUnit;
    public PackageRepository(TransactionUnit transactionUnit) { this.transactionUnit = transactionUnit;}


    public Response registerPackage(JSONArray pack) {

        // Validate pack format and check for existing entry
        if(!validatePack(pack)) {
            System.out.println("Package validation failed");
            return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "");
        }

        // Get all card hashes in db and search in hashmap
        String[] deckHashes = new String[pack.length()];
        try {
            HashMap<String, String> cardHashes = getCardHashMap();

            if(cardHashes.isEmpty()) {
                for(int i = 0; i < pack.length(); i++) {

                    JSONObject tempJson = pack.getJSONObject(i);
                    System.out.println(tempJson.toString());
                    String packHash = generateHash(tempJson.toString());
                    System.out.println(packHash);
                    deckHashes[i] = packHash;
                }
            }
            else {
                for(int i = 0; i < pack.length(); i++) {

                    JSONObject tempJson = pack.getJSONObject(i);
                    String packHash = generateHash(tempJson.toString());
                    if(cardHashes.containsKey(tempJson.getString("Id"))
                            || cardHashes.get(tempJson.getString("Id")).equals(packHash)) {
                        return new Response(HttpStatus.CONFLICT,
                                ContentType.TEXT, "At least one card in the packages already exists");
                    }
                    else {
                        deckHashes[i] = packHash;
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Infer type, element and special with BattleCard model

        ArrayList<BattleCard> packList = new ArrayList<>();
        for(int i = 0; i < pack.length(); i++) {
            JSONObject tempJson = pack.getJSONObject(i);
            packList.add(new BattleCard(tempJson.getString("Id"),
                    tempJson.getString("Name"), tempJson.getFloat("Damage")));
        }

        for(int i = 0; i < packList.size(); i++) {
            try {
                insertCard(packList.get(i), deckHashes[i]);
            } catch (Exception e) {
                e.printStackTrace();
                throw new DbAccessException(e.getMessage());

            }

        }

        // Extract and save IDs as JSON array
        JSONArray idPack = new JSONArray();
        for (BattleCard battleCard : packList) {
            idPack.put(battleCard.getId());
        }

        try {
            addPack(idPack);
        } catch (Exception e) {
            throw new DbAccessException(e.getMessage());
        }

        return new Response(HttpStatus.CREATED, ContentType.TEXT, "Package and cards successfully created");


    }



    private void addPack(JSONArray pack) {

        /*ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;

        try {
            node = mapper.readTree(pack.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        PGobject packJsonb = new PGobject();
        packJsonb.setType("jsonb");
        try {
            packJsonb.setValue(node.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/

        try {

            String insertQuery = "INSERT INTO pack (content) VALUES (?::jsonb)";
            PreparedStatement stmt = this.transactionUnit.prepareStatement(insertQuery);
            stmt.setString(1, pack.toString());
            int rowCount = stmt.executeUpdate();
            stmt.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new DbAccessException("Could not insert pack", e);
        }


    }

    private void insertCard(BattleCard card, String cardHash) {

        String query = "INSERT INTO card VALUES (?, ?, ?, ?, ?, ?, ?)";

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
            stmt.setString(7, cardHash);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }



    private boolean validatePack(JSONArray pack)  {

        HashSet<String> validKeys = new HashSet<>();
        validKeys.add("Id");
        validKeys.add("Name");
        validKeys.add("Damage");

        System.out.println(validKeys);

        if(pack.length() != 5) { return false; }

        for(int i = 0; i < pack.length(); i++) {

            String tempJson = pack.getJSONObject(i).toString();

            System.out.println(tempJson);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = null;
            try {
                node = mapper.readTree(tempJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if(keyWhitelistCheck(node, validKeys)) {
                JsonNode idNode = node.get("Id");
                JsonNode nameNode = node.get("Name");
                JsonNode damageNode = node.get("Damage");

                System.out.println(idNode.toString());
                System.out.println(nameNode.toString());
                System.out.println(damageNode.toString());


                if(idNode.isTextual() && nameNode.isTextual() && damageNode.isNumber()) {
                    System.out.println("Type check passed");
                }
                else {
                    System.out.println("Type check failed");
                    return false;
                }

            }
            else {
                System.out.println("Key not whitelisted");
                return false;
            }
        }
        return true;
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
