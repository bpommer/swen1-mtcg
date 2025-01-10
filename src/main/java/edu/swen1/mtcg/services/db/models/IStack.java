package edu.swen1.mtcg.services.db.models;

import org.json.JSONObject;

public interface IStack {
    JSONObject list();
    int add(JSONObject card);
    int remove(String id);
    boolean integrityCheck();
}
