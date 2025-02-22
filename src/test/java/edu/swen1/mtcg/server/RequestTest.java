package edu.swen1.mtcg.server;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class RequestTest {

    public Request testRequest;


    @BeforeEach
    public void setUp() {
        testRequest = new Request();
    }


    @Test
    @DisplayName("Request object path setter test")
    public void testPathSetter() {

        // Positive test
        String testPath1 = "/path/test";
        testRequest.setPath(testPath1);

        assertEquals(testPath1, testRequest.getPath());
        assertEquals("path", testRequest.getPathParts().get(0));
        assertEquals("test", testRequest.getPathParts().get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> {
            String t = testRequest.getPathParts().get(2);
        });



        testRequest = new Request();

        // Test if empty lines get excluded
        String testPath2 = "/path//////test";
        testRequest.setPath(testPath2);

        assertEquals(testPath2, testRequest.getPath());
        assertEquals("path", testRequest.getPathParts().get(0));
        assertEquals("test", testRequest.getPathParts().get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> {
            String t = testRequest.getPathParts().get(2);
        });


    }

    @Test
    @DisplayName("Request object URL setter test")
    public void testUrlSetter() {

        // Without params
        String testUrl1 = "/path/test";
        testRequest.setUrl(testUrl1);
        assertEquals(testUrl1, testRequest.getUrl());
        assertNull(testRequest.getParams());



        // With params
        String testUrl2 = "/path/test?param=value";
        testRequest.setUrl(testUrl2);
        assertEquals(testUrl2, testRequest.getUrl());
        assertEquals("value", testRequest.getParams().get("param"));

        // Check proper parameter split from URL
        String testUrl3 = "/path/test???param=value";
        testRequest.setUrl(testUrl3);
        assertEquals(testUrl3, testRequest.getUrl());
        assertEquals("/path/test", testRequest.getPath());
        assertEquals("value", testRequest.getParams().get("??param"));

        // Check assignment of multiple params
        String testUrl4 = "/path/test?sort=-damage&name=WaterGoblin&mindmg=10";
        testRequest.setUrl(testUrl4);
        assertEquals(testUrl4, testRequest.getUrl());
        assertEquals("-damage", testRequest.getParams().get("sort"));
        assertEquals("WaterGoblin", testRequest.getParams().get("name"));
        assertEquals("10", testRequest.getParams().get("mindmg"));


    }





}
