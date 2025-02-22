package edu.swen1.mtcg.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class HashGenerator {

    // Generate salt/hash pair for new user
    public static String[] generateHashPair(String password) {
        try {
            String[] hashPair = new String[2];
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hashPair[0] = generateSalt();
            String saltedPassword = password + hashPair[0];

            byte[] hash = digest.digest(saltedPassword.getBytes());
            hashPair[1] = Base64.getEncoder().encodeToString(hash);
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
