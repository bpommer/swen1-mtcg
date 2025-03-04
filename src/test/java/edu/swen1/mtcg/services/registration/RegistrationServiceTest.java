package edu.swen1.mtcg.services.registration;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.HeaderMap;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.RequestSchemaChecker;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.lang.reflect.Field;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistrationServiceTest {

    RegistrationService service = new RegistrationService();

    @InjectMocks
    RegistrationController mockController = mock(RegistrationController.class);

    @Mock public static MockedStatic<RequestSchemaChecker> requestSchemaChecker;

    @Mock public static MockedStatic<SessionRepository> sessionRepository;


    @Mock Request mockRequest = mock(Request.class);

    @Mock User mockUser = mock(User.class);

    @Mock
    HeaderMap mockHeaderMap = mock(HeaderMap.class);


    String registrationTestBody = new JSONObject()
            .put("Username", "testUser")
            .put("Password", "testPassword")
            .toString();


    String updateTestBody = new JSONObject()
            .put("Name", "test")
            .put("Bio", "testBio")
            .put("Image", "testImage")
            .toString();


    @BeforeAll
    static void setUp() {
        requestSchemaChecker = Mockito.mockStatic(RequestSchemaChecker.class);
        sessionRepository = Mockito.mockStatic(SessionRepository.class);
    }

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

    @AfterAll
    public static void tearDown() {
        requestSchemaChecker.close();
        sessionRepository.close();

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



        requestSchemaChecker.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                .thenReturn(true);

        sessionRepository.when(() -> SessionRepository.fetchUserFromName(any()))
                        .thenReturn(null);

        when(mockRequest.getMethod()).thenReturn(RestMethod.POST);
        when(mockRequest.getBody()).thenReturn(registrationTestBody);

        Response res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.CREATED.statusCode, res.getStatusCode());
    }



    @Test
    public void failUserRegistration() {


        // TEST: improper formatting of json
        requestSchemaChecker.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                .thenReturn(false);

        sessionRepository.when(() -> SessionRepository.fetchUserFromName(any()))
                .thenReturn(new User());


        when(mockRequest.getMethod()).thenReturn(RestMethod.POST);
        when(mockRequest.getBody()).thenReturn(registrationTestBody);

        Response res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, res.getStatusCode());

        // TEST: User already exists
        requestSchemaChecker.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                .thenReturn(true);


        res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.CONFLICT.statusCode, res.getStatusCode());

    }

    @Test
    public void testUserUpdate() {

        // TEST: update matching user
        requestSchemaChecker.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                .thenReturn(true);

        sessionRepository.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(mockUser);

        when(mockController.updateUser(any(), any(), any(), any()))
                .thenReturn(new Response(
                                HttpStatus.OK,
                                ContentType.TEXT,
                                "User sucessfully updated."
                        )
                );

        // Prepare request mock
        List<String> testList = new ArrayList<>(Arrays.asList("users", "kienboeck"));

        when(mockRequest.getMethod()).thenReturn(RestMethod.PUT);
        when(mockRequest.getBody()).thenReturn(updateTestBody);
        when(mockRequest.getPathParts()).thenReturn(testList);

        when(mockRequest.getHeaderMap()).thenReturn(mockHeaderMap);
        when(mockHeaderMap.getAuthHeader()).thenReturn("Bearer kienboeck-mtcgToken");

        when(mockUser.getUsername()).thenReturn("kienboeck");

        Response res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.OK.statusCode, res.getStatusCode());

        // TEST: update user as admin
        when(mockUser.getUsername()).thenReturn("admin");
        assertEquals(HttpStatus.OK.statusCode, res.getStatusCode());



    }

    @Test
    public void failUserUpdate() {

        // TEST: Username does not match
        requestSchemaChecker.when(() -> RequestSchemaChecker.JsonKeyValueCheck(any(), any()))
                .thenReturn(true);

        sessionRepository.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(mockUser);

        when(mockController.updateUser(any(), any(), any(), any()))
                .thenReturn(new Response(
                                HttpStatus.OK,
                                ContentType.TEXT,
                                "User sucessfully updated."
                        )
                );

        // Prepare request mock
        List<String> testList = new ArrayList<>(Arrays.asList("users", "kienboeck"));

        when(mockRequest.getMethod()).thenReturn(RestMethod.PUT);
        when(mockRequest.getBody()).thenReturn(updateTestBody);
        when(mockRequest.getPathParts()).thenReturn(testList);

        when(mockRequest.getHeaderMap()).thenReturn(mockHeaderMap);
        when(mockHeaderMap.getAuthHeader()).thenReturn("Bearer kienboeck-mtcgToken");

        when(mockUser.getUsername()).thenReturn("brian");

        Response res = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.UNAUTHORIZED.statusCode, res.getStatusCode());



    }





}
