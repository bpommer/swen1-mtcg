package edu.swen1.mtcg.services.db.repository;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.DbAccessException;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.utils.HashGenerator;
import org.json.JSONArray;
import org.json.JSONObject;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;



public class SessionRepository {

    private TransactionUnit transactionUnit;
    public SessionRepository(TransactionUnit transactionUnit) {
        this.transactionUnit = transactionUnit;
    }


    // POST /users
    public Response registerUser(String username, String password) {


        HashMap<String, String> hashPair = HashGenerator.generateHashPair(password);


            try {
                // System.out.println(hashPair[0] + " " + hashPair[1] + " " + username);
                PreparedStatement stmt = this.transactionUnit.prepareStatement("""
                        INSERT INTO profile(id, username, password, salt, coins, playcount, elo, token, stack, deck, bio, image, wins, lastlogin)
                        VALUES (DEFAULT, ?, ?, ?, DEFAULT, DEFAULT, DEFAULT, ?, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT)
                       """);


                if(hashPair.get("password") == null || hashPair.get("salt") == null) {
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error");
                }

                stmt.setString(1, username);

                stmt.setString(2, hashPair.get("password"));
                stmt.setString(3, hashPair.get("salt"));
                stmt.setNull(4, Types.VARCHAR);

                stmt.executeUpdate();
                return new Response(HttpStatus.CREATED, ContentType.TEXT, "User successfully created");
            } catch(SQLException e) {
                e.printStackTrace();
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error");
            }
    }
    public Response loginUser(String username, String password) {
        User foundUser = null;
        foundUser = fetchUserFromName(username);

        if(foundUser == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Invalid username/password provided");
        }

        String hashedPassword = HashGenerator.generateHash(password + foundUser.getSalt());

        if(!hashedPassword.equals(foundUser.getPassword())) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Invalid username/password provided");
        }

        String token = username + "-mtcgToken";
        String hashedToken = HashGenerator.generateHash(token);

        String tokenQuery = """
                UPDATE profile
                SET token = ?
                WHERE id = ?""";

        try {
            PreparedStatement stmt = this.transactionUnit.prepareStatement(tokenQuery);
            stmt.setString(1, hashedToken);
            stmt.setInt(2, foundUser.getId());

        } catch (SQLException e) {
            throw new DbAccessException(e);
        }

        JSONObject resBody = new JSONObject().put("token", token);

        return new Response(HttpStatus.OK, ContentType.JSON, resBody.toString());

    }





    public Response loginUser(String username) {

            String newToken = username + "-mtcgToken";
            String hashedToken = HashGenerator.generateHash(newToken);

            String tokenQuery = """
                    UPDATE profile
                    SET token = ?
                    WHERE username = ?""";

            try {
                PreparedStatement stmt = this.transactionUnit.prepareStatement(tokenQuery);
                stmt.setString(1, hashedToken);
                stmt.setString(2, username);
                stmt.executeUpdate();
            } catch(SQLException e) {
                throw new DbAccessException("Error in login");
            }

            JSONObject response = new JSONObject();
            response.put("token", newToken);
            return new Response(HttpStatus.OK, ContentType.JSON, response.toString());











    }



    // PUT /users/{username}
    public Response updateUser(String username, String name, String bio, String image) {

        User searchUser = fetchUserFromName(username);
        if(searchUser == null) {
            return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "User not found");
        }

        String query = "UPDATE profile SET name = ?, bio = ?, image = ? WHERE username = ?";

        try (PreparedStatement stmt = this.transactionUnit.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, bio);
            stmt.setString(4, image);
            stmt.setString(4, username);
            stmt.executeUpdate();
            return new Response(HttpStatus.OK, ContentType.TEXT, "User successfully updated.");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error.");
        }
    }

    // Fetch user data from token
    // Returns null if token is invalid or user does not exist
    public static User fetchUserFromToken(String token) {
        String[] splitToken = token.split(" ", 2);

        if(!splitToken[0].equals("Bearer")) {
            return null;
        }

        String tokenHash = HashGenerator.generateHash(splitToken[1]);
        String query = "SELECT * FROM profile WHERE token = ?";

        TransactionUnit tempUnit = new TransactionUnit();
        PreparedStatement stmt = tempUnit.prepareStatement(query);

        try {
            stmt.setString(1, tokenHash);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return new User(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getInt(6),
                        rs.getInt(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10),
                        rs.getString(11),
                        rs.getString(12),
                        rs.getString(13),
                        rs.getInt(14),
                        rs.getString(15)
                );
            }
            return null;
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static User fetchUserFromName(String username) {

        String query = "SELECT * FROM profile WHERE username = ?";

        TransactionUnit tempUnit = new TransactionUnit();

        try {
            PreparedStatement stmt = tempUnit.prepareStatement(query);

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return new User(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getInt(6),
                        rs.getInt(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10),
                        rs.getString(11),
                        rs.getString(12),
                        rs.getString(13),
                        rs.getInt(14),
                        rs.getString(15)
                );

            } else {
                return null;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User fetchUserFromId(int id) {

        String query = "SELECT * FROM profile WHERE id = ?";

        TransactionUnit tempUnit = new TransactionUnit();
        PreparedStatement stmt = tempUnit.prepareStatement(query);

        try {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
            return new User(
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getInt(5),
                    rs.getInt(6),
                    rs.getInt(7),
                    rs.getString(8),
                    rs.getString(9),
                    rs.getString(10),
                    rs.getString(11),
                    rs.getString(12),
                    rs.getString(13),
                    rs.getInt(14),
                    rs.getString(15)
            );
            }
            return null;



        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static JSONArray fetchScoreboard() {
        TransactionUnit tempUnit = new TransactionUnit();

        String query = "SELECT * FROM profile ORDER BY elo DESC";
        PreparedStatement stmt = tempUnit.prepareStatement(query);

        ResultSet rs = null;
        JSONArray jsonArray = new JSONArray();

        try {
            rs = stmt.executeQuery();

            while(rs.next()) {

                User user = new User(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getInt(6),
                        rs.getInt(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10),
                        rs.getString(11),
                        rs.getString(12),
                        rs.getString(13),
                        rs.getInt(14),
                        rs.getString(15)
                );

                jsonArray.put(user.getUserStats());

            }
            return jsonArray;
        } catch (SQLException e) {
            return null;
        }


    }

}
