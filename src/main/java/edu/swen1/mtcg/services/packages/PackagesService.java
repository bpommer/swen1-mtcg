package edu.swen1.mtcg.services.packages;


import com.ctc.wstx.shaded.msv_core.verifier.regexp.Token;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.server.IService;
import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;

import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.utils.TokenAuthenticator;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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

        System.out.println("Token: " + request.getHeader("Authorization"));

        if(request.getMethod() == RestMethod.POST) {
            System.out.println("POST");
            String requestToken  = request.getHeader("Authorization");

            System.out.println("TokenAuthenticator: " + TokenAuthenticator.validAdminToken(requestToken));
            System.out.println(requestToken);
            if(requestToken != null && TokenAuthenticator.validAdminToken(requestToken)) {
                System.out.println("Admin token validated");


                // Validate key and values
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode node = mapper.readTree(request.getBody());
                    if(node != null && node.isArray() && node.size() == 5) {
                        JSONArray packArray = new JSONArray();



                        for(JsonNode jsonNode : node) {

                            // Read all keys into hashset and compare to whitelist
                            Set<String> objKeys = new HashSet<>();
                            Iterator<String> objFields = node.fieldNames();
                            while(objFields.hasNext()) {
                                objKeys.add(objFields.next());
                            }

                            // Check value types of keys
                            if(jsonNode.get("Id").isTextual()
                                    && jsonNode.get("Name").isTextual()
                            && jsonNode.get("Damage").isNumber()
                            && objKeys.equals(CARD_KEY_WHITELIST)) {

                                String tempId = jsonNode.get("Id").toString();
                                String tempName = jsonNode.get("Name").toString();
                                float tempDamage = Float.parseFloat(jsonNode.get("Damage").asText());

                                // Check for correct format
                                if(tempId.length() != ID_STRING_SIZE
                                        || tempName.length() > NAME_STRING_MAX
                                || tempDamage <= 0) {
                                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
                                } else {

                                }
                            } else {
                                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
                            }
                        }




                        return controller.addPackage(newEntry);
                    }
                    else {
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad request");
                }

            }
            if((requestToken != null)) {
                System.out.println("else if");
                try {
                    String[] splitToken = requestToken.split(" ", 2);

                    User user = fetchUserFromToken(splitToken[1]);
                    if(user != null && TokenAuthenticator.validUserToken(user.getUsername(), requestToken)) {
                        System.out.println("User next");
                        return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT, "Provided user is not admin");

                    } else {
                        return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid");
                    }
                } catch (Exception e) {
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Error processing request");
                }
            }
            else {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid");
            }
        }
        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request");
    }
}
