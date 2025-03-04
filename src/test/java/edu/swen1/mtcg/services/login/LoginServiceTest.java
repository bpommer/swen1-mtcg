package edu.swen1.mtcg.services.login;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.services.registration.RegistrationService;
import edu.swen1.mtcg.utils.HashGenerator;
import edu.swen1.mtcg.utils.RequestSchemaChecker;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class LoginServiceTest {



    @InjectMocks
    LoginController mockController = mock(LoginController.class);

    LoginService loginService = new LoginService();


    String testUsername = "testUser";
    String testPassword = "testPassword";
    String testSalt = "XddMb84/30DMwU4VGQRobA==";
    String testPasswordHash = "TkBltlyD8A2XteyVU2E5WW4EMePx8EKSClDXHotksNA=";

    User testUser;

    @Mock
    public static MockedStatic<RequestSchemaChecker> checkerStub;
    @Mock
    public static MockedStatic<SessionRepository> sessionRepositoryStub;
    @Mock
    public static MockedStatic<HashGenerator> hashGeneratorStub;

    @BeforeAll
    static void setup() {
        checkerStub = Mockito.mockStatic(RequestSchemaChecker.class);
        sessionRepositoryStub = Mockito.mockStatic(SessionRepository.class);
        hashGeneratorStub = Mockito.mockStatic(HashGenerator.class);
    }

    @BeforeEach
    public void setUp() {
        testUser = new User();

        // Inject controller mock into service

        Field field = null;
        try {
            field = LoginService.class.getDeclaredField("controller");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        try {
            field.set(loginService, mockController);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @AfterAll
    static void tearDown() {
        checkerStub.close();
        sessionRepositoryStub.close();
        hashGeneratorStub.close();
    }


    @Test
    @DisplayName("Test correct password")
    public void testPasswordCheck() {

        try {

            // Insert password and salt in user model
            testUser.setPassword(testPasswordHash);
            testUser.setSalt(testSalt);

            // Set behavior of stubs
            when(mockController.login(any()))
                    .thenReturn(new Response(HttpStatus.OK, ContentType.TEXT, HttpStatus.OK.statusMessage));

            checkerStub.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                    .thenReturn(true);

            sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromName(any()))
                    .thenReturn(testUser);

            hashGeneratorStub.when(() -> HashGenerator.generateHash("testPasswordXddMb84/30DMwU4VGQRobA=="))
                    .thenReturn(testPasswordHash);


            Request testRequest = new Request();
            testRequest.setMethod(RestMethod.POST);

            testRequest.setBody(new JSONObject()
                    .put("Username", testUsername)
                    .put("Password", testPassword)
                    .toString()
            );

            Response res = loginService.handleRequest(testRequest);
            assertEquals(HttpStatus.OK.statusCode, res.getStatusCode());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Test incorrect password")
    public void failPasswordCheck() {

        try {
            // Insert password and salt in user model
            testUser.setPassword(testPasswordHash);
            testUser.setSalt(testSalt);

            // Set static mock behavior
            when(mockController.login(any()))
                    .thenReturn(new Response(HttpStatus.OK, ContentType.TEXT, HttpStatus.OK.statusMessage));

            checkerStub.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                    .thenReturn(true);

            sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromName(any()))
                    .thenReturn(testUser);

            hashGeneratorStub.when(() -> HashGenerator.generateHash(any()))
                    .thenReturn("abcdefg");


            Request testRequest = new Request();
            testRequest.setMethod(RestMethod.POST);

            testRequest.setBody(new JSONObject()
                    .put("Username", testUsername)
                    .put("Password", (testPassword + "abcdefg"))
                    .toString()
            );

            Response res = loginService.handleRequest(testRequest);
            assertEquals(HttpStatus.UNAUTHORIZED.statusCode, res.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }



}
