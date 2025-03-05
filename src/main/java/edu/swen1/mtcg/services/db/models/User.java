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
    private int losses;
    private String lastLogin;

    public User() {

    }

    public User(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.password = user.getPassword();
        this.salt = user.getSalt();
        this.coins = user.getCoins();
        this.playCount = user.getPlayCount();
        this.elo = user.getElo();
        this.token = user.getToken();
        this.stack = user.getStack().toString();
        this.deck = user.getDeck().toString();
        this.name = user.getName();
        this.bio = user.getBio();
        this.image = user.getImage();
        this.wins = user.getWins();
        this.lastLogin = user.getLastLogin();
    }


    public User(Integer id, String username, String password, String salt,
                Integer coins, Integer playCount, Integer elo, String token,
                String stack, String deck, String name, String bio,
                String image, int wins, int losses, String lastLogin) {

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
        this.losses = losses;
        this.lastLogin = lastLogin;
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

        if(name != null) {
            userData.put("Name", name);
        } else {
            userData.put("Name", JSONObject.NULL);
        }

        if(bio != null) {
            userData.put("Bio", bio);
        } else {
            userData.put("Bio", JSONObject.NULL);
        }

        if(image != null) {
            userData.put("Image", image);
        } else {
            userData.put("Image", JSONObject.NULL);
        }

        return userData;
    }
    
    public JSONObject getUserStats() {
        JSONObject userStats = new JSONObject();

        if (this.name != null) {
            userStats.put("Name", name);
        } else {
            userStats.put("Name", JSONObject.NULL);
        }

        userStats.put("Elo", elo);
        userStats.put("Wins", wins);
        userStats.put("Losses", this.losses);

        return userStats;
    }

    public JSONArray getAllCards() {

        JSONArray stackCards = new JSONArray(stack);
        JSONArray deckCards = new JSONArray(deck);

        for(int i = 0; i < deckCards.length(); i++) {
            stackCards.put(deckCards.getJSONObject(i));
        }
        return stackCards;
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
