package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.services.db.models.BattleCard;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Map;

public class BattleCardFactory {

    public static final Map<String, Integer> elementMap = Map.of(
            "Water", 2,
            "Fire", 3
    );

    public static final Map<String, Integer> specialMap = Map.of(
            "Goblin", 1,
            "Dragon", 2,
            "Wizzard", 3,
            "Ork", 4,
            "Knight", 5,
            "Kraken", 6,
            "FireElf", 7

    );
    public static final Map<String, Integer> cardTypeMap = Map.of(
            "Spell", 2
    );


    public static BattleCard buildBattleCard(JSONObject rawCard) {

        BattleCard card = new BattleCard(rawCard);

        String name = rawCard.getString("Name");

        inferCardTypeFromName(card, name);
        inferElementFromName(card, name);
        inferSpecialFromName(card, name);

        return card;
    }

    private static void inferElementFromName(BattleCard card, String name) {

        for(String key : elementMap.keySet()) {
            if(name.contains(key)) {
                card.putProperty("Element", key);
                card.setElementId(elementMap.get(key));
                return;
            }
        }
        card.putProperty("Element", "Normal");
        card.setElementId(1);
    }


    private static void inferSpecialFromName(BattleCard card, String name) {
        for(String key : specialMap.keySet()) {
            if(name.contains(key)) {
                card.putProperty("Special", key);
                card.setSpecialId(specialMap.get(key));
                return;
            }
        }
        card.setSpecialId(null);
    }

    private static void inferCardTypeFromName(BattleCard card, String name) {
        for(String key : cardTypeMap.keySet()) {
            if(name.contains(key)) {
                card.putProperty("Type", key);
                card.setTypeId(cardTypeMap.get(key));
                return;
            }
        }
        card.putProperty("Type", "Monster");
        card.setTypeId(1);
    }










}
