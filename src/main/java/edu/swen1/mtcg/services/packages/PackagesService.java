package edu.swen1.mtcg.services.packages;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.server.IService;

import edu.swen1.mtcg.utils.TokenAuthenticator;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.SQLException;

public class PackagesService implements IService {

    private final PackageController controller;

    public PackagesService() { this.controller = new PackageController(); }

    @Override
    public Response handleRequest(Request request) {

        if(request.getMethod().equals(RestMethod.POST)) {



            String requestToken  = request.getHeaderMap().getAuthHeader();
            if(requestToken != null && TokenAuthenticator.validAdminToken(requestToken)) {

                ObjectMapper mapper = new ObjectMapper();

                try {
                    JsonNode node = mapper.readTree(request.getBody());
                    if(node != null && node.isArray()) {
                        JSONArray newEntry = new JSONArray(node.asText());
                        return controller.addPackage(newEntry);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Error processing request");
                }

            } else {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Access token is missing or invalid");
            }


        }



        return new Response(HttpStatus.BAD_REQUEST, ContentType.TEXT, "Error: Bad request");

    }
}
