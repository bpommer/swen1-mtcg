package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.services.db.models.BattleCard;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Map;

public class BattleCardFactory {

    public final Map<String, Integer> elementMap = Map.of(
            "Water", 2,
            "Fire", 3
    );

    public final Map<String, Integer> specialMap = Map.of(
            "Goblin", 1,
            "Dragon", 2,
            "Wizzard", 3,
            "Ork", 4,
            "Knight", 5,
            "Kraken", 6,
            "FireElf", 7

    );
    public final Map<String, Integer> cardTypeMap = Map.of(
            "Spell", 2
    );


    public BattleCard buildBattleCard(JSONObject rawCard) {

        BattleCard card = new BattleCard(rawCard);

        String name = rawCard.getString("Name");

        card.setElementId(inferElementFromName(name));
        card.setTypeId(inferCardTypeFromName(name));
        card.setSpecialId(inferSpecialFromName(name));

        return card;
    }

    private int inferElementFromName(String name) {

        for(String key : elementMap.keySet()) {
            if(name.contains(key)) {
                return elementMap.get(key);
            }
        }
        return 1;
    }


    private @Nullable Integer inferSpecialFromName(String name) {
        for(String key : specialMap.keySet()) {
            if(name.contains(key)) {
                return specialMap.get(key);
            }
        }
        return null;
    }

    private int inferCardTypeFromName(String name) {
        for(String key : cardTypeMap.keySet()) {
            if(name.contains(key)) {
                return cardTypeMap.get(key);
            }
        }
        return 1;
    }










}
