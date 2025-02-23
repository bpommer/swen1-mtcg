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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;

public class DeckService implements IService {

    private final DeckController controller;

    public DeckService() { this.controller = new DeckController(); }

    @Override
    public Response handleRequest(Request request) {

        // GET /deck
        if(request.getMethod() == RestMethod.GET) {
            String token = request.getHeaderMap().getAuthHeader();
            HashMap<String, String> params = request.getParams();

            User user = SessionRepository.fetchUserFromToken(token);
            if(user == null) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
            }
            if(validUserToken(user.getUsername(), token)) {
                try {
                    Response res = controller.fetchDeck(user.getId(), params);
                    return res;
                } catch (Exception e) {
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal Server Error");

                }
            } else {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Unauthorized");
            }

        }


        else if(request.getMethod() == RestMethod.PUT) {

            // Check user token
            String requestBody = request.getBody();
            String token = request.getHeaderMap().getAuthHeader();
            User user = null;
            user = fetchUserFromToken(token);

            if(user == null || !validUserToken(user.getUsername(), token)) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT,
                        "Access token is missing or invalid");
            }

            // Check type of array content
            JSONArray requestArray = new JSONArray(requestBody);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = null;
            try {
                rootNode = mapper.readTree(requestArray.toString());
            } catch (JsonProcessingException e) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
            }
            if(rootNode != null && rootNode.isArray()) {
                for(JsonNode node : rootNode) {
                    if(!node.isTextual()) {
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
                    }
                }
            }

            // Check if exactly 4 cards are submitted
            if(requestArray.length() != 4) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT,
                        "The provided deck did not include the required amount of cards");
            }

            // Fill hashset with card IDs
            HashSet<String> requestSet = new HashSet<String>();
            for(Object o : requestArray) {
                JSONObject obj = (JSONObject) o;
                requestSet.add(((JSONObject) o).getString("Id"));
            }

            // Load cards from user and build new deck
            try {
                JSONArray userStack = new JSONArray(user.getStack());
                JSONArray userDeck = new JSONArray(user.getDeck());
                ArrayList<JSONObject> inDeck = new ArrayList<>();
                ArrayList<JSONObject> fromStack = new ArrayList<>();




                // Search cards that are already in the deck
                for(Object deckCard : userDeck) {
                    JSONObject deckCardJson = (JSONObject) deckCard;
                    String deckCardId = deckCardJson.getString("Id");
                    if(requestSet.contains(deckCardId)) {
                        inDeck.add(new JSONObject(deckCardJson));
                    }
                }
                // Search and remove cards that are in the stack
                if(inDeck.size() != 4) {
                    for(int i = 0; i < userStack.length(); i++) {

                        JSONObject stackCardJson = new JSONObject(userStack.get(i).toString());
                        String stackCardId = stackCardJson.getString("Id");
                        if (requestSet.contains(stackCardId)) {
                            fromStack.add(new JSONObject(stackCardJson));
                            userStack.remove(i);
                        }

                        userStack.put(stackCardJson);
                    }
                } else {
                    return new Response(HttpStatus.OK, ContentType.JSON, "The deck has been successfully configured");
                }

                // Check if all specified cards are owned by the user
                if(inDeck.size() + fromStack.size() != 4) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT,
                            "At least one of the provided cards does not belong to the user or is not available.");
                }

                JSONArray newDeck = new JSONArray();

                // Assemble new deck and update deck and stack
                for(JSONObject deckCardJson : inDeck) {
                    newDeck.put(deckCardJson);
                }
                for(JSONObject stackCardJson : fromStack) {
                    newDeck.put(stackCardJson);
                }

                return controller.changeDeck(newDeck, userStack, user.getId());


            } catch (Exception e) {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error");
            }

        }
        else {
            return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented");
        }

    }
}
