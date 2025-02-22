package edu.swen1.mtcg.services.packages;

import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.HeaderMap;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class PackageServiceTest {
    PackagesService service;
    @Mock Request adminMock;
    @Mock Request userMock;
    @Mock HeaderMap adminHeaders;
    @Mock HeaderMap userHeaders;

    String testPack1 = "[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]";
    String testPack2 = "[{\"Id\":\"644808c2-f87a-4600-b313-122b02322fd5\", \"Name\":\"WaterGoblin\", \"Damage\":  9.0}, {\"Id\":\"4a2757d6-b1c3-47ac-b9a3-91deab093531\", \"Name\":\"Dragon\", \"Damage\": 55.0}, {\"Id\":\"91a6471b-1426-43f6-ad65-6fc473e16f9f\", \"Name\":\"WaterSpell\", \"Damage\": 21.0}, {\"Id\":\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\", \"Name\":\"Ork\", \"Damage\": 55.0}, {\"Id\":\"f8043c23-1534-4487-b66b-238e0c3c39b5\", \"Name\":\"WaterSpell\",   \"Damage\": 23.0}]";
    String typoPackage = "[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damge\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]";
    String extraKeyPackage = "[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0, \"abvc\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]";


    @BeforeEach
    public void setup() {
        service = new PackagesService();
        adminHeaders = new HeaderMap();
        userHeaders = new HeaderMap();
        adminMock = Mockito.mock(Request.class);
        userMock = Mockito.mock(Request.class);

        when(adminMock.getHeader("Authorization")).thenReturn("Bearer admin-mtcgToken");
        when(userMock.getHeader("Authorization")).thenReturn("Bearer kienboec-mtcgToken");

        when(adminMock.getBody()).thenReturn(testPack1);
        when(userMock.getBody()).thenReturn(testPack1);

        when(adminMock.getMethod()).thenReturn(RestMethod.POST);
        when(userMock.getMethod()).thenReturn(RestMethod.POST);

    }

    @Test
    public void testWrongToken() {

        Response testResponse = null;
        testResponse = service.handleRequest(userMock);
        assertEquals(HttpStatus.FORBIDDEN.statusCode, testResponse.getStatusCode());

        when(userHeaders.getAuthHeader()).thenReturn("Bearer asfdsafag");
        testResponse = service.handleRequest(userMock);
        assertEquals(HttpStatus.UNAUTHORIZED.statusCode, testResponse.getStatusCode());

    }

    @Test
    public void registerSamePackageTest() {
        Response testResponse = null;
        testResponse = service.handleRequest(adminMock);
        assertEquals(HttpStatus.CREATED.statusCode, testResponse.getStatusCode());

        when(adminMock.getBody()).thenReturn(testPack2);
        testResponse = service.handleRequest(adminMock);
        assertEquals(HttpStatus.CREATED.statusCode, testResponse.getStatusCode());

        // Test registration of duplicate package
        testResponse = service.handleRequest(adminMock);
        assertEquals(HttpStatus.CONFLICT.statusCode, testResponse.getStatusCode());
    }

    @Test
    public void registerMisformedPackageTest() {
        Response testResponse = null;

        // Test with typo
        when(adminMock.getBody()).thenReturn(typoPackage);
        testResponse = service.handleRequest(adminMock);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, testResponse.getStatusCode());

        // Test with extra key
        when(adminMock.getBody()).thenReturn(extraKeyPackage);
        testResponse = service.handleRequest(adminMock);
        assertEquals(HttpStatus.BAD_REQUEST.statusCode, testResponse.getStatusCode());

    }





}
