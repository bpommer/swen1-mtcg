package edu.swen1.mtcg.services.trading;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


public class TradingServiceTest {

    @Mock TradingService tradingService;
    String userToken = "Bearer kienboec-mtcgToken";
    String adminToken = "Bearer admin-mtcgToken";

    @Mock Request requestMock;
    JSONObject testCard1 = new JSONObject("{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}");
    JSONObject testCard2 = new JSONObject("{\"Id\":\"8c20639d-6400-4534-bd0f-ae563f11f57a\", \"Name\":\"WaterSpell\",   \"Damage\": 25.0}");



    @Mock User testUser1;
    @Mock User testUser2;

    JSONObject testDeal1 = new TradingDeal("TestTrade1", "845f0dc7-37d0-426e-994e-43fc3ac83c08",
            "spell", 20.0F).toJSON();


    @BeforeEach
    public void setup() {

        testUser1 = Mockito.mock(User.class);
        testUser2 = Mockito.mock(User.class);

        requestMock = Mockito.mock(Request.class);

        when(testUser1.getStack()).thenReturn(new JSONArray().put(testCard1));
        when(testUser2.getStack()).thenReturn(new JSONArray().put(testCard2));

        when(requestMock.getHeader("Authorization")).thenReturn(userToken);





        this.tradingService = new TradingService();

    }

    @Test
    public void testGet() {
        // Test auth
        when(requestMock.getMethod()).thenReturn(RestMethod.GET);
        when(requestMock.getHeader("Authorization")).thenReturn("");

        Response res1 = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.UNAUTHORIZED.statusCode, res1.getStatusCode());

        when(requestMock.getHeader("Authorization")).thenReturn(userToken);

        res1 = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.NO_CONTENT.statusCode, res1.getStatusCode());


    }

    @Test
    public void testTradeOffer() {
        when(requestMock.getMethod()).thenReturn(RestMethod.POST);
        when(requestMock.getHeader("Authorization")).thenReturn(userToken);
        when(requestMock.getBody()).thenReturn(testDeal1.toString());

        Response res = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.CREATED.statusCode, res.getStatusCode());

        User afterTest = SessionRepository.fetchUserFromId(2);

        for(int i = 0; i < afterTest.getStack().length(); i++) {
            JSONObject afterTestCard = afterTest.getStack().getJSONObject(i);
            if(afterTestCard.getString("Id").equals(testCard1.getString("Id"))) {
                fail();
            }

        }

    }

    @Test
    public void testTradingDeal() {
        when(requestMock.getMethod()).thenReturn(RestMethod.POST);
        when(requestMock.getHeader("Authorization")).thenReturn(userToken);
        when(requestMock.getBody()).thenReturn(testDeal1.toString());




    }








}
