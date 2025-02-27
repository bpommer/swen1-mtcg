package edu.swen1.mtcg.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;

public class HashGenerator {

    public static final String HASH_ALGORITHM = "SHA-256";
    public static final int SALT_LENGTH = 16;

    // Generate salt/hash pair for new user
    public static HashMap<String, String> generateHashPair(String password) {

        String salt = generateSalt();
        String saltedPassword = password + salt;
        String hashedPassword = HashGenerator.generateHash(saltedPassword);

        if (salt == null || hashedPassword == null) {
            return null;
        }

        HashMap<String, String> hashPair = new HashMap<>();

        hashPair.put("password", hashedPassword);
        hashPair.put("salt", salt);
        return hashPair;
    }



    // Digest string to hash
    public static String generateHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    // Generate random salt
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
