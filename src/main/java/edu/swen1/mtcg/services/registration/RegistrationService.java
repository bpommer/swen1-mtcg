package edu.swen1.mtcg.services.registration;

import com.ctc.wstx.shaded.msv_core.verifier.regexp.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.TokenAuthenticator;
import org.json.JSONObject;


import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RegistrationService implements IService {
    private final RegistrationController controller;

    public RegistrationService() { this.controller = new RegistrationController(); }

    @Override
    public Response handleRequest(Request request) {

        SessionRepository process = new SessionRepository(new TransactionUnit());

        /*
        POST /users
        summary: Register a new user
        description: Register a new user with username and password
        */


        if (request.getMethod() == RestMethod.POST) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode node = mapper.readTree(request.getBody());

                String username = node.get("Username").asText();
                String password = node.get("Password").asText();

                if(!checkWhitelist(node)) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
                }
                return controller.register(username, password);

            } catch (Exception e) {
                e.printStackTrace();
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
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
            String username = null;

            if(path.size() == 2 && authToken != null) {

                username = path.get(1);
                if(TokenAuthenticator.validUserToken(username, authToken)
                        || TokenAuthenticator.validAdminToken(authToken)) {
                    return controller.getUser(username);
                }

                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Authentication token required");

            }
            else if (path.size() == 2 && authToken == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Authentication token required");
            }
            else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Invalid request");
            }

        } else if (request.getMethod() == RestMethod.PUT) {

            List<String> path = request.getPathParts();
            String authToken = request.getHeaderMap().getAuthHeader();
            String username = null;

            if(path.size() == 2 && authToken != null) {

                username = path.get(1);
                if(TokenAuthenticator.validUserToken(username, authToken)
                        || TokenAuthenticator.validAdminToken(authToken)) {
                    return controller.updateUser(username, new JSONObject(request.getBody()));
                }

                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid");

            }
            else if (path.size() == 2 && authToken == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid");
            }
            else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Invalid request");
            }

        }
        return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "Service not found");

    }

    private boolean checkWhitelist(JsonNode node) {
        HashSet<String> whitelist = new HashSet<>();
        whitelist.add("Username");
        whitelist.add("Password");

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while(fields.hasNext()) {
            if(!whitelist.contains(fields.next().getKey())) {
                return false;
            }
        }
        return true;
    }

}







