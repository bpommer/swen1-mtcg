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
import edu.swen1.mtcg.services.db.repository.SessionRepository;

import java.io.IOException;


public class LoginService implements IService {
    private final LoginController controller;
    public LoginService() { this.controller = new LoginController(); }


    @Override
    public Response handleRequest(Request request) {

        String username;
        String password;
        if(request.getMethod() == RestMethod.POST) {

            SessionRepository process = new SessionRepository(new TransactionUnit());
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode node = mapper.readTree(request.getBody());

                username = node.get("Username").asText();
                password = node.get("Password").asText();

            } catch (IOException e) {
                e.printStackTrace();
                return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Bad Request");
            }

            try {
                return process.fetchUser(username, password);
            } catch (Exception e) {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Error processing request");
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
