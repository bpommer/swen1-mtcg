package edu.swen1.mtcg.services.transactions;
import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.HeaderMap;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.services.packages.PackagesService;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionServiceTest {


    TransactionsService service = new TransactionsService();

    @Mock
    Request mockRequest = mock(Request.class);
    @Mock
    User userMock = mock(User.class);
    @Mock
    public static MockedStatic<SessionRepository> sessionRepositoryMock;
    @Mock
    HeaderMap headerMapMock = mock(HeaderMap.class);

    @InjectMocks
    TransactionsController controller = mock(TransactionsController.class);



    @BeforeAll
    public static void setUp() {
        sessionRepositoryMock = Mockito.mockStatic(SessionRepository.class);
    }

    @BeforeEach
    public void setup() {

        Field field = null;
        try {
            field = TransactionsService.class.getDeclaredField("controller");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        try {
            field.set(service, controller);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        when(controller.purchasePack(any()))
                .thenReturn(new Response(HttpStatus.OK, ContentType.TEXT, HttpStatus.OK.statusMessage));

    }

    @AfterAll
    public static void tearDown() {
        sessionRepositoryMock.close();

    }


    @Test
    public void testCoinCount() {


        // Setup mock request
        when(mockRequest.getMethod()).thenReturn(RestMethod.POST);
        when(mockRequest.getPathParts())
        .thenReturn(new ArrayList<>(Arrays.asList("transactions", "packages")));
        when(mockRequest.getHeaderMap()).thenReturn(headerMapMock);
        when(headerMapMock.getAuthHeader()).thenReturn("Bearer token");

        // Setup user mock
        when(userMock.getCoins()).thenReturn(6);
        when(userMock.getStack()).thenReturn(new JSONArray());
        when(userMock.getId()).thenReturn(2);

        // Setup static mock
        sessionRepositoryMock.when(() -> fetchUserFromToken(any()))
                .thenReturn(userMock);

        // TEST: User has enough coins
        Response res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.OK.statusCode, res.getStatusCode());

        // TEST: User does not have enough coins
        when(userMock.getCoins()).thenReturn(3);
        res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.FORBIDDEN.statusCode, res.getStatusCode());


    }

    // Test if routing works
    @Test
    public void testRouting() {

        // Setup mock request
        when(mockRequest.getMethod()).thenReturn(RestMethod.POST);
        when(mockRequest.getPathParts())
                .thenReturn(new ArrayList<>(Arrays.asList("transactions", "cards")));
        when(mockRequest.getHeaderMap()).thenReturn(headerMapMock);
        when(headerMapMock.getAuthHeader()).thenReturn("Bearer token");

        // TEST: invalid route
        Response res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.NOT_IMPLEMENTED.statusCode, res.getStatusCode());

        // TEST: only one pathPart
        when(mockRequest.getPathParts())
                .thenReturn(new ArrayList<>(Arrays.asList("transactions")));
        assertEquals(HttpStatus.NOT_IMPLEMENTED.statusCode, res.getStatusCode());


    }



}
