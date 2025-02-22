package edu.swen1.mtcg.services.transactions;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class TransactionServiceTest {
    @Mock
    User userMock;
    TransactionsController controller;
    TransactionsService service;
    @Mock
    Request mockRequest;


    @BeforeEach
    public void setup() {
        userMock = Mockito.mock(User.class);
        controller = new TransactionsController();
        service = new TransactionsService();
        mockRequest = Mockito.mock(Request.class);
    }


    @Test
    public void noCoins() {
        when(userMock.getCoins()).thenReturn(0);
        when(userMock.getStack()).thenReturn(new JSONArray());
        when(userMock.getId()).thenReturn(2);
        Response res = controller.purchasePack(userMock);
        assertEquals(HttpStatus.FORBIDDEN.statusCode, res.getStatusCode());

    }

    @Test
    public void buyPackages() {
        when(userMock.getCoins()).thenReturn(20);
        when(userMock.getStack()).thenReturn(new JSONArray());
        when(userMock.getId()).thenReturn(2);


        Response res = controller.purchasePack(userMock);
        assertEquals(HttpStatus.OK.statusCode, res.getStatusCode());

        User afterTest = SessionRepository.fetchUserFromId(2);
        assertEquals(15, afterTest.getCoins());


        when(userMock.getCoins()).thenReturn(15);
        when(userMock.getStack()).thenReturn(new JSONArray());
        when(userMock.getId()).thenReturn(2);
        res = controller.purchasePack(userMock);
        assertEquals(HttpStatus.OK.statusCode, res.getStatusCode());

        afterTest = SessionRepository.fetchUserFromId(2);
        assertEquals(10, afterTest.getCoins());

        when(userMock.getCoins()).thenReturn(15);
        when(userMock.getStack()).thenReturn(new JSONArray());
        when(userMock.getId()).thenReturn(2);
        res = controller.purchasePack(userMock);
        assertEquals(HttpStatus.NOT_FOUND.statusCode, res.getStatusCode());

    }

    // Test if routing works
    @Test
    public void testRouting() {
        when(mockRequest.getPathParts()).thenReturn(Arrays.asList("transactions", "packages"));
        when(mockRequest.getHeader("Authorization")).thenReturn("kodsjfoaphfn sdkjfpaoi");
        when(mockRequest.getMethod()).thenReturn(RestMethod.POST);

        Response res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.UNAUTHORIZED.statusCode, res.getStatusCode());
    }



}
