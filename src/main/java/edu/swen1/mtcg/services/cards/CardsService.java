package edu.swen1.mtcg.services.cards;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.BattleCard;
import edu.swen1.mtcg.services.db.models.Card;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.CardDataRepository;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.BattleCardFactory;
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
            User foundUser = SessionRepository.fetchUserFromToken(authToken);

            if (foundUser == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token missing or invalid.\n");
            }

            if(path.size() == 1) {
                JSONArray userCards = foundUser.getAllCards();

                if (userCards.isEmpty()) {
                    return new Response(HttpStatus.NO_CONTENT, ContentType.TEXT, "The user has no cards.\n");
                } else {
                    return new Response(HttpStatus.OK, ContentType.JSON, userCards.toString());
                }


            } else if (path.size() == 2) {

                Card foundCard = CardDataRepository.getCardData(path.get(1));
                if (foundCard == null) {
                    return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "Card not found.\n");
                } else {

                    BattleCard newCard = BattleCardFactory.buildBattleCard(foundCard.toJSON());
                    return new Response(HttpStatus.OK, ContentType.JSON, newCard.cardExtendedInfo().toString());
                }


            }

        }

        return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented\n");
    }

}
