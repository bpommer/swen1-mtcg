package edu.swen1.mtcg.services.packages;


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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PackagesService implements IService {
    @SuppressWarnings("FieldCanBeLocal")
    private final int ID_STRING_SIZE = 36;
    private final int NAME_STRING_MAX = 200;
    private final Set<String> CARD_KEY_WHITELIST = new HashSet<>(Arrays.asList("Id", "Name", "Damage"));
    private final PackageController controller;

    public PackagesService() { this.controller = new PackageController(); }

    @Override
    public Response handleRequest(Request request) {

        if(request.getMethod() == RestMethod.POST) {

            String requestToken  = request.getHeader("Authorization");
            User foundUser = SessionRepository.fetchUserFromToken(requestToken);

            if(foundUser != null && foundUser.getUsername().equals("admin")) {

                JSONArray packArray = null;

                try {
                    packArray = new JSONArray(request.getBody());
                } catch (JSONException e) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request");
                }

                for(int i = 0; i < packArray.length(); i++) {
                    JSONObject cd = packArray.getJSONObject(i);
                    if(!RequestSchemaChecker.JsonKeyValueCheck(cd.toString(), SchemaWhitelists.CARD)) {
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
