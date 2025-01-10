package edu.swen1.mtcg.services.db.models;

import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.utils.HashGenerator;
import org.json.JSONObject;


public class Card {

    private String id;
    private String name;
    private float damage;


    // Construct card from JSON entry
    public Card(JSONObject cardJson) {
        this.id = cardJson.getString("Id");
        this.name = cardJson.getString("Name");
        this.damage = cardJson.getFloat("Damage");
    }

    public Card(String id, String name, float damage) {
        this.id = id;
        this.name = name;
        this.damage = damage;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public float getDamage() {
        return damage;
    }





    // public Card(int id, String name, String type, int damage, int element, int special)



}
