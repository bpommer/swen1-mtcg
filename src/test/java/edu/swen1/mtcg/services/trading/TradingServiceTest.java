package edu.swen1.mtcg.services.trading;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.services.login.LoginController;
import edu.swen1.mtcg.services.login.LoginService;
import edu.swen1.mtcg.utils.RequestSchemaChecker;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TradingServiceTest {

    TradingService tradingService = new TradingService();
    String userToken = "Bearer kienboec-mtcgToken";
    String adminToken = "Bearer admin-mtcgToken";

    @Mock Request requestMock;
    JSONObject testDeal = new JSONObject(
            "{\"Id\":\"testDeal\", \"CardToTrade\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Type\": \"monster\",  \"MinimumDamage\": 10.0}");

    @InjectMocks
    TradingController mockController = mock(TradingController.class);

    @Mock User testUser1;
    @Mock User testUser2;

    JSONObject testDeal1 = new TradingDeal("TestTrade1", "845f0dc7-37d0-426e-994e-43fc3ac83c08",
            "spell", 20.0F).toJSON();


    @BeforeEach
    public void setup() {

        // Inject controller mock into service

        Field field = null;
        try {
            field = TradingService.class.getDeclaredField("controller");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        try {
            field.set(tradingService, mockController);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }


        requestMock = Mockito.mock(Request.class);

    }

    @Test
    public void testAuth() {

        // Set static mock behavior
        MockedStatic<SessionRepository> sessionRepositoryStub = Mockito.mockStatic(SessionRepository.class);
        sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(null);

        // Test auth
        when(requestMock.getMethod()).thenReturn(RestMethod.GET);
        when(requestMock.getHeader("Authorization")).thenReturn("");

        Response res1 = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.UNAUTHORIZED.statusCode, res1.getStatusCode());


    }

    @Test
    public void testTradePost() {

        // Set static mock behavior
        MockedStatic<SessionRepository> sessionRepositoryStub = Mockito.mockStatic(SessionRepository.class);
        sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(new User());

        MockedStatic<RequestSchemaChecker> checkerStub = Mockito.mockStatic(RequestSchemaChecker.class);
        checkerStub.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                .thenReturn(true);

        // Set request mock behavior
        when(requestMock.getMethod()).thenReturn(RestMethod.POST);
        when(requestMock.getHeader("Authorization")).thenReturn(userToken);
        when(requestMock.getBody()).thenReturn(testDeal.toString());
        when(requestMock.getPathParts()).thenReturn(new ArrayList<String>(Arrays.asList("test")));

        // Set controller mock behavior
        when(mockController.newTradeOffer(any(), any()))
                .thenReturn(new Response(HttpStatus.CREATED, ContentType.TEXT, "Trade created\n"));

        // Test request with properly formed request
        Response res = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.CREATED.statusCode, res.getStatusCode());

        // Test with invalid Damage
        JSONObject invalidDmg = new JSONObject(requestMock.getBody());
        invalidDmg.put("MinimumDamage", -1);
        when(requestMock.getBody()).thenReturn(invalidDmg.toString());

        res = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, res.getStatusCode());

        // Test with invalid card string
        JSONObject invalidId = new JSONObject(testDeal.toString());
        invalidId.put("CardToTrade", "845f0c7-37d0-426e-994e-43fc3ac83c08");
        when(requestMock.getBody()).thenReturn(invalidId.toString());

        res = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, res.getStatusCode());


    }

    @Test
    public void testTradingDeal() {
        when(requestMock.getMethod()).thenReturn(RestMethod.POST);
        when(requestMock.getHeader("Authorization")).thenReturn(userToken);
        when(requestMock.getBody()).thenReturn(testDeal1.toString());




    }








}
