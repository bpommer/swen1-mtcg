package edu.swen1.mtcg.utils;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class HashGeneratorTest {

    String password = "testPassword";

    String testSalt = "T1gT2Eood45TbY0V7L+rtWPehVtQasbyyuiiDB3M";

    String testHash = "k4Mthsl5gRz3lsSWNoqJD0IjzTMAWz2aqMrrUHTCgdA=";


    @Test
    public void testHashGenerator() {
        String res = HashGenerator.generateHash((password + testSalt));
        assertEquals(testHash, res);

        res = HashGenerator.generateHash((password + testSalt + "a"));
        assertFalse(res.equals(testHash));

        res = HashGenerator.generateHash(null);
        assertNull(res);

        res = HashGenerator.generateHash("");
        assertNull(res);


    }

    @Test
    public void testHashPairGenerator() {

        HashMap<String, String> hashPair = HashGenerator.generateHashPair(password);
        assertEquals(44, hashPair.get("password").length());
        assertEquals(40, hashPair.get("salt").length());

        hashPair = HashGenerator.generateHashPair(null);
        assertNull(hashPair);

        hashPair = HashGenerator.generateHashPair("");
        assertNull(hashPair);

    }






}
