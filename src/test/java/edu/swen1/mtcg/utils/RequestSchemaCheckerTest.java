package edu.swen1.mtcg.utils;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestSchemaCheckerTest {

    String userCredentialsTest = "{\"Username\":\"altenhof\", \"Password\":\"markus\"}";
    // extra key
    String userCredentialsTest2 = "{\"Username\":\"altenhof\", \"Password\":\"markus\", \"extra\": \"e\"}";
    // missing key
    String userCredentialsTest3 = "{\"Username\":\"altenhof\"}";
    // invalid key
    String userCredentialsTest4 = "{\"Usernae\":\"altenhof\", \"Password\":\"markus\"}";
    // invalid value type
    String userCredentialsTest5 = "{\"Username\":\"altenhof\", \"Password\": true}";


    String userDataTest = "{\"Name\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}";
    // extra key
    String userDataTest2 = "{\"Name\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\", \"extra\": \"e\"}";
    // missing key
    String userDataTest3 = "{\"Bio\": \"me playin...\", \"Image\": \":-)\"}";
    // invalid key
    String userDataTest4 = "{\"Nam\": \"Kienboeck\",  \"Bio\": \"me playin...\", \"Image\": \":-)\"}";
    // invalid value type
    String userDataTest5 = "{\"Name\": 1,  \"Bio\": \"me playin...\", \"Image\": \":-)\"}";


    String cardTest = "{\"Id\":\"27051a20-8580-43ff-a473-e986b52f297a\", \"Name\":\"FireElf\", \"Damage\": 28.0}";
    // extra key
    String cardTest2 = "{\"Id\":\"27051a20-8580-43ff-a473-e986b52f297a\", \"Name\":\"FireElf\", \"Damage\": 28.0, \"Type\": \"spell\"}";
    // missing key
    String cardTest3 = "{\"Id\":\"27051a20-8580-43ff-a473-e986b52f297a\", \"Name\":\"FireElf\"}";
    // invalid key
    String cardTest4 = "{\"Id\":\"27051a20-8580-43ff-a473-e986b52f297a\", \"Name\":\"FireElf\", \"Damge\": 28.0, \"}";
    // invalid value type
    String cardTest5 = "{\"Id\":\"27051a20-8580-43ff-a473-e986b52f297a\", \"Name\":\"FireElf\", \"Damage\": \"test\"}";


    String tradingDealTest = "{\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"CardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Type\": \"monster\", \"MinimumDamage\": 15}";
    // extra key
    String tradingDealTest2 = "{\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"Damage\": 14, \"CardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Type\": \"monster\", \"MinimumDamage\": 15}";
    // missing key
    String tradingDealTest3 = "{\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"CardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"MinimumDamage\": 15}";
    // invalid key
    String tradingDealTest4 = "{\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"CardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Type\": \"monster\", \"MinimumDamage\": 15, \"Username\": \"test\"}";
    // invalid value type
    String tradingDealTest5 = "{\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\", \"CardToTrade\": 15, \"Type\": \"monster\", \"MinimumDamage\": 15}";


    @Test
    public void validObjects() {
        assertTrue(RequestSchemaChecker.JsonKeyValueCheck(userCredentialsTest, SchemaWhitelists.USER_CREDENTIALS));
        assertTrue(RequestSchemaChecker.JsonKeyValueCheck(userDataTest, SchemaWhitelists.USER_DATA));
        assertTrue(RequestSchemaChecker.JsonKeyValueCheck(cardTest, SchemaWhitelists.CARD));
        assertTrue(RequestSchemaChecker.JsonKeyValueCheck(tradingDealTest, SchemaWhitelists.TRADEDEAL));
    }

    @Test
    public void invalidObjects() {
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userCredentialsTest2, SchemaWhitelists.USER_CREDENTIALS));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userCredentialsTest3, SchemaWhitelists.USER_CREDENTIALS));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userCredentialsTest4, SchemaWhitelists.USER_CREDENTIALS));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userCredentialsTest5, SchemaWhitelists.USER_CREDENTIALS));

        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userDataTest2, SchemaWhitelists.USER_DATA));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userDataTest3, SchemaWhitelists.USER_DATA));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userDataTest4, SchemaWhitelists.USER_DATA));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userDataTest5, SchemaWhitelists.USER_DATA));

        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(cardTest2, SchemaWhitelists.CARD));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(cardTest3, SchemaWhitelists.CARD));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(cardTest4, SchemaWhitelists.CARD));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(cardTest5, SchemaWhitelists.CARD));

        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(tradingDealTest2, SchemaWhitelists.TRADEDEAL));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(tradingDealTest3, SchemaWhitelists.TRADEDEAL));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(tradingDealTest4, SchemaWhitelists.TRADEDEAL));
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(tradingDealTest5, SchemaWhitelists.TRADEDEAL));
    }

    @Test
    public void invalidUserCredentials() {
        JSONObject credentials = new JSONObject(userCredentialsTest);
        credentials.put("Username", 15);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(credentials.toString(), SchemaWhitelists.USER_CREDENTIALS));

        credentials = new JSONObject(userCredentialsTest);
        credentials.put("Password", true);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(credentials.toString(), SchemaWhitelists.USER_CREDENTIALS));
    }

    @Test
    public void invalidUserData() {
        JSONObject userdata = new JSONObject(userDataTest);
        userdata.put("Name", true);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userdata.toString(), SchemaWhitelists.USER_DATA));

        userdata = new JSONObject(userDataTest);
        userdata.put("Bio", 15);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userdata.toString(), SchemaWhitelists.USER_DATA));

        userdata = new JSONObject(userDataTest);
        userdata.put("Image", 12.34);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(userdata.toString(), SchemaWhitelists.USER_DATA));
    }

    @Test
    public void invalidCard() {
        JSONObject card = new JSONObject(cardTest);
        card.put("Id", false);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(card.toString(), SchemaWhitelists.CARD));

        card = new JSONObject(cardTest);
        card.put("Name", 15);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(card.toString(), SchemaWhitelists.CARD));

        card = new JSONObject(cardTest);
        card.put("Damage", true);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(card.toString(), SchemaWhitelists.CARD));
    }

    @Test
    public void invalidTradingDeal() {
        JSONObject trade = new JSONObject(tradingDealTest);
        trade.put("Id", false);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(trade.toString(), SchemaWhitelists.TRADEDEAL));

        trade = new JSONObject(tradingDealTest);
        trade.put("CardToTrade", 12);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(trade.toString(), SchemaWhitelists.TRADEDEAL));

        trade = new JSONObject(tradingDealTest);
        trade.put("Type", true);
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(trade.toString(), SchemaWhitelists.TRADEDEAL));

        trade = new JSONObject(tradingDealTest);
        trade.put("MinimumDamage", "test");
        assertFalse(RequestSchemaChecker.JsonKeyValueCheck(trade.toString(), SchemaWhitelists.TRADEDEAL));

    }
}
