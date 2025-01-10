package edu.swen1.mtcg.services.db.models;

import org.json.JSONObject;

public class Deck extends Stack {


    public Deck(JSONObject source, StackType type) {
        super(source, type);
    }



}
