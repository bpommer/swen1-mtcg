package edu.swen1.mtcg.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class RequestSchemaChecker {

    public static boolean JsonKeyValueCheck(String target, SchemaWhitelists schema) {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode targetNode = null;

        // Map JSON to node
        try {
            targetNode = mapper.readTree(target);
        } catch (JsonProcessingException e) {
            return false;
        }

        // Extract keys into hashset
        Set<String> JsonKeySet = new HashSet<>();
        Iterator<String> jsonKeys = targetNode.fieldNames();
        while (jsonKeys.hasNext()) {
            JsonKeySet.add(jsonKeys.next());
        }

        // Fetch keyset from whitelist and compare to JsonKeySet
        Map<String, FieldValueType> wl = schema.whitelist;
        HashMap<String, FieldValueType> schemaMap = new HashMap<String, FieldValueType>(wl);
        if(!schemaMap.keySet().equals(JsonKeySet)) {
            return false;
        }

        // Check if value type for each key matches whitelist value type
        for(String key : JsonKeySet) {
            switch(schemaMap.get(key)) {
                case STRING:
                    if(!targetNode.get(key).isTextual() || targetNode.get(key).asText().isEmpty()) {

                        return false;
                    }
                    break;
                case INTEGER:
                    if(!targetNode.get(key).isInt()) {
                        return false;
                    }
                    break;
                case FLOAT:
                    if(!targetNode.get(key).isFloat()) {
                        return false;
                    }
                    break;
                case BOOLEAN:
                    if(!targetNode.get(key).isBoolean()) {
                        return false;
                    }
                    break;
                case NUMBER:
                    if(!targetNode.get(key).isNumber()) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;

    }



}
