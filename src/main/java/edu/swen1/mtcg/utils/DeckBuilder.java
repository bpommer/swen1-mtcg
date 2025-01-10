package edu.swen1.mtcg.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class DeckBuilder {

    JSONObject deck;

    public DeckBuilder(JSONObject inputDeck) {

        JSONArray cards = inputDeck.getJSONArray("deck");

        if(cards == null || cards.length() == 0) {
            inputDeck.put("deck", new JSONArray());
            return;
        }

        else {

            this.deck = new JSONObject();
            this.deck.put("deck", new JSONArray(cards.toString()));

        }

    }

    JSONObject getDeck() {return this.deck;}



}
