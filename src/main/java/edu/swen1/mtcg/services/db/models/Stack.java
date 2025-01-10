package edu.swen1.mtcg.services.db.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Stack implements IStack {

    JSONArray cards;
    private final String key;
    private boolean checkPassed = false;
    private String[] cardParams;

    public Stack(JSONObject source, StackType type) {

        
        switch (type) {
            case STACK:
                this.key = "stack";
                this.cardParams = new String[]{"Id", "Name", "Damage", "Count"};
                break;
            case DECK:
                this.key = "deck";
                this.cardParams = new String[]{"Id", "Name", "Damage"};
                break;
            case TRADE:
                this.key = "trades";
                this.cardParams = new String[]{"Id", "CardToTrade", "Type", "MinimumDamage"};
                break;
            case BATTLEDECK:
                this.key = "battledeck";
                this.cardParams = new String[]{"Id", "CardToTrade", "Type", "MinimumDamage"};

                break;
            default:
                this.key = "unknown";
                this.cards = new JSONArray();
                return;
        }
        this.cards = new JSONArray(source.getJSONArray(key).toString());

        if(!integrityCheck()) {
            this.cards = new JSONArray();
            return;
        }


    }

    @Override
    public JSONObject list() {
        JSONObject result = new JSONObject();
        result.put(this.key, cards);
        return result;
    }

    @Override
    public int add(JSONObject card) {

        if(!integrityCheck()) {
            return -1;
        }
        this.cards.put(card);
        return 0;
    }

    @Override
    public int remove(String id) {

        JSONArray tempArray = new JSONArray(cards.toString());
        for(int i = 0; i < this.cards.length(); i++) {

            if(tempArray.getJSONObject(i).getString(this.key).equals(id)) {
                tempArray.remove(i);
                this.cards = tempArray;
                return 0;
            }
        }
        return -1;
    }


    // Check if JSON format is valid
    @Override
    public boolean integrityCheck() {

        for(int i = 0; i < cards.length(); i++) {

            JSONObject card = cards.getJSONObject(i);

            for (int j = 0; j < this.cardParams.length; j++) {
                if(!(card.has(this.cardParams[j]))) {
                    return false;
                }
            }
        }
        return true;
    }

    // Overload function to accommodate checking a card in transit
    public boolean integrityCheck(JSONObject targetCard) {
        for (int j = 0; j < this.cardParams.length; j++) {
            if(!(targetCard.has(this.cardParams[j]))) {
                return false;
            }
        }
        return true;
    }





}
