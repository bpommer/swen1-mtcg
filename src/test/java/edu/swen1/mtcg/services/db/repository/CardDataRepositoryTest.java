package edu.swen1.mtcg.services.db.repository;


import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;


import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;


public class CardDataRepositoryTest {

    CardDataRepository cardDataRepository;


    @BeforeEach
    public void setup() {
        cardDataRepository = new CardDataRepository(new TransactionUnit());
    }

    public void hashSetTest() {

        HashSet<String> cards = null;

        try {
            cards = CardDataRepository.getHashSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }








}
