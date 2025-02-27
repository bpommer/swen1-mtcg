package edu.swen1.mtcg.services.deck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;

public class DeckService implements IService {

    private final DeckController controller;
    private final int DECK_SIZE = 4;

    public DeckService() { this.controller = new DeckController(); }

    @Override
    public Response handleRequest(Request request) {

        // GET /deck
        if(request.getMethod() == RestMethod.GET) {
            String token = request.getHeaderMap().getAuthHeader();
            HashMap<String, String> params = request.getParams();

            User foundUser = SessionRepository.fetchUserFromToken(token);
            if(foundUser == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Unauthorized");
            } else {
                JSONArray jsonDeck = new JSONArray(foundUser.getDeck());

                if(jsonDeck.isEmpty()) {
                    return new Response(HttpStatus.NO_CONTENT, ContentType.TEXT, HttpStatus.NO_CONTENT.statusMessage);
                } else {
                    // Build string if plain format is specified in params, otherwise return JSON array
                    if(params.containsKey("format") && params.get("format").equals("plain")) {
                        StringBuilder deckString = new StringBuilder().append("\n");
                        for(int i = 0; i < jsonDeck.length(); i++) {
                            JSONObject tempCard = jsonDeck.getJSONObject(i);
                            deckString.append("Id: ").append(tempCard.get("Id")).append("\n");
                            deckString.append("Name: ").append(tempCard.get("Name")).append("\n");
                            deckString.append("Damage: ").append(tempCard.get("Damage")).append("\n");
                            deckString.append("\n");
                        }
                        return new Response(HttpStatus.OK, ContentType.JSON, deckString.toString());
                    } else {
                        return new Response(HttpStatus.OK, ContentType.JSON, jsonDeck.toString());
                    }
                }
            }

        }


        else if(request.getMethod() == RestMethod.PUT) {

            // Check user token
            String requestBody = request.getBody();
            String token = request.getHeaderMap().getAuthHeader();
            User user = null;
            user = fetchUserFromToken(token);

            if(user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT,
                        "Access token is missing or invalid");
            }

            // Check if body is a valid JSON array
            JSONArray requestArray = null;
            try {
                requestArray = new JSONArray(requestBody);
            } catch (JSONException e) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, HttpStatus.BAD_REQUEST.statusMessage);
            }
            // Check if array contains exactly 4 cards
            if(requestArray.length() != DECK_SIZE) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, HttpStatus.BAD_REQUEST.statusMessage);
            }

            // Check if array only consists of strings
            for(int i = 0; i < requestArray.length(); i++) {
                if(!(requestArray.get(i) instanceof String)) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, HttpStatus.BAD_REQUEST.statusMessage);
                }
            }

            return controller.changeDeck(requestArray, user);







        }
        else {
            return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented");
        }

    }
}
