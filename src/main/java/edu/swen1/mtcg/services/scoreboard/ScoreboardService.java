package edu.swen1.mtcg.services.scoreboard;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import org.json.JSONArray;

public class ScoreboardService implements IService {

    @Override
    public Response handleRequest(Request request) {

        if(request.getMethod() == RestMethod.GET) {
            String authToken = request.getHeaderMap().getAuthHeader();

            User foundUser = null;
            foundUser = SessionRepository.fetchUserFromToken(authToken);

            if(foundUser == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token missing or invalid");
            }

            JSONArray board = SessionRepository.fetchAllUserStats();

            if(board == null || board.length() == 0) {
                return new Response(HttpStatus.OK, ContentType.JSON, new JSONArray().toString());
            }
            return new Response(HttpStatus.OK, ContentType.JSON, board.toString());



        } else {
            return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented");
        }

    }

}
