package edu.swen1.mtcg.utils;


import edu.swen1.mtcg.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.io.*;


public class RequestBuilderTest {

    private RequestBuilder requestBuilder;

    private BufferedReader testReader;

    InputStream inputStream;

    // Request templates

    // Properly formatted request
    String testRequest1 = new StringBuilder()
            .append("GET /testEndpoint?params=testparam HTTP/1.1\r\n")
            .append("Content-Type: text/plain\r\n")
            .append("Content-Length: 14\r\n")
            .append("Authorization: testToken\r\n")
            .append("\r\n")
            .append("TestBodyAsText")
            .toString();


    // Invalid RestMethods
    String testRequestInvalidMethod1 = new StringBuilder()
            .append("AAA /testEndpoint HTTP/1.1\r\n")
            .append("Content-Type: text/plain\r\n")
            .append("Content-Length: 14\r\n")
            .append("Authorization: testToken\r\n")
            .append("\r\n")
            .append("TestBodyAsText")
            .toString();

    String testRequestInvalidMethod2 = new StringBuilder()
            .append(" /testEndpoint HTTP/1.1\r\n")
            .append("Content-Type: text/plain\r\n")
            .append("Content-Length: 14\r\n")
            .append("Authorization: testToken\r\n")
            .append("\r\n")
            .append("TestBodyAsText")
            .toString();



    @BeforeEach
    public void setup() {
        requestBuilder = new RequestBuilder();

    }

    // Test build with properly formatted request
    @Test
    @DisplayName("Test build with properly formatted string")
    public void testBuild() {
        testReader = new BufferedReader(new StringReader(testRequest1));
        Request res = null;

        try {
            res = requestBuilder.buildRequest(testReader);
        } catch (Exception e) {
            fail();
        }

        // Check if request getters return proper values
        assertEquals("GET", res.getMethod().toString());
        assertEquals("/testEndpoint", res.getRoute());
        assertEquals("TestBodyAsText", res.getBody());

        // Check HeaderMap
        assertEquals("text/plain", res.getHeader("Content-Type"));
        assertEquals("14", res.getHeader("Content-Length"));
        assertEquals("testToken", res.getHeader("Authorization"));
        assertEquals("params=testparam", res.getParams());
    }


    @Test
    public void invalidMethod() {

        // Test with invalid RestMethod
        testReader = new BufferedReader(new StringReader(testRequestInvalidMethod1));
        Exception exc = assertThrows(IOException.class, () -> {
            Request res = requestBuilder.buildRequest(testReader);
        });
        assertEquals("Illegal request method: AAA", exc.getMessage());

        // Test with no set RestMethod
        testReader = new BufferedReader(new StringReader(testRequestInvalidMethod2));
        exc = assertThrows(IOException.class, () -> {
            Request res = requestBuilder.buildRequest(testReader);
        });

        assertEquals("Illegal request method: ", exc.getMessage());




    }




}
