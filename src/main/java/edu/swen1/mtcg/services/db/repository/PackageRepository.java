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
            for(int j = 0; j < cardIdList.size(); j++) {
                String tempString = cardIdList.get(j);
                if (obj.has(tempString)) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Duplicate card ID in pack");
                }
            }

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


    public Response buyPack(User user) {

        // Check if any pack is available in db
        String query = "SELECT id FROM pack WHERE amount > 0 ORDER BY RANDOM() LIMIT 1";
        PreparedStatement stmt = this.transactionUnit.prepareStatement(query);
        ResultSet result = null;
        JSONArray targetPack = null;

        try {
            result = stmt.executeQuery();
            if(!result.next()) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "No card package available for buying");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String addQuery = """
                WITH pk AS (
                    SELECT id, content FROM pack
                    WHERE amount > 0
                    ORDER BY RANDOM()
                    LIMIT 1
                ), pu AS (
                    UPDATE pack
                    SET amount = amount - 1
                    WHERE id = pk.id
                    FROM pk
                )
                UPDATE profile
                SET stack = stack || pk.content::jsonb,
                coins = coins - 5
                WHERE id = ?
                FROM pk""";

        try {
            stmt = transactionUnit.prepareStatement(addQuery);
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DbAccessException(e);
        }




        return new Response(HttpStatus.OK, ContentType.JSON, targetPack.toString());
    }


}
