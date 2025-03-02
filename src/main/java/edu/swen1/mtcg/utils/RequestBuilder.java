package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;


public class RequestBuilder {
    public Request buildRequest(BufferedReader reader) throws IOException {
        Request request = new Request();
        String line = reader.readLine();

        if(line != null) {
            String[] parts = line.split(" ");

            String methodString = parts[0];

            try {
                request.setMethod(getMethod(parts[0]));

            } catch(IllegalArgumentException iae) {
                throw new IOException("Illegal request method: " + methodString);
            }

            System.out.println(parts[1]);
            setPath(request, parts[1]);

            line = reader.readLine();

            while(!line.isEmpty()) {
                request.getHeaderMap().splitLine(line);
                line = reader.readLine();
            }

            if(request.getHeaderMap().getContentLength() > 0) {

                char[] buffer = new char[request.getHeaderMap().getContentLength()];
                reader.read(buffer, 0, request.getHeaderMap().getContentLength());
                request.setBody(new String(buffer));

            }

            // System.out.println("RequestBuilder: " + request.getMethod() + " " + request.getPath() + " " + request.getBody());
        }
        return request;
    }

    private RestMethod getMethod(String methodString) throws IllegalArgumentException {
        return RestMethod.valueOf(methodString.toUpperCase(Locale.ROOT));
    }

    private void setPath(Request request, String path) {
        boolean hasParams = path.indexOf("?") != -1;
        if(hasParams) {
            String[] parts = path.split("\\?");
            request.setPath(parts[0]);
            request.setParams(parts[1]);
        }

        else {
            request.setPath(path);
            request.setParams(null);
         }

    }

}
