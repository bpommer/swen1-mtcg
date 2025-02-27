package edu.swen1.mtcg.services.db.models;

import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.utils.HashGenerator;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;


@Getter
@Setter
public class User {

    private int id;
    private String username;
    private String password;
    private String salt;
    private int coins;
    private int playCount;
    private int elo;
    private String token;
    private String stack;
    private String deck;
    private String name;
    private String bio;
    private String image;
    private int wins;
    private String lastLogin;
    private String tokenSalt;




    public User(Integer id, String username, String password, String salt,
                Integer coins, Integer playCount, Integer elo, String token,
                String stack, String deck, String name, String bio,
                String image, int wins, String lastLogin) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.coins = coins;
        this.playCount = playCount;
        this.elo = elo;
        this.token = token;
        this.stack = stack;
        this.deck = deck;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.wins = wins;
        this.lastLogin = lastLogin;
    }

    public User(Integer id, String username, String password, String salt,
                Integer coins, Integer playCount, Integer elo, String token,
                String stack, String deck, String name, String bio,
                String image, int wins, String lastLogin, String tokenSalt) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.coins = coins;
        this.playCount = playCount;
        this.elo = elo;
        this.token = token;
        this.stack = stack;
        this.deck = deck;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.wins = wins;
        this.lastLogin = lastLogin;
        this.tokenSalt = tokenSalt;
    }





    // Constructor for schema UserData
    public User(String username, String bio, String image) {
        this.username = username;
        this.bio = bio;
        this.image = image;
    }

    // Constructor for schema UserStats
    public User(String username, int elo, int wins) {
        this.username = username;
        this.elo = elo;
        this.wins = wins;
    }

    
    public JSONObject getUserData() {
        JSONObject userData = new JSONObject();
        userData.put("Name", username);
        userData.put("Bio", bio);
        userData.put("Image", image);
        return userData;
    }
    
    public JSONObject getUserStats() {
        JSONObject userStats = new JSONObject();

        if(this.name == null) {
            userStats.put("Name", Optional.empty());
        } else {
            userStats.put("Name", username);
        }


        userStats.put("Elo", elo);
        userStats.put("Wins", wins);
        userStats.put("Losses", this.playCount - this.wins);
        if((this.playCount - this.wins) < 0) {
            userStats.put("Losses", 0);
        }
        return userStats;
    }
    
    
    
    

    public int getId() {return id;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public String getSalt() {return salt;}
    public int getCoins() {return coins;}
    public int getPlayCount() {return playCount;}
    public int getElo() {return elo;}
    public JSONArray getStack() {return new JSONArray(stack);}
    public JSONArray getDeck() {return new JSONArray(deck);}







}
