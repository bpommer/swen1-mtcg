package edu.swen1.mtcg.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;

public class HashGenerator {

    // Generate salt/hash pair for new user
    public static HashMap<String, String> generateHashPair(String password) {
        try {

            HashMap<String, String> hashPair = new HashMap<>();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salt = generateSalt();
            String saltedPassword = password + salt;

            byte[] hash = digest.digest(saltedPassword.getBytes());

            hashPair.put("password", Base64.getEncoder().encodeToString(hash));
            hashPair.put("salt", salt);
            return hashPair;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String generateHash(String password) {
        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateSalt() {
        int saltLength = 16;
        byte[] salt = new byte[saltLength];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
