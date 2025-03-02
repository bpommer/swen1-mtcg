package edu.swen1.mtcg.services.packages;


import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.server.IService;

import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.HashGenerator;
import edu.swen1.mtcg.utils.RequestSchemaChecker;
import edu.swen1.mtcg.utils.SchemaWhitelists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PackagesService implements IService {

    private final String ID_PATTERN_STRING = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";
    private final PackageController controller;
    private final int PACKAGE_SIZE = 5;

    public PackagesService() { this.controller = new PackageController(); }

    @Override
    public Response handleRequest(Request request) {

        if(request.getMethod() == RestMethod.POST) {

            String requestToken  = request.getHeader("Authorization");
            User foundUser = SessionRepository.fetchUserFromToken(requestToken);

            if(foundUser != null && foundUser.getUsername().equals("admin")) {

                // Load body into JSONArray and check if body is an array
                JSONArray packArray = null;
                try {
                    packArray = new JSONArray(request.getBody());
                } catch (JSONException e) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request");
                }

                // Check if array contains exactly 5 elements
                if(packArray.length() != PACKAGE_SIZE) { return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request"); }


                // Card format validation logic
                for(int i = 0; i < packArray.length(); i++) {
                    JSONObject cd = null;

                    // Check if array element is JSON
                    try {
                        cd = packArray.getJSONObject(i);
                    } catch (JSONException e) {
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request");
                    }

                    // Check JSON format
                    if(!RequestSchemaChecker.JsonKeyValueCheck(cd.toString(), SchemaWhitelists.CARD)) {
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request");
                    }

                    // Check if ID matches regex pattern and if damage is not negative
                    if(!cd.getString("Id").matches(ID_PATTERN_STRING)
                    || cd.getFloat("Damage") < 0) {
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request");
                    }

                }


                return controller.addPackage(packArray);
            }
            else if(foundUser != null) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT, "Provided user is not admin");
            }
            else {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid");
            }
        }
        else {
            return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented");
        }
    }
}
