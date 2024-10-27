package edu.swen1.mtcg.server;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Response {
    private int statusCode;
    private String statusMessage;
    private String contentType;
    private String body;

    public Response(HttpStatus status, ContentType contentType, String body) {

        this.statusCode = status.statusCode;
        this.statusMessage = status.statusMessage;
        this.contentType = contentType.mimeType;
        this.body = body;

    }



    // Build response with StringBuilder for better performance
    // Use lineSeparator for platform independence
    public String getMessage() {

        String localDatetime = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("UTC")));
        StringBuilder res = new StringBuilder();

        res.append("HTTP/1.1 ").append(this.statusCode).append(" ")
                .append(this.statusMessage).append("\r").append(System.lineSeparator())
        .append("Cache-Control: max-age=0\r").append(System.lineSeparator())
        .append("Connection: close\r").append(System.lineSeparator())
        .append("Date: ")
                .append(localDatetime).append("\r").append(System.lineSeparator())
        .append("Expires:")
                .append(localDatetime).append("\r").append(System.lineSeparator())
        .append("Content-Type: ")
                .append(this.contentType).append("\r").append(System.lineSeparator())
        .append("Content-Length: ")
                .append(this.body.length()).append("\r").append(System.lineSeparator())
                .append("\r").append(System.lineSeparator()).append(this.body);

        System.out.println("Response: " + res);

        return res.toString();
    }

}
