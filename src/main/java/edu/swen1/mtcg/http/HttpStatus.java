package edu.swen1.mtcg.http;


// Define HTTP response codes
public enum HttpStatus {
    OK(200, "OK"),
    CREATED(201, "Resource created"),
    ACCEPTED(202, "Request accepted"),
    NO_CONTENT(204, "No content"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    CONFLICT(409, "Confilct"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    NOT_IMPLEMENTED(501, "Not implemented");

    public final int statusCode;
    public final String statusMessage;

    HttpStatus(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;

    }
}
