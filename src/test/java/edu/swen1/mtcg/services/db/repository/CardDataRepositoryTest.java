package edu.swen1.mtcg.services.db.repository;


import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;


import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
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
            cards = CardDataRepository.getCardHashSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }








}
