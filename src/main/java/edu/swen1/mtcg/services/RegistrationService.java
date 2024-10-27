package edu.swen1.mtcg.services;

// import com.fasterxml.jackson.databind.util.JSONPObject;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.server.IService;
import org.json.JSONObject;

public class RegistrationService implements IService {

    @Override
    public Response handleRequest(Request request) {
        System.out.println("Registration service called");

        String resJson = "{ \"response\": true }";


        Response res = new Response(HttpStatus.OK, ContentType.JSON, resJson);

        System.out.println("Registration response constructed.\n");

        return res;

        }


}




