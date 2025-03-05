package edu.swen1.mtcg.services.db.models;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;


public class BattleCard extends Card {


    @Getter @Setter
    private Integer typeId;
    @Getter @Setter
    private Integer specialId;
    @Getter @Setter
    private Integer elementId;
    @Getter
    private HashMap<String, String> properties = new HashMap<>();

    public BattleCard(String id, String name, float damage) {
        super(id, name, damage);
    }

    public BattleCard(BattleCard card) {
        super(card.getId(), card.getName(), card.getDamage());
        this.typeId = card.getTypeId();
        this.specialId = card.getSpecialId();
        this.elementId = card.getElementId();
        this.properties = card.getProperties();
    }

    public BattleCard(String id, String type, String name,
                      float damage, String element, String special) {
        super(id, name, damage);
        properties.put("Type", type);
        if(special != null) {
            properties.put("Special", special);
        } else {
            properties.put("Special", null);
        }
        properties.put("Element", element);
    }

    public JSONObject cardExtendedInfo() {
        JSONObject json = this.toJSON();
        json.put("Type", properties.get("Type"));
        if(properties.get("Special") != null) {
            json.put("Special", properties.get("Special"));
        } else {
            json.put("Special", JSONObject.NULL);
        }
        json.put("Element", properties.get("Element"));
        return json;
    }



    public BattleCard(JSONObject cardJson) {
        super(cardJson);
    }

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }




}
