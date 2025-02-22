package edu.swen1.mtcg.services.registration;

import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistrationServiceTest {

    Request adminMock;
    Request userMock;
    Request errorMock;

    RegistrationService registrationService;

    @BeforeEach
    public void setup() {
        registrationService = new RegistrationService();
        this.adminMock = new Request();
        this.userMock = new Request();
        this.errorMock = new Request();

    }


    // Test user registration
    @Test
    public void registerUser() {

        this.adminMock.setMethod(RestMethod.POST);
        this.adminMock.setBody("{\"Username\":\"admin\",    \"Password\":\"istrator\"}");

        this.userMock.setMethod(RestMethod.POST);
        this.userMock.setBody("{\"Username\":\"kienboec\", \"Password\":\"different\"}");


        Response testResponse;
        testResponse = registrationService.handleRequest(adminMock);
        assertEquals(HttpStatus.CREATED.statusCode, testResponse.getStatusCode());

        testResponse = registrationService.handleRequest(userMock);
        assertEquals(HttpStatus.CREATED.statusCode, testResponse.getStatusCode());



        // Duplicate registration
        testResponse = registrationService.handleRequest(userMock);
        assertEquals(HttpStatus.CONFLICT.statusCode, testResponse.getStatusCode());
    }



    @Test
    public void shouldFail() {

        // Check conflict
        this.adminMock.setMethod(RestMethod.POST);
        this.adminMock.setBody("{\"Username\":\"admin\",    \"Password\":\"istrator\"}");;

        Response testResponse;
        testResponse = registrationService.handleRequest(adminMock);
        assertEquals(HttpStatus.CONFLICT.statusCode, testResponse.getStatusCode());

        // Check bad keys
        this.errorMock.setMethod(RestMethod.POST);
        this.errorMock.setBody("{\"Usename\":\"admin\",    \"Password\":\"istrator\"}");;

        testResponse = registrationService.handleRequest(errorMock);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, testResponse.getStatusCode());

        // Check additional keys
        this.errorMock.setMethod(RestMethod.POST);
        this.errorMock.setBody("{\"Username\":\"admin\",    \"Password\":\"istrator\", \"aaa\":\"bbb\"}");;

        testResponse = registrationService.handleRequest(errorMock);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, testResponse.getStatusCode());

    }


}
