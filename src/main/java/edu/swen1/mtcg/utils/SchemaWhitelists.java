package edu.swen1.mtcg.utils;

import java.util.Map;

public enum SchemaWhitelists {
    USER_CREDENTIALS(Map.ofEntries(
            Map.entry("Username", FieldValueType.STRING),
            Map.entry("Password", FieldValueType.STRING)
    )),
    USER_DATA(Map.ofEntries(
            Map.entry("Name", FieldValueType.STRING),
            Map.entry("Bio", FieldValueType.STRING),
            Map.entry("Image", FieldValueType.STRING)
    )),
    // Not applicable
    /*USER_STATS(Map.ofEntries(
            Map.entry("Name", FieldValueType.STRING),
            Map.entry("Elo", FieldValueType.INTEGER),
            Map.entry("Wins", FieldValueType.INTEGER),
            Map.entry("Losses", FieldValueType.INTEGER)
    )),*/
    CARD(Map.ofEntries(
            Map.entry("Id", FieldValueType.STRING),
            Map.entry("Name", FieldValueType.STRING),
            Map.entry("Damage", FieldValueType.NUMBER)

    )),
    TRADEDEAL(Map.ofEntries(
            Map.entry("Id", FieldValueType.STRING),
            Map.entry("CardToTrade", FieldValueType.STRING),
            Map.entry("Type", FieldValueType.STRING),
            Map.entry("MinimumDamage", FieldValueType.NUMBER)
    ));

    public final Map<String, FieldValueType> whitelist;

    SchemaWhitelists(Map<String, FieldValueType> set) { this.whitelist = set; }

}
