package edu.swen1.mtcg.services.trading;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import edu.swen1.mtcg.services.db.models.User;

import edu.swen1.mtcg.utils.TokenAuthenticator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;

public class TradingService implements IService {
    private final int ID_STRING_SIZE = 36;
    private final Set<String> KEX_WHITELIST = new HashSet<>
            (Arrays.asList("Id", "CardToTrade", "Type", "MinimumDamage"));
    private final Set<String> TYPE_WHITELIST = new HashSet<>(Arrays.asList("monster", "spell"));

    @Override
    public Response handleRequest(Request request) {
        if(request.getMethod() == RestMethod.GET) {
            String token = request.getHeader("Authorization");
            String params = request.getParams();



            String[] split = request.getHeader("Authorization").split(" ", 2);

            if(token.length() == 0 || split[1] == null || split[1].length() == 0) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token missing or invalid");
            }


            User checkUser = fetchUserFromToken(split[1]);

            if(checkUser != null && TokenAuthenticator.validUserToken(checkUser.getUsername(), token)) {
                TradingController controller = new TradingController();
                Response res = controller.getTradeListings();
                return res;

            } else {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token missing or invalid");

            }
        }
        else if(request.getMethod() == RestMethod.POST) {
            String token = request.getHeader("Authorization");
            HashMap<String, String> params = request.getParams();
            JSONArray userStack = null;
            String[] split = request.getHeader("Authorization").split(" ", 2);
            User checkUser = fetchUserFromToken(split[1]);

            if(token.length() == 0 || split[1] == null || split[1].length() == 0 || checkUser == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token missing or invalid");
            }


            TradingController controller = new TradingController();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = null;
            TradingDeal newDeal = null;
            JSONObject dealJson = new JSONObject(request.getBody());

            // Check for required keys and valid value types
            try {
                node = mapper.readTree(request.getBody());

                // Check value types
                if (node.get("Id").isTextual() && node.get("CardToTrade").isTextual()
                        && node.get("Type").isTextual() && node.get("MinimumDamage").isNumber()
                        && Float.parseFloat(node.get("MinimumDamage").toString()) > 0) {

                    newDeal = new TradingDeal(dealJson.get("Id").toString(), dealJson.get("CardToTrade").toString(),
                            dealJson.get("Type").toString(), Float.parseFloat(dealJson.get("MinimumDamage").toString()));
                    Set<String> jsonKeys = newDeal.toJSON().keySet();

                    // Whitelist checks and ID size
                    if (!KEX_WHITELIST.containsAll(jsonKeys)
                            || node.get("Id").toString().length() != ID_STRING_SIZE
                            || !TYPE_WHITELIST.contains(newDeal.getType())) {
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
                    }
                } else {
                    System.out.println("Types failed");
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
                }

            } catch(JsonProcessingException e){
                System.out.println("JSON exception");
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
            }


            return controller.newTradeOffer(checkUser, newDeal);


        }
        else if(request.getMethod() == RestMethod.DELETE) {

            if(request.getPathParts().size() != 2) {



            }

        }



        return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, HttpStatus.BAD_REQUEST.statusMessage);
    }
}
