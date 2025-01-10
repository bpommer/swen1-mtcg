package edu.swen1.mtcg.services.db.models;

import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.utils.HashGenerator;




public class User {

    private int id;
    private String username;
    private String password;
    private String salt;
    private int coins;
    private int playCount;
    private int elo;


    // Overload constructor to allow registration/login and full user data
    public User(Integer id, String username, String password, String salt, Integer coins, Integer playCount, Integer elo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.coins = coins;
        this.playCount = playCount;
        this.elo = elo;
    }

    // Constructor for login/registration
    public User(String username, String password, RestMethod method) {
        this.username = username;

        // If POST, hash and salt password
        if(method == RestMethod.POST) {
            String[] pwgen = HashGenerator.generateHashPair(username);
            this.password = pwgen[0] + pwgen[1];
            this.salt = pwgen[1];
        }
        else {
            this.password = password;
            this.salt = null;
        }

    }

    public int getId() {return id;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public String getSalt() {return salt;}
    public int getCoins() {return coins;}
    public int getPlayCount() {return playCount;}
    public int getElo() {return elo;}






}
