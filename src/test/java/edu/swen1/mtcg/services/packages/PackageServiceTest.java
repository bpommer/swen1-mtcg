package edu.swen1.mtcg.services.packages;

import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.HeaderMap;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.services.trading.TradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PackageServiceTest {

    PackagesService service = new PackagesService();

    @InjectMocks
    PackageController mockController = Mockito.mock(PackageController.class);
    @Mock Request mockRequest = Mockito.mock(Request.class);

    @Mock User mockUser = mock(User.class);


    String testPack1 = "[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]";
    String testPack2 = "[{\"Id\":\"644808c2-f87a-4600-b313-122b02322fd5\", \"Name\":\"WaterGoblin\", \"Damage\":  9.0}, {\"Id\":\"4a2757d6-b1c3-47ac-b9a3-91deab093531\", \"Name\":\"Dragon\", \"Damage\": 55.0}, {\"Id\":\"91a6471b-1426-43f6-ad65-6fc473e16f9f\", \"Name\":\"WaterSpell\", \"Damage\": 21.0}, {\"Id\":\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\", \"Name\":\"Ork\", \"Damage\": 55.0}, {\"Id\":\"f8043c23-1534-4487-b66b-238e0c3c39b5\", \"Name\":\"WaterSpell\",   \"Damage\": 23.0}]";
    String typoPackage = "[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damge\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]";


    @BeforeEach
    public void setup() {
        Field field = null;
        try {
            field = PackagesService.class.getDeclaredField("controller");
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

    @Test
    public void testPrivilege() {
        // Set static mock behavior
        MockedStatic<SessionRepository> sessionRepositoryStub = Mockito.mockStatic(SessionRepository.class);
        sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(mockUser);

        when(mockUser.getUsername()).thenReturn("user");
        when(mockRequest.getMethod()).thenReturn(RestMethod.POST);

        Response testResponse = null;
        testResponse = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.FORBIDDEN.statusCode, testResponse.getStatusCode());

        sessionRepositoryStub.when(() -> SessionRepository.fetchUserFromToken(any()))
                .thenReturn(null);
        testResponse = service.handleRequest(mockRequest);
        assertEquals(HttpStatus.UNAUTHORIZED.statusCode, testResponse.getStatusCode());

    }



}
