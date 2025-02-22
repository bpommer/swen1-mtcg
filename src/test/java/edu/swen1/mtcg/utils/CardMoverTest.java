package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.services.db.models.Card;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CardMoverTest {

    JSONObject testMonster;
    JSONObject testSpell;
    JSONObject testWizard;
    JSONObject testDeck;
    JSONObject testStack;

    // Create JSON stubs for tests
    @BeforeEach
    void setup() {
        this.testMonster = new JSONObject();
        testMonster.put("id", 1);
        testMonster.put("name", "Big Goblin");
        testMonster.put("type", "Monster");
        testMonster.put("damage", 5);
        testMonster.put("element", "Normal");
        testMonster.put("special", "Goblin");

        this.testSpell = new JSONObject();
        testSpell.put("id", 2);
        testSpell.put("name", "Fire Meteor");
        testSpell.put("type", "Spell");
        testSpell.put("damage", 4);
        testSpell.put("element", "Fire");
        testSpell.put("special", JSONObject.NULL);

        this.testWizard = new JSONObject();
        testWizard.put("id", 3);
        testWizard.put("name", "Merric the Sorcerer");
        testWizard.put("type", "Monster");
        testWizard.put("damage", 3);
        testWizard.put("element", "Water");
        testWizard.put("special", "Wizard");

        JSONArray deckArray = new JSONArray();
        deckArray.put(testMonster);
        deckArray.put(testSpell);
        deckArray.put(testWizard);

        JSONArray stackArray = new JSONArray();

        JSONObject spellClone = new JSONObject(testSpell.toString());
        JSONObject monsterClone = new JSONObject(testMonster.toString());
        JSONObject wizardClone = new JSONObject(testWizard.toString());

        spellClone.put("count", 3);
        monsterClone.put("count", 4);
        wizardClone.put("count", 2);

        stackArray.put(spellClone);
        stackArray.put(monsterClone);
        stackArray.put(wizardClone);


        this.testStack = new JSONObject();
        testStack.put("stack", stackArray);


        this.testDeck = new JSONObject();
        testDeck.put("deck", deckArray);

        System.out.println("Deck: " + testDeck.toString() + "\n");
        System.out.println("Stack: " + testStack.toString() + "\n");

    }

    /*@Test
    void moveCardFromDeck() {



        CardMover mover = new CardMover(testDeck, testStack);

        mover.moveCard(1, this.testDeck, this.testStack);
        testDeck = mover.fetchDeck();
        testStack = mover.getStack();

        System.out.println("Deck: " + testDeck.toString() + "\n");
        System.out.println("Stack: " + testStack.toString() + "\n");

        assertEquals(3, (testStack.getJSONArray("stack")).length());
        assertEquals(2, (testDeck.getJSONArray("deck")).length());

        assertEquals(-1, mover.moveCard(5, this.deck, this.stack));




    }

    @Test
    void moveCardFromStack() {

        CardMover mover = new CardMover(testDeck, testStack);

        int test = mover.stackToDeck(3);

        assertEquals(0, test);
        assertEquals(3, mover.getStack().getJSONArray("stack").length());
        assertEquals(4, mover.fetchDeck().getJSONArray("deck").length());





        JSONObject newStack = new JSONObject(mover.getStack().toString());

        System.out.println("Stack: " + newStack.toString() + "\n");



        JSONObject wizardCount = new JSONObject(mover.getStack().toString());

        assertEquals(1, wizardCount.getJSONArray("stack").getJSONObject(2).getInt("count"));


    }

    // Check if card with corresponding ID exists in stack and deck
    @Test
    void cardExists() {

        CardMover mover = new CardMover(testDeck, testStack);

        assertTrue(mover.cardExists(1, mover.fetchDeck()) > 0);
        assertFalse(mover.cardExists(5, mover.fetchDeck()) > 0);

    }*/
















}
