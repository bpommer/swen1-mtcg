package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.services.db.models.BattleCard;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BattleCardFactoryTest {

    BattleCardFactory factory = new BattleCardFactory();

    JSONObject testMonster1 = new JSONObject().put("Name", "test").put("Id", "asdfascvv").put("Damage", 123);

    JSONObject testMonster2 = new JSONObject()
            .put("Name", "Dragon").put("Id", "9e8238a4-8a7a-487f-9f7d-a8c97899eb48").put("Damage", 70.0);



    JSONObject testMonster3 = new JSONObject()
            .put("Name", "WaterGoblin").put("Id", "b2237eca-0271-43bd-87f6-b22f70d42ca4").put("Damage", 11.0);


    JSONObject testSpell1 = new JSONObject()
            .put("Name", "FireSpell").put("Id", "dcd93250-25a7-4dca-85da-cad2789f7198").put("Damage", 23.0);


    @Test
    public void defaultInference() {
        BattleCard testCard = factory.buildBattleCard(testMonster1);
        assertEquals("Normal", testCard.getProperties().get("Element"));
        assertNull(testCard.getProperties().get("Special"));
        assertEquals("Monster", testCard.getProperties().get("Type"));

    }

    @Test
    public void specialMonsterDefaultType() {
        BattleCard testCard = factory.buildBattleCard(testMonster2);
        assertEquals("Normal", testCard.getProperties().get("Element"));
        assertEquals("Dragon", testCard.getProperties().get("Special"));
        assertEquals("Monster", testCard.getProperties().get("Type"));
    }

    @Test
    public void testElementInference() {
        BattleCard testCard = factory.buildBattleCard(testMonster3);
        assertEquals("Water", testCard.getProperties().get("Element"));
        assertEquals("Goblin", testCard.getProperties().get("Special"));
        assertEquals("Monster", testCard.getProperties().get("Type"));
    }

    @Test
    public void testCardTypeInference() {
        BattleCard testCard = factory.buildBattleCard(testSpell1);
        assertEquals("Fire", testCard.getProperties().get("Element"));
        assertNull(testCard.getProperties().get("Special"));
        assertEquals("Spell", testCard.getProperties().get("Type"));

    }



}
