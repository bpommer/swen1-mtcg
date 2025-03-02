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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    @Test
    @DisplayName("Test correct password")
    public void testPasswordCheck() {

        try {
            MockedStatic<RequestSchemaChecker> checkerStub = Mockito.mockStatic(RequestSchemaChecker.class);
            MockedStatic<SessionRepository> sessionRepositoryStub = Mockito.mockStatic(SessionRepository.class);
            MockedStatic<HashGenerator> hashGeneratorStub = Mockito.mockStatic(HashGenerator.class);


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

            // Create mocks for static methods used and define behavior
            MockedStatic<RequestSchemaChecker> checkerStub = Mockito.mockStatic(RequestSchemaChecker.class);
            MockedStatic<SessionRepository> sessionRepositoryStub = Mockito.mockStatic(SessionRepository.class);
            MockedStatic<HashGenerator> hashGeneratorStub = Mockito.mockStatic(HashGenerator.class);

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
            fail();
        }
    }



}
