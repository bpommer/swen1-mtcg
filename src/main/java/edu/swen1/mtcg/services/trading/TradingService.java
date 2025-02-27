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

import edu.swen1.mtcg.utils.RequestSchemaChecker;
import edu.swen1.mtcg.utils.SchemaWhitelists;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;

public class TradingService implements IService {
    private final Set<String> TYPE_WHITELIST = new HashSet<>(Arrays.asList("monster", "spell"));
    private final String ID_PATTERN_STRING = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

    @Override
    public Response handleRequest(Request request) {

        String token = request.getHeader("Authorization");
        HashMap<String, String> params = request.getParams();
        User foundUser = fetchUserFromToken(token);
        TradingController controller = new TradingController();

        if (foundUser == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token missing or invalid");
        }

        if(request.getMethod() == RestMethod.GET) {

            return controller.getTrades();
        }



        else if(request.getMethod() == RestMethod.POST) {




            if(request.getPathParts().size() == 1) {

                // Check if JSON is well-formed and keys contain proper value types
                if(!RequestSchemaChecker.JsonKeyValueCheck(request.getBody(), SchemaWhitelists.TRADEDEAL)) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
                }

                JSONObject dealJson = new JSONObject(request.getBody());

                // Check for invalid data in JSON
                if(dealJson.getFloat("MinimumDamage") < 0
                || !TYPE_WHITELIST.contains(dealJson.getString("Type"))
                || !dealJson.getString("CardToTrade").matches(ID_PATTERN_STRING)) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
                }

                TradingDeal newDeal = new TradingDeal(
                        dealJson.getString("Id"),
                        dealJson.getString("CardToTrade"),
                        dealJson.getString("Type"),
                        dealJson.getFloat("MinimumDamage"));

                return controller.newTradeOffer(foundUser, newDeal);

            }
            else if(request.getPathParts().size() == 2) {
                return controller.makeTrade(foundUser, request.getBody(), request.getPathParts().get(1));
            }


        }
        else if(request.getMethod() == RestMethod.DELETE) {
            if(request.getPathParts().size() != 2) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
            } else {
                return controller.revokeTradeOffer(foundUser, request.getPathParts().get(1));
            }

        } else {
            return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, HttpStatus.NOT_IMPLEMENTED.statusMessage);
        }
        return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, HttpStatus.NOT_IMPLEMENTED.statusMessage);

    }
}
