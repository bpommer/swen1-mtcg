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
                } else if(params.equals("format=plain")) {

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

    public Response updateDeck(JSONArray newDeck, User user) {

        if(newDeck.length() != DECK_SIZE) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "The provided deck did not include the required amount of cards");
        }


        // If deck is not empty, shuffle back into stack
        String shuffleQuery = """
                WITH d AS (
                    SELECT deck FROM profile WHERE id = ?
                ), us AS (
                    UPDATE profile SET stack = stack || COALESCE(d.deck, '[]'::jsonb)
                )
                UPDATE profile
                SET deck = '[]'::jsonb
                """;
        try {
            PreparedStatement stmt = this.transactionUnit.prepareStatement(shuffleQuery);
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbAccessException(e);
        }

        // Check if user owns all cards specified for the deck
        // This query also takes duplicates into account by assigning each card a unique row number
        String searchQuery = """
                WITH ls AS (
                    SELECT
                    jsonb_array_elements(stack) AS st,
                    generate_series(1, jsonb_array_length(stack)) AS in
                    FROM profile
                    WHERE profile.id = ?
                    ORDER BY (stack ->>'Id')::UUID
                ), c1 AS (
                    SELECT ls.st, ls.in
                    FROM ls
                    WHERE st->>'Id' = ?
                    LIMIT 1
                ), c2 AS (
                    SELECT ls.st, ls.in
                    FROM ls, c1
                    WHERE st->>'Id' = ?
                    AND ls.in != c1.in
                    LIMIT 1
                ), c3 AS (
                    SELECT ls.st, ls.in
                    FROM ls, c1, c2
                    WHERE st->>'Id' = ?
                    AND ls.in NOT IN (c1.in, c2.in)
                    LIMIT 1
                ), c4 AS (
                    SELECT ls.st, ls.in
                    FROM ls, c1, c2, c3
                    WHERE st->>'Id' = ?
                    AND ls.in NOT IN (c1.in, c2.in, c3.in)
                    LIMIT 1
                )
                SELECT COUNT(*) FROM (
                    SELECT * FROM c1
                    UNION ALL
                    SELECT * FROM c2
                    UNION ALL
                    SELECT * FROM c3
                    UNION ALL
                    SELECT * FROM c4
                    UNION ALL
                ) AS count
                """;

        try {
            PreparedStatement stmt = this.transactionUnit.prepareStatement(shuffleQuery);
            stmt.setInt(1, user.getId());
            stmt.setString(2, newDeck.get(1).toString());
            stmt.setString(3, newDeck.get(2).toString());
            stmt.setString(4, newDeck.get(3).toString());
            stmt.setString(5, newDeck.get(4).toString());
            ResultSet rs = stmt.executeQuery();

            if(rs.next() && rs.getInt("count") != DECK_SIZE) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT,
                        "At least one of the provided cards does not belong to the user or is not available.");
            }

        } catch (SQLException e) {
            throw new DbAccessException(e);
        }


        String setDeckQuery = """
                WITH ls AS (
                    SELECT
                    jsonb_array_elements(stack) AS st,
                    generate_series(1, jsonb_array_length(stack)) AS in
                    FROM profile
                    WHERE profile.id = ?
                    ORDER BY (stack ->>'Id')::UUID
                ), c1 AS (
                    SELECT ls.st AS card1, ls.in AS index
                    FROM ls
                    WHERE st->>'Id' = ?
                    LIMIT 1
                ), c2 AS (
                    SELECT ls.st AS card2, ls.in AS index
                    FROM ls, c1
                    WHERE st->>'Id' = ?
                    AND ls.in != c1.in
                    LIMIT 1
                ), c3 AS (
                    SELECT ls.st AS card3, ls.in AS index
                    FROM ls, c1, c2
                    WHERE st->>'Id' = ?
                    AND ls.in NOT IN (c1.index, c2.index)
                    LIMIT 1
                ), c4 AS (
                    SELECT ls.st AS card4, ls.in AS index
                    FROM ls, c1, c2, c3
                    WHERE st->>'Id' = ?
                    AND ls.in NOT IN (c1.index, c2.index, c3.index)
                    LIMIT 1
                ), fl AS (
                    SELECT jsonb_agg(ls.st) AS newstack
                    FROM ls, c1, c2, c3, c4
                    WHERE ls.in NOT IN (c1.index, c2.index, c3.index, c4.index)
                ), us AS (
                    UPDATE profile
                    SET stack = COALESCE(fl.newStack, '[]'::jsonb)
                    FROM fl
                    WHERE id = ?
                )
                UPDATE profile
                SET deck = '[]'::jsonb || c1.card1 || c2.card2 || c3.card3 || c4.card4,
                stack = COALESCE(fl.newStack, '[]'::jsonb)
                WHERE id = ?
                """;

            try {
                PreparedStatement setup = this.transactionUnit.prepareStatement(setDeckQuery);

                setup.setInt(1, user.getId());
                setup.setString(2, newDeck.get(1).toString());
                setup.setString(3, newDeck.get(2).toString());
                setup.setString(4, newDeck.get(3).toString());
                setup.setString(5, newDeck.get(4).toString());
                setup.setInt(6, user.getId());
                setup.setInt(7, user.getId());

                setup.executeUpdate();
            } catch(SQLException e) {
                throw new DbAccessException(e);
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
