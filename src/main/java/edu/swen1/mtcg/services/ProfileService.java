package edu.swen1.mtcg.services;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.server.IService;


public class ProfileService implements IService {
    @Override
    public Response handleRequest(Request request) {
        return new Response(HttpStatus.OK, ContentType.TEXT,
                "ProfileService accessed. " + request.getBody());

    }

}
