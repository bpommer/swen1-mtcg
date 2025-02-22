package edu.swen1.mtcg.services.cards;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.Card;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.CardDataRepository;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import org.json.JSONArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CardsService implements IService {



    @Override
    public Response handleRequest(Request request) {

        if(request.getMethod() == RestMethod.GET) {
            List<String> path = request.getPathParts();
            String authToken = request.getHeaderMap().getAuthHeader();
            String username = null;


            if(path.size() == 1 && authToken != null) {
                try {
                    User userdata = SessionRepository.fetchUserFromToken(authToken);
                    if(userdata != null) {
                        String stack = userdata.getStack().toString();
                        JSONArray stackArray = new JSONArray(stack);
                        if(stackArray.isEmpty()) {
                            return new Response(HttpStatus.NO_CONTENT, ContentType.TEXT, "No cards in stack");
                        }
                        return new Response(HttpStatus.OK, ContentType.JSON, stackArray.toString());
                    } else {
                        return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR,
                            ContentType.TEXT, "Internal server error");
                }
            }
            else if(path.size() == 1) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid");
            }
            else if(path.size() == 2) {
                System.out.println("Endpoint: " + path.get(0) + " " + path.get(1));
                Card card = CardDataRepository.getCardData(path.get(1));

                if(card == null) { return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "Card not found"); }

                return new Response(HttpStatus.OK, ContentType.JSON, card.toString());
            }
        }
        return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented");
    }

}
