package edu.swen1.mtcg.services.db.repository;

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

public class PackageRepository {
    public final int PACK_SIZE = 5;
    private TransactionUnit transactionUnit;
    public PackageRepository(TransactionUnit transactionUnit) { this.transactionUnit = transactionUnit;}


    public Response registerPackage(JSONArray pack) {

        if(pack.length() != PACK_SIZE) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Pack does not contain exactly 5 cards\n");
        }

        // Load card IDs into ArrayList and check for duplicate IDs

        String[] cardIdList = new String[PACK_SIZE];
        HashSet<String> idSet = new HashSet<String>();
        for(int i = 0; i < pack.length(); i++) {
            JSONObject obj = pack.getJSONObject(i);
            String tempId = obj.getString("Id");
            if(idSet.contains(tempId)) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Duplicate card ID in pack\n");
            } else {
                idSet.add(tempId);
                cardIdList[i] = tempId;
            }
        }

        // Check if card ID exists in db
        try {
            String checkQuery = "SELECT id FROM card WHERE id IN (?, ?, ?, ?, ?)";
            PreparedStatement stmt = this.transactionUnit.prepareStatement(checkQuery);

            for(int i = 0; i < PACK_SIZE; i++) {
                stmt.setObject((i + 1), UUID.fromString(cardIdList[i]));
            }
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                return new Response(HttpStatus.CONFLICT, ContentType.TEXT,
                        "At least one card in the packages already exists\n");
            }
        } catch(SQLException e) {
            throw new DbAccessException("Card ID query failed", e);
        }

        String registerQuery = """
                    INSERT INTO card VALUES (?,
                    ?, ?, ?, ?, ?, ?::jsonb)""";

        // Infer type, element and special from cards and register in db
        BattleCardFactory battleCardFactory = new BattleCardFactory();

        for(int i = 0; i < pack.length(); i++) {
            BattleCard newCard = battleCardFactory.buildBattleCard(pack.getJSONObject(i));

            try {
                PreparedStatement stmt = this.transactionUnit.prepareStatement(registerQuery);
                stmt.setObject(1, UUID.fromString(newCard.getId()));
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
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException("Failed to insert pack");
        }
        return new Response(HttpStatus.CREATED, ContentType.TEXT, "Package and cards successfully created.\n");


    }


    public Response buyPack(User user) {

        // Check if any pack is available in db
        String query = "SELECT id, content FROM pack WHERE amount > 0 ORDER BY id ASC LIMIT 1";
        PreparedStatement stmt = this.transactionUnit.prepareStatement(query);
        int packId = 0;
        String packContent = "";

        try {
            ResultSet result = null;
            result = stmt.executeQuery();
            if(!result.next()) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "No card package available for buying.\n");
            } else {
                packId = result.getInt(1);
                packContent = result.getString(2);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        // Insert pack into user stack and update user coins
        JSONArray targetPack = null;
        String addQuery = """
                WITH pk AS (
                    SELECT content AS c FROM pack
                    WHERE pack.id = ?
                    AND amount > 0
                    LIMIT 1
                )
                UPDATE profile
                SET stack = stack || (SELECT c FROM pk)::jsonb,
                coins = coins - 5
                WHERE profile.id = ?""";

        try {
            stmt = transactionUnit.prepareStatement(addQuery);
            stmt.setInt(1, packId);
            stmt.setInt(2, user.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DbAccessException(e);
        }

        // Remove 1 instance of the pack from db
        String removeQuery = """
                UPDATE pack
                SET amount = amount - 1
                WHERE pack.id = ?
                """;
        try {
            stmt = transactionUnit.prepareStatement(removeQuery);
            stmt.setInt(1, packId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }


        return new Response(HttpStatus.OK, ContentType.JSON, packContent);
    }


}
