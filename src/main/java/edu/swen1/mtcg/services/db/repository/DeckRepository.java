package edu.swen1.mtcg.services.db.repository;

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
import java.util.HashMap;

import edu.swen1.mtcg.utils.IQueryBuilder;

public class DeckRepository implements IQueryBuilder {

    public final int DECK_SIZE = 4;
    private TransactionUnit transactionUnit;
    public DeckRepository(TransactionUnit transactionUnit) {
        this.transactionUnit = transactionUnit;
    }

    public Response getDeck(int userId, HashMap<String, String> params) {
        String query = """
                    SELECT jsonb_array_elements_text(deck) AS decktext,
                    jsonb_array_elements(deck) AS deckobject
                    FROM profile WHERE id = ?""";
        query = buildParamQuery(query, params);

        PreparedStatement stmt = this.transactionUnit.prepareStatement(query);
        try {
            stmt.setInt(1, userId);
            ResultSet res = stmt.executeQuery();

            if(res.next()) {
                JSONArray userDeckJSON = new JSONArray();

                do {
                    JSONObject userCard = new JSONObject(res.getString(1));
                    userDeckJSON.put(userCard);


                } while(res.next());


                if(userDeckJSON.isEmpty()) {
                    return new Response(HttpStatus.NO_CONTENT, ContentType.TEXT,
                            "The request was fine, but the deck doesn't have any cards");
                } else if(params.get("format").equals("plain")) {

                    StringBuilder builder = new StringBuilder();
                    for(int i = 0; i < userDeckJSON.length(); i++) {
                        JSONObject userDeckCard = userDeckJSON.getJSONObject(i);
                        builder.append("Id: ").append(userDeckCard.getString("Id")).append("\n");
                        builder.append("Name: ").append(userDeckCard.getString("Name")).append("\n");
                        builder.append("Damage: ").append(userDeckCard.getFloat("Damage")).append("\n\n");
                    }
                    return new Response(HttpStatus.OK, ContentType.TEXT, builder.toString());
                } else {
                    return new Response(HttpStatus.OK, ContentType.JSON, userDeckJSON.toString());
                }
            }
            return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "User not found" );
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal Server Error");
        }
    }

    public Response updateDeck(User user) {

        String updateQuery = """
                UPDATE profile
                SET deck = ?::jsonb,
                stack = ?::jsonb
                WHERE id = ?
                """;

        try {
            PreparedStatement stmt = this.transactionUnit.prepareStatement(updateQuery);
            stmt.setString(1, user.getDeck().toString());
            stmt.setString(2, user.getStack().toString());
            stmt.setInt(3, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException("Could not update deck", e);
        }

        return new Response(HttpStatus.OK, ContentType.TEXT, "The deck has been successfully configured");

    }

    @Override
    public String buildParamQuery(String baseQuery, HashMap<String, String> parameters) {

        if(parameters.containsKey("sort")) {

            switch(parameters.get("sort")) {
                case "+name":
                    baseQuery = baseQuery + " ORDER BY (deckobject-->'Name') ASC";
                    break;
                case "-name":
                    baseQuery = baseQuery + " ORDER BY (deckobject-->'Name') DESC";
                    break;
                case "+damage":
                    baseQuery = baseQuery + " ORDER BY (deckobject-->'Damage')::FLOAT ASC";
                    break;
                case "-damage":
                    baseQuery = baseQuery + " ORDER BY (deckobject-->'Damage') DESC";
                    break;
                case "+id":
                    baseQuery = baseQuery + " ORDER BY (deckobject-->'Id') ASC";
                    break;
                case "-id":
                    baseQuery = baseQuery + " ORDER BY (deckobject-->'Id') DESC";
                    break;
                default:
                    return baseQuery;
            }
            return baseQuery;



        }


        return baseQuery;
    }
}
