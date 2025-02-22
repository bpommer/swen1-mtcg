package edu.swen1.mtcg.utils;


import java.util.HashMap;

public interface IQueryBuilder {

    String buildParamQuery(String query, HashMap<String, String> parameters);

}
