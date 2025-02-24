package edu.swen1.mtcg.services.db.models;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

import static edu.swen1.mtcg.services.db.repository.CardDataRepository.getBattleProperties;


public class BattleCard extends Card {

    private String type;
    private String special;
    private String element;

    @Getter
    @Setter
    private int typeId;
    @Getter
    @Setter
    private Integer specialId;
    @Getter
    @Setter
    private int elementId;

    public BattleCard(String id, String name, float damage) {
        super(id, name, damage);
    }

    public BattleCard(JSONObject cardJson) {
        super(cardJson);
    }


    public BattleCard(String id, String name, float damage, String element, String cardType, String special) {
        super(id, name, damage);

        this.type = cardType;
        this.element = element;
        this.special = special;
    }



}
