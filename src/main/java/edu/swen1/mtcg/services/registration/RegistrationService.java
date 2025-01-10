package edu.swen1.mtcg.services.registration;

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

public class RegistrationService implements IService {
    private final RegistrationController controller;

    public RegistrationService() { this.controller = new RegistrationController(); }

    @Override
    public Response handleRequest(Request request) {
        System.out.println("Registration service called");

        SessionRepository process = new SessionRepository(new TransactionUnit());


        if (request.getMethod() == RestMethod.POST) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode node = mapper.readTree(request.getBody());

                String username = node.get("Username").asText();
                String password = node.get("Password").asText();

                return controller.register(username, password);

            } catch (IOException e) {
                e.printStackTrace();
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Error processing request");
            }
        }

        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request");

        // Response res = new Response(HttpStatus.OK, ContentType.JSON, resJson);
        //System.out.println("Registration response constructed.\n");
    }
}







