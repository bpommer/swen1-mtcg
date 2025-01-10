package edu.swen1.mtcg.utils;

import org.json.JSONArray;
import org.json.JSONObject;

// Parse metadata from deck to simplify battle logic
public class BattleDeckBuilder extends DeckBuilder {

    public BattleDeckBuilder(JSONObject deck) {
        super(deck);

        JSONArray battleCards = new JSONArray(this.deck.getJSONArray("deck").toString());


        // Parse element, type and special metadata from card name
        for (int i = 0; i < battleCards.length(); i++) {

            JSONObject battleCard = new JSONObject(battleCards.getJSONObject(i).toString());
            String cardName = battleCard.getString("name");

            // Parse element
            if(cardName.contains("Water")) {
                battleCard.put("element", "water");
            } else if(cardName.contains("Fire")) {
                battleCard.put("element", "fire");
            } else {
                battleCard.put("element", "normal");
            }

            // Parse type
            if(cardName.contains("Spell")) {
                battleCard.put("type", "spell");
            } else {
                battleCard.put("type", "monster");
            }

            // Parse special
            if(cardName.contains("Goblin")) {
                battleCard.put("special", "goblin");
            } else if(cardName.contains("Dragon")) {
                battleCard.put("special", "dragon");
            } else if(cardName.equals("Wizzard")) {
                battleCard.put("special", "wizzard");
            } else if(cardName.equals("Ork")) {
                battleCard.put("special", "ork");
            } else if(cardName.equals("Knight")) {
                battleCard.put("special", "knight");
            } else if(cardName.equals("Kraken")) {
                battleCard.put("special", "kraken");
            } else if(cardName.equals("FireElf")) {
                battleCard.put("special", "fire_elf");
            } else {
                battleCard.put("special", "none");
            }
            battleCard.put("defeated", false);

            battleCards.put(battleCard);

        }
        this.deck.put("deck", new JSONArray(battleCards.toString()));
    }






}
