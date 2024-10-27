package edu.swen1.mtcg.services.db.repository;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.DbAccessException;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.utils.HashGenerator;
import org.json.JSONObject;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;


public class SessionRepository {

    private TransactionUnit transactionUnit;
    public SessionRepository(TransactionUnit transactionUnit) {
        this.transactionUnit = transactionUnit;
    }

    // Check if user exists
    public boolean checkUser(String username, String password) {
        try (PreparedStatement stmt = this.transactionUnit.prepareStatement("""
                        SELECT COUNT(*) FROM profile WHERE username = ?
                        """))
        {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                int count = rs.getInt(1);
                return (count == 0);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
        return false;
    }

    public Response registerUser(String username, String password) {
        if(checkUser(username, password)) {
            String[] hashPair = new String[2];
            hashPair = HashGenerator.generateHashPair(password);

            try
            {
                System.out.println(hashPair[0] + " " + hashPair[1] + " " + username);
                PreparedStatement stmt = this.transactionUnit.prepareStatement("""
                        INSERT INTO profile(id,username,password,salt,coins,playcount,elo) VALUES (DEFAULT, ?, ?, ?, DEFAULT, DEFAULT, DEFAULT)
                        """);
                stmt.setString(1, username);
                stmt.setString(2, hashPair[1]);
                stmt.setString(3, hashPair[0]);
                int rowCount = stmt.executeUpdate();
                System.out.println(rowCount + " rows inserted");
                stmt.close();

                return new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"created\": true }");


            } catch(SQLException e) {
                e.printStackTrace();
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"registered\": false }");

            }


        }
        else {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"User already exists\" }");
        }

    }


    public Response fetchUser(String username, String password) {

        try (PreparedStatement stmt = this.transactionUnit.prepareStatement("""
                        SELECT id, username, password, salt, coins, playcount, elo FROM profile WHERE username = ?
                        """))
        {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            User profile = null;
            while(rs.next()) {
                profile = new User(
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getInt(5),
                    rs.getInt(6),
                    rs.getInt(7)
                );
            }

            assert profile != null;
            String hashControl = HashGenerator.generateHash(password + profile.getSalt());

            // Get token based on password salt
            if(Objects.equals(hashControl, profile.getPassword())) {

                JSONObject body = new JSONObject();
                body.put("id", profile.getId());
                body.put("username", profile.getUsername());
                body.put("coins", profile.getCoins());
                body.put("playcount", profile.getPlayCount());
                body.put("elo", profile.getElo());
                body.put("token", profile.getUsername() + "-mtcgToken");

                String output = body.toString();
                return new Response(HttpStatus.OK, ContentType.JSON, output);
            }
            else {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{ \"registered\": false }");
            }





        } catch(SQLException e) {
            throw new DbAccessException("Could not register user " + username, e);
        }



    }



}
