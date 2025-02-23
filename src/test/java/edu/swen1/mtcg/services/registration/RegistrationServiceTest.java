package edu.swen1.mtcg.services.registration;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistrationServiceTest {

    RegistrationService service = new RegistrationService();

    @InjectMocks
    RegistrationController mockController = mock(RegistrationController.class);


    String registrationTestBody = new JSONObject()
            .put("Username", "testUser")
            .put("Password", "testPassword")
            .toString();

    String registrationInvalidType1 = new JSONObject()
            .put("Username", 123)
            .put("Password", "testPassword")
            .toString();

    String registrationInvalidType2 = new JSONObject()
            .put("Username", "testUser")
            .put("Password", true)
            .toString();

    String registrationInvalidType3 = new JSONObject()
            .put("Username", 123.45)
            .toString();

    String updateTestBody1 = new JSONObject()
            .put("Name", "test")
            .put("Bio", "testBio")
            .put("Image", "testImage")
            .toString();

    // empty field
    String updateTestBody2 = new JSONObject()
            .put("Name", "")
            .put("Bio", "testBio")
            .put("Image", "testImage")
            .toString();



    @BeforeEach
    public void setup() {

        // Fetch controller attribute from class
        // and inject mock

        Field field = null;
        try {
            field = RegistrationService.class.getDeclaredField("controller");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        field.setAccessible(true);

        try {
            field.set(service, mockController);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }


    // Test user registration
    @Test
    @DisplayName("Register with properly formatted request")
    public void testUserRegistration() {


        when(mockController.register(any(), any()))
                .thenReturn(new Response(
                        HttpStatus.CREATED,
                        ContentType.TEXT,
                        "User successfully created"
                )
        );

        Request testRequest = new Request();
        testRequest.setMethod(RestMethod.POST);
        testRequest.setBody(registrationTestBody);

        Response res = service.handleRequest(testRequest);
        assertEquals(HttpStatus.CREATED.statusCode, res.getStatusCode());
    }



    @Test
    public void failUserRegistration() {

        Request testRequest = new Request();
        testRequest.setMethod(RestMethod.POST);
        testRequest.setBody(registrationInvalidType1);

        Response res = service.handleRequest(testRequest);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, res.getStatusCode());

        testRequest = new Request();
        testRequest.setMethod(RestMethod.POST);
        testRequest.setBody(registrationInvalidType2);

        res = service.handleRequest(testRequest);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, res.getStatusCode());

        testRequest = new Request();
        testRequest.setMethod(RestMethod.POST);
        testRequest.setBody(registrationInvalidType3);

        res = service.handleRequest(testRequest);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, res.getStatusCode());

    }

    @Test
    public void testUserUpdate() {

        when(mockController.updateUser(any(), any(), any(), any()))
                .thenReturn(new Response(
                                HttpStatus.OK,
                                ContentType.TEXT,
                                "User sucessfully updated."
                        )
                );

        Request testRequest = new Request();
        testRequest.setMethod(RestMethod.PUT);
        testRequest.setBody(updateTestBody1);
        testRequest.setPath("/users/testUser");

        Response res = service.handleRequest(testRequest);
        assertEquals(HttpStatus.OK.statusCode, res.getStatusCode());





    }





}
