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
import edu.swen1.mtcg.services.db.models.BattleCard;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import edu.swen1.mtcg.services.db.models.User;

import edu.swen1.mtcg.services.db.repository.TradeRepository;
import edu.swen1.mtcg.utils.BattleCardFactory;
import edu.swen1.mtcg.utils.RequestSchemaChecker;
import edu.swen1.mtcg.utils.SchemaWhitelists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;

public class TradingService implements IService {
    private final String ID_PATTERN_STRING = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";
    private final TradingController controller = new TradingController();



    @Override
    public Response handleRequest(Request request) {

        String token = request.getHeader("Authorization");
        HashMap<String, String> params = request.getParams();
        User foundUser = fetchUserFromToken(token);

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
                || !dealJson.getString("CardToTrade").matches(ID_PATTERN_STRING)) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
                }

                TradingDeal newDeal = new TradingDeal(
                        dealJson.getString("Id"),
                        dealJson.getString("CardToTrade"),
                        dealJson.getString("Type"),
                        dealJson.getFloat("MinimumDamage"));

                newDeal.setOwnerId(foundUser.getId());

                System.out.println("Id: " + newDeal.getTradeid());
                System.out.println("CardToTrade: " + newDeal.getCardid());
                System.out.println("Type: " + newDeal.getType());
                System.out.println("MinimumDamage: " + newDeal.getMindamage());


                return controller.newTradeOffer(foundUser, newDeal);

            }
            else if(request.getPathParts().size() == 2) {

                // Check if trade exists
                TradingDeal deal = TradeRepository.getTradingDeal(request.getPathParts().get(1));
                if(deal == null) {
                    return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "The provided deal ID was not found.\n");
                }

                // Check for trade with self
                if(deal.getOwnerId().equals(foundUser.getId())) {
                    return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT,
                            "The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck, or the user tries to trade with self\n");

                }


                String cardId = null;
                try {
                    cardId = (String) new JSONTokener(request.getBody()).nextValue();
                } catch (Exception e) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
                }

                // Fetch stack from user and query for matching card
                JSONArray userStackArray = foundUser.getStack();
                boolean cardFound = false;
                JSONObject offeredCard = null;


                // Check if user owns card and if it matches requirements
                for (int i = 0; i < userStackArray.length(); i++) {

                    JSONObject tempObj = userStackArray.getJSONObject(i);
                    String tempId = tempObj.getString("Id");

                    if(tempId.equals(cardId)) {
                        // Use BattleCardFactory to infer card type
                        BattleCard tempCard = new BattleCardFactory().buildBattleCard(tempObj);
                        if(tempCard != null && tempCard.getDamage() >= deal.getMindamage()
                        && tempCard.getProperties().get("Type").equalsIgnoreCase(deal.getType())) {
                            offeredCard = new JSONObject(tempObj.toString());
                            userStackArray.remove(i);
                            cardFound = true;
                            break;
                        }
                    }

                }

                if(cardFound) {
                    foundUser.setStack(userStackArray.toString());
                    return controller.makeTrade(foundUser, offeredCard, deal.getTradeid());
                } else {
                    return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT,
                            "The offered card is not owned by the user, or the requirements are not met (Type, MinimumDamage), or the offered card is locked in the deck, or the user tries to trade with self\n");

                }

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
