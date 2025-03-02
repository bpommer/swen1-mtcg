package edu.swen1.mtcg.services.transactions;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;

import java.util.List;

public class TransactionsService implements IService {
    private final TransactionsController controller;

    public TransactionsService() { this.controller = new TransactionsController(); }

    @Override
    public Response handleRequest(Request request) {

        List<String> pathParts = request.getPathParts();

        if(request.getMethod() == RestMethod.POST
                && pathParts.size() == 2 && pathParts.get(1).equals("packages")) {

            String token = request.getHeaderMap().getAuthHeader();
            User user = SessionRepository.fetchUserFromToken(token);

            if(user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token missing or invalid.\n");
            } else {
                if(user.getCoins() < 5) {
                    return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT, "Not enough money for buying a card package.\n");
                }
                return controller.purchasePack(user);
            }


        } else {
            return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, HttpStatus.NOT_IMPLEMENTED.statusMessage);
        }


    }
}
