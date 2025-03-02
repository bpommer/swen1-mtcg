package edu.swen1.mtcg.services.db.models;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

import static edu.swen1.mtcg.services.db.repository.CardDataRepository.getBattleProperties;


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



    public BattleCard(JSONObject cardJson) {
        super(cardJson);
    }

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }




}
