package edu.swen1.mtcg.services.db.models;

import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.utils.HashGenerator;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@Getter
@Setter
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

    public JSONObject toJSON() {
        JSONObject cardJson = new JSONObject();
        cardJson.put("Id", id);
        cardJson.put("Name", name);
        cardJson.put("Damage", damage);
        return cardJson;
    }

}
