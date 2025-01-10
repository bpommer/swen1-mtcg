package edu.swen1.mtcg.services.db.repository;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.BattleCard;

import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import org.mockito.Mock;

public class PackageRepositoryTest {

    String testDeckString = "[{\"Id\":\"d7d0cb94-2cbf-4f97-8ccf-9933dc5354b8\", \"Name\":\"WaterGoblin\", \"Damage\":  9.0}, {\"Id\":\"44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e\", \"Name\":\"Dragon\", \"Damage\": 55.0}, {\"Id\":\"2c98cd06-518b-464c-b911-8d787216cddd\", \"Name\":\"WaterSpell\", \"Damage\": 21.0}, {\"Id\":\"951e886a-0fbf-425d-8df5-af2ee4830d85\", \"Name\":\"Ork\", \"Damage\": 55.0}, {\"Id\":\"dcd93250-25a7-4dca-85da-cad2789f7198\", \"Name\":\"FireSpell\", \"Damage\": 23.0}]";
    JSONArray testDeck;
    @Mock PackageRepository repo;



    @BeforeEach
    void setUp() {

        testDeck = new JSONArray(testDeckString);
    }




    @Test
    void testPackageRegistration() {

        TransactionUnit testUnit = new TransactionUnit();
        repo = new PackageRepository(testUnit);

        System.out.println(testDeck.toString());

        Response res = null;

        res = repo.registerPackage(testDeck);

        // assertEquals(201, res.getStatusCode());

        // testUnit.dbCommit();

        // res = repo.registerPackage(testDeck);

        assertEquals(409, res.getStatusCode());

    }






}
