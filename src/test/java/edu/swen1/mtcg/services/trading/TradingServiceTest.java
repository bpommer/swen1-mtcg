package edu.swen1.mtcg.services.trading;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.BattleCard;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.services.db.repository.TradeRepository;
import edu.swen1.mtcg.services.login.LoginController;
import edu.swen1.mtcg.services.login.LoginService;
import edu.swen1.mtcg.utils.BattleCardFactory;
import edu.swen1.mtcg.utils.RequestSchemaChecker;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TradingServiceTest {

    TradingService tradingService = new TradingService();
    String userToken = "Bearer kienboec-mtcgToken";


    String testPackString = "[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]";
    JSONArray testPack = new JSONArray(testPackString);

    @Mock Request requestMock;
    JSONObject testDeal = new JSONObject(
            "{\"Id\":\"testDeal\", \"CardToTrade\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Type\": \"monster\",  \"MinimumDamage\": 10.0}");

    @InjectMocks
    TradingController mockController = mock(TradingController.class);

    @Mock TradingDeal mockTradingDeal = Mockito.mock(TradingDeal.class);

    JSONObject testDeal1 = new TradingDeal("TestTrade1", "845f0dc7-37d0-426e-994e-43fc3ac83c08",
            "spell", 20.0F).toJSON();


    @Mock
    User mockUser = Mockito.mock(User.class);

    @Mock
    BattleCard mockBattleCard = Mockito.mock(BattleCard.class);

    @Mock
    public static MockedStatic<SessionRepository> sessionRepositoryStub;

    @Mock
    public static MockedStatic<RequestSchemaChecker> checkerStub;

    @Mock
    public static MockedStatic<TradeRepository> tradeRepositoryStub;

    @Mock
    public static MockedStatic<BattleCardFactory> cardFactoryStub;


    @BeforeAll
    public static void setUp() {
        sessionRepositoryStub = Mockito.mockStatic(SessionRepository.class);
        checkerStub = Mockito.mockStatic(RequestSchemaChecker.class);
        tradeRepositoryStub = Mockito.mockStatic(TradeRepository.class);
        cardFactoryStub = Mockito.mockStatic(BattleCardFactory.class);
    }

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
    @AfterAll
    public static void tearDown() {
        sessionRepositoryStub.close();
        checkerStub.close();
        tradeRepositoryStub.close();
        cardFactoryStub.close();
    }


    @Test
    public void testAuth() {

        // Set static mock behavior
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
        sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(new User());


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

        // TEST: Card with invalid Damage
        JSONObject invalidDmg = new JSONObject(requestMock.getBody());
        invalidDmg.put("MinimumDamage", -1);
        when(requestMock.getBody()).thenReturn(invalidDmg.toString());

        res = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, res.getStatusCode());

        // TEST: Card with invalid card string
        JSONObject invalidId = new JSONObject(testDeal.toString());
        invalidId.put("CardToTrade", "845f0c7-37d0-426e-994e-43fc3ac83c08");
        when(requestMock.getBody()).thenReturn(invalidId.toString());

        res = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, res.getStatusCode());
    }

    @Test
    public void testTradingDealOwnerCheck() {

        // Set static mock behavior
        sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(mockUser);

        checkerStub.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                .thenReturn(true);


        tradeRepositoryStub.when(() -> TradeRepository.getTradingDeal(any()))
                .thenReturn(mockTradingDeal);

        // Set request mock behavior

        when(requestMock.getMethod()).thenReturn(RestMethod.POST);
        when(requestMock.getHeader("Authorization")).thenReturn(userToken);
        when(requestMock.getBody()).thenReturn(testDeal1.toString());
        when(requestMock.getPathParts()).thenReturn(new ArrayList<String>(Arrays.asList("test", "idTest")));


        // TEST: Trade with self
        // Set trade and user mock behavior
        when(mockTradingDeal.getOwnerId()).thenReturn(1);
        when(mockUser.getId()).thenReturn(1);

        Response res = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.FORBIDDEN.statusCode, res.getStatusCode());


    }

    @Test
    public void testCardOwnershipCheck() {
        // TEST: Search for card by ID from stack
        // and check type and dmg meets requirements


        // Set static mock behavior
        sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(mockUser);

        when(mockUser.getId()).thenReturn(2);
        when(mockUser.getStack()).thenReturn(testPack);

        checkerStub.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                .thenReturn(true);

        tradeRepositoryStub.when(() -> TradeRepository.getTradingDeal(any()))
                .thenReturn(mockTradingDeal);

        cardFactoryStub.when(() -> BattleCardFactory.buildBattleCard(any()))
                .thenReturn(mockBattleCard);

        // Prepare mock controller
        when(mockController.makeTrade(any(), any(), any()))
                .thenReturn(new Response(HttpStatus.OK, ContentType.TEXT, "OK\n"));

        // Mock trade deal and user model
        when(mockTradingDeal.getOwnerId()).thenReturn(1);
        when(mockTradingDeal.getMindamage()).thenReturn(10F);
        when(mockTradingDeal.getType()).thenReturn("monster");


        // Prepare property map stub
        HashMap<String, String> testMap = new HashMap<>();
        testMap.put("Type", "monster");

        // Prepare battle card mock
        when(mockBattleCard.getProperties())
                .thenReturn(testMap);
        when(mockBattleCard.getDamage()).thenReturn(50.0F);

        // Prepare request mock
        when(requestMock.getMethod()).thenReturn(RestMethod.POST);
        when(requestMock.getHeader("Authorization")).thenReturn(userToken);
        when(requestMock.getBody()).thenReturn("\"845f0dc7-37d0-426e-994e-43fc3ac83c08\"");
        when(requestMock.getPathParts()).thenReturn(new ArrayList<String>(Arrays.asList("test", "idTest")));

        Response res = tradingService.handleRequest(requestMock);
        assertEquals(HttpStatus.OK.statusCode, res.getStatusCode());

    }








}
