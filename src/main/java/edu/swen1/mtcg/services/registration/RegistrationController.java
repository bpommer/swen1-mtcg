package edu.swen1.mtcg.services.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.utils.Controller;
import edu.swen1.mtcg.utils.TokenAuthenticator;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class RegistrationController extends Controller {

    // POST /users
    public Response register(String username, String password) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {

            Response res = new SessionRepository(transactionUnit).registerUser(username, password);
            transactionUnit.dbCommit();
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            transactionUnit.dbRollback();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"Internal Server Error\" }"
            );

        }

    }
    // GET /users/{username}
    public Response getUser(String username) {
        try {
            User res = SessionRepository.fetchUserFromName(username);
            if(res != null) {
                return new Response(HttpStatus.OK, ContentType.JSON, res.getUserData().toString());
            } else {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"error\": \"User not found\" }");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"Internal Server Error\" }"
            );
        }

    }

    // PUT /users/{username}
    public Response updateUser(String username, JSONObject newdata) {

        if(UserDataWhitelist(newdata)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = null;
            try {
                node = mapper.readTree(newdata.toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal Server Error");
            }

            // Check value types
            if(node.get("Name").isTextual() && node.get("Bio").isTextual()
                    && node.get("Image").isTextual()) {
                TransactionUnit transactionUnit = new TransactionUnit();

                try(transactionUnit) {

                    Response res = new SessionRepository(transactionUnit).updateUser(node.get("Name").asText(),
                            node.get("Bio").asText(), node.get("Image").asText());
                    transactionUnit.dbCommit();

                    return res;

                } catch (Exception e) {
                    e.printStackTrace();
                    transactionUnit.dbRollback();
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal Server Error");

                }


            } else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
            }
        } else {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
        }

    }

    public boolean UserDataWhitelist(JSONObject data) {
        Set<String> whitelist = new HashSet<>(Arrays.asList("Name", "Bio", "Image"));
        Iterator<String> keys = data.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            if(!whitelist.contains(key)) {
                return false;
            }
        }
        return true;

    }





}
