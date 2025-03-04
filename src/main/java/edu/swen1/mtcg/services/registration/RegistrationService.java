package edu.swen1.mtcg.services.registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.RequestSchemaChecker;
import edu.swen1.mtcg.utils.SchemaWhitelists;
import org.json.JSONObject;


import java.util.*;

public class RegistrationService implements IService {
    private final RegistrationController controller;

    public RegistrationService() { this.controller = new RegistrationController(); }

    @Override
    public Response handleRequest(Request request) {

        /*
        POST /users
        summary: Register a new user
        description: Register a new user with username and password
        */


        if (request.getMethod() == RestMethod.POST) {
            if(!RequestSchemaChecker.JsonKeyValueCheck(request.getBody(), SchemaWhitelists.USER_CREDENTIALS)) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request.\n");
            }

            try {

                JSONObject credentials = new JSONObject(request.getBody());
                String username = credentials.getString("Username");
                String password = credentials.getString("Password");

                if(SessionRepository.fetchUserFromName(username) != null) {
                    return new Response(HttpStatus.CONFLICT, ContentType.TEXT, "User already exists\n");
                }


                return controller.register(username, password);

            } catch (Exception e) {
                e.printStackTrace();
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, HttpStatus.BAD_REQUEST.statusMessage);
            }
        }

        /*
        GET /users/{username}
        summary: Retrieves the user data for the given username.
        description: Retrieves the user data for the username provided in the route.
        Only the admin or the matching user can successfully retrieve the data.
        */

        else if (request.getMethod() == RestMethod.GET) {

            List<String> path = request.getPathParts();
            String authToken = request.getHeaderMap().getAuthHeader();

            if(path.size() == 2 && authToken != null) {

                String targetUser = path.get(1);
                User foundUser = SessionRepository.fetchUserFromToken(authToken);

                if(foundUser != null &&
                        (foundUser.getUsername().equals(targetUser) || foundUser.getUsername().equals("admin"))
                ) {
                    return controller.getUser(targetUser);
                } else {
                    return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Authentication token required.\n");
                }

            }
            else if (path.size() == 2) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Authentication token required.\n");
            }
            else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Invalid request.\n");
            }

        } else if (request.getMethod() == RestMethod.PUT) {

            List<String> path = request.getPathParts();
            String authToken = request.getHeaderMap().getAuthHeader();

            User foundUser = SessionRepository.fetchUserFromToken(authToken);


            if(path.size() == 2 && foundUser != null) {

                String targetUser = path.get(1);

                if((foundUser.getUsername().equals(targetUser)
                        || foundUser.getUsername().equals("admin"))
                    && RequestSchemaChecker.JsonKeyValueCheck(request.getBody(), SchemaWhitelists.USER_DATA)) {

                    JSONObject data = new JSONObject(request.getBody());


                    return controller.updateUser(targetUser, data.getString("Name"),
                            data.getString("Bio"), data.getString("Image"));
                } else {
                    return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid\n");
                }
            }
            else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Invalid request.\n");
            }


        }
        return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented.\n");

    }

}







