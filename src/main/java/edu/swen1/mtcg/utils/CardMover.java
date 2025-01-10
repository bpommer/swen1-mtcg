package edu.swen1.mtcg.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.smartcardio.CardException;


public class CardMover {

    private JSONObject deck;
    private JSONObject stack;


    public CardMover(JSONObject deck, JSONObject stack) {
        this.deck = deck;
        this.stack = stack;

    }

    public int cardExists(String id, JSONObject target) {

        JSONArray tempArray;

        if(target.has("deck")) {
            tempArray = target.getJSONArray("deck");
        }
        else if(target.has("stack")) {
            tempArray = target.getJSONArray("stack");
        }
        else {
            return -1;
        }

        for(int i = 0; i < tempArray.length(); i++) {
            JSONObject tempCard = tempArray.getJSONObject(i);
            if(tempCard.getString("id") == id) {
                return i;
            }
        }
        return -1;
    }

    public JSONObject takeCard(String id, JSONObject target) throws CardMoverException {

        JSONArray tempArray = null;
        boolean isStack = true;
        int cardIndex = cardExists(id, target);

        if(cardIndex == -1) {
            throw new CardMoverException("Card not found");
        }

        if (target.has("deck")) {

            if (this.deck.getJSONArray("deck").isEmpty()) {
                throw new CardMoverException("Target is empty");
            }

            tempArray = target.getJSONArray("deck");
            isStack = false;
        }
        else if (target.has("stack")) {

            if (this.stack.getJSONArray("stack").isEmpty()) {
                throw new CardMoverException("Target is empty");
            }
            tempArray = target.getJSONArray("stack");

        }
        else {
            throw new CardMoverException("Invalid target format");
        }

        JSONObject tempCard = new JSONObject(tempArray.getJSONObject(cardIndex).toString());

        if (isStack) {
            if(tempArray.getJSONObject(cardIndex).getInt("count") <= 1) {
                tempArray.remove(cardIndex);

            }
            else {
                int cardCount = tempCard.getInt("count");
                tempCard.put("count", cardCount - 1);
                tempArray.put(cardIndex, new JSONObject(tempCard.toString()));
            }
            this.stack.put("stack", tempArray);
            tempCard.remove("count");
        }
        else {
            tempArray.remove(cardIndex);
            this.deck.put("deck", tempArray);
        }
        return tempCard;
    }

    public int insertCard(JSONObject card, JSONObject target) throws CardMoverException {

        boolean isStack = false;
        JSONArray tempArray = null;

        if(target.has("deck")) {
            tempArray = target.getJSONArray("deck");

            if(tempArray.length() >= 4) {
                throw new CardMoverException("Deck is full");
            }

        }
        else if(target.has("stack")) {
            tempArray = target.getJSONArray("stack");
            isStack = true;
        }
        else {
            throw new CardMoverException("Invalid target format");
        }

        // Handle stack entry
        if(isStack) {

            int cardIndex = cardExists(card.getString("Id"), target);

            if(cardIndex == -1) {
                card.put("count", 1);
                tempArray.put(card);
            }
            else {
                int cardCount = tempArray.getJSONObject(cardIndex).getInt("count");
                card.put("count", cardCount + 1);
                tempArray.put(cardIndex, new JSONObject(tempArray.getJSONObject(cardIndex).toString()));
            }
            this.stack.put("stack", tempArray);
        }
        else {
            tempArray.put(card);
            this.deck.put("deck", tempArray);
        }
        return 0;
    }

    public int moveCard(String id, JSONObject source, JSONObject dest) throws Exception {

        // Prevent execution if keypairs are not valid

        JSONObject tempCard = null;

        try {
            tempCard = takeCard(id, source);
        } catch (CardMoverException e) {

            if(e.getMessage().equals("Card not found")) {

            }
            else if (e.getMessage().equals("Target is full")) {

            }

        }





        if(tempCard == null) {
            return -1;
        }

        if(insertCard(tempCard, dest) == -1) {



        }

        return 0;


    }







    public JSONObject getDeck() {
        return deck;
    }
    public JSONObject getStack() {
        return stack;
    }

    






}
