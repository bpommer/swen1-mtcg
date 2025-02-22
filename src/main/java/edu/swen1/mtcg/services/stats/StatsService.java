package edu.swen1.mtcg.services.stats;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.TokenAuthenticator;
import org.json.JSONObject;

public class StatsService implements IService {
    @Override
    public Response handleRequest(Request request) {

        if(request.getMethod() == RestMethod.GET) {
            String authToken = request.getHeaderMap().getAuthHeader();

            String[] split = authToken.split(" ", 2);


            User foundUser = null;
            foundUser = SessionRepository.fetchUserFromToken(split[1]);

            if(foundUser == null || !TokenAuthenticator.validUserToken(foundUser.getUsername(), authToken)) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token missing or invalid");
            }

            JSONObject statsJson = foundUser.getUserStats();

            return new Response(HttpStatus.OK, ContentType.JSON, statsJson.toString());
        } else {
            return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented");
        }

    }
}
