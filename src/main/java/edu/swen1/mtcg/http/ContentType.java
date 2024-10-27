package edu.swen1.mtcg.http;


// Define MIME types and implement constructor
public enum ContentType {
    TEXT("text/plain"),
    HTML("text/html"),
    JSON("application/json");

    public final String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }
}
