package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.services.db.repository.SessionRepository;

import java.sql.ResultSet;
import java.sql.SQLException;

import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromName;
import static edu.swen1.mtcg.services.db.repository.SessionRepository.fetchUserFromToken;


public class TokenAuthenticator {


    public static boolean validUserToken(String token) {

        String[] splitToken;
        splitToken = token.split(" ", 2);

        if(!(splitToken[0].equals("Bearer"))) {
            return false;
        }

        ResultSet userData = null;
        try {
            userData = fetchUserFromToken(splitToken[1]);

            if(userData != null && userData.next()) {
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public static boolean validAdminToken(String token) {

        String[] splitToken;
        splitToken = token.split(" ", 2);

        if(!(splitToken[0].equals("Bearer"))) {
            return false;
        }
        ResultSet userData = null;

        try {

            userData = fetchUserFromName("admin");

            if(userData != null && userData.next()) {
                if(token.equals(userData.getString("token"))) {
                    return true;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;

    }






}
