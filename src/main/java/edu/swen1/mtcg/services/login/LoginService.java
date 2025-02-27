package edu.swen1.mtcg.services.login;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.HashGenerator;
import edu.swen1.mtcg.utils.RequestSchemaChecker;
import edu.swen1.mtcg.utils.SchemaWhitelists;
import org.json.JSONObject;

import java.io.IOException;


public class LoginService implements IService {
    private final LoginController controller;
    public LoginService() { this.controller = new LoginController(); }


    @Override
    public Response handleRequest(Request request) {


        if(request.getMethod() == RestMethod.POST) {

            // Check if body has proper formatting
            if(!RequestSchemaChecker.JsonKeyValueCheck(
                    request.getBody(), SchemaWhitelists.USER_CREDENTIALS)) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");

            }

            JSONObject credentials = new JSONObject(request.getBody());

            String username = credentials.getString("Username");
            String password = credentials.getString("Password");
            User foundUser = SessionRepository.fetchUserFromName(username);

            // Check if username exists
            if(foundUser == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Invalid username/password provided");
            }

            // Check for correct password
            String passwordHashed = HashGenerator.generateHash(password + foundUser.getSalt());
            if(passwordHashed == null || !passwordHashed.equals(foundUser.getPassword())) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Invalid username/password provided");
            } else {
                return controller.login(username);
            }





        }

        else if(request.getMethod() == RestMethod.GET) {

            return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "Unauthorized");


        }

        else {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Unauthorized");
        }


        //return new Response(HttpStatus.OK, ContentType.TEXT,
        //        "ProfileService accessed. " + request.getBody());

    }

}
