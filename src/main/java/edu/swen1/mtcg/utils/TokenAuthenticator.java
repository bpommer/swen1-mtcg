package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;

import java.sql.ResultSet;
import java.sql.SQLException;

import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromName;
import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;


public class TokenAuthenticator {

    // User token: Fetch user data from db with token
    // and compare with corresponding username
    public static boolean validUserToken(String username, String token) {

        String[] splitToken;
        splitToken = token.split(" ", 2);

        System.out.println("User token validation");


        System.out.println(splitToken[0]);

        System.out.println(splitToken[1]);

        if(!(splitToken[0].equals("Bearer"))) {
            return false;
        }



        User userData = null;
        try {
            userData = fetchUserFromToken(splitToken[1]);
            if(userData != null) {
                return username.equals(userData.getUsername());
            }

        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public static boolean validAdminToken(String token) {

        String[] splitToken;
        splitToken = token.split(" ", 2);

        System.out.println(splitToken[0]);

        System.out.println(splitToken[1]);

        if(!(splitToken[0].equals("Bearer"))) {
            return false;
        }
        User userData = null;

        try {

            userData = fetchUserFromName("admin");

            if(userData != null) {
                if(splitToken[1].equals(userData.getToken())) {
                    return true;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;

    }






}
