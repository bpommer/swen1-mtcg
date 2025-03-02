package edu.swen1.mtcg.services.battles;

import edu.swen1.mtcg.services.db.models.BattleCard;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.utils.BattleCardFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class BattleLogic {

    User player1;
    User player2;

    ArrayList<BattleCard> deckP1;
    ArrayList<BattleCard> deckP2;

    StringBuilder battleLog = new StringBuilder();

    public boolean specialLogSet = false;


    // Infer properties from player cards and load decks
    public BattleLogic(User player1, User player2) {
        this.player1 = player1;
        this.player2 = player2;

        BattleCardFactory factory = new BattleCardFactory();

        JSONArray deckP1JSON = new JSONArray(this.player1.getDeck());
        JSONArray deckP2JSON = new JSONArray(this.player2.getDeck());

        for(int i = 0; i < deckP1JSON.length(); i++) {
            JSONObject tempP1 = deckP1JSON.getJSONObject(i);
            BattleCard tempCard = factory.buildBattleCard(tempP1);
            deckP1.add(tempCard);
        }

        for(int i = 0; i < deckP1JSON.length(); i++) {
            JSONObject tempP2 = deckP2JSON.getJSONObject(i);
            BattleCard tempCard = factory.buildBattleCard(tempP2);
            deckP2.add(tempCard);
        }

    }

    public void startBattle() {

        Random random = new Random();


        for(int i = 0; i < 100; i++) {

            // Check for win condition
            if(deckP1.isEmpty()) {
                break;
            } else if(deckP2.isEmpty()) {
                break;
            }

            int p1Index = random.nextInt(deckP1.size());
            int p2Index = random.nextInt(deckP2.size());

            BattleCard p1Card = new BattleCard(deckP1.get(p1Index));
            BattleCard p2Card = new BattleCard(deckP2.get(p2Index));

            // Modify damage based on special
            if(p1Card.getProperties().get("Special") != null
            || p2Card.getProperties().get("Special") != null) {

                ArrayList<BattleCard> newCards = processSpecial(p1Card, p2Card);
                p1Card = newCards.get(0);
                p2Card = newCards.get(1);
            }

            if((!Objects.equals(p1Card.getElementId(), p2Card.getElementId()))
            && (p1Card.getProperties().get("Type").equals("Spell")
            || p2Card.getProperties().get("Type").equals("Spell")
            )) {

                ArrayList<BattleCard> newCards = processTypes(p1Card, p2Card);
                p1Card = newCards.get(0);
                p2Card = newCards.get(1);
            }





        }
    }


    public ArrayList<BattleCard> processSpecial(BattleCard card1, BattleCard card2) {

        boolean specialFound = false;

        switch(card1.getProperties().get("Special")) {

            case "Goblin":
                if(card2.getProperties().get("Special").equals("Dragon")) {
                    card1.setDamage(0);
                }
                specialFound = true;
                break;
            case "Wizzard":
                if(card2.getProperties().get("Special").equals("Ork")) {
                    card2.setDamage(-1);
                }
                specialFound = true;
                break;
            case "Knight":
                if(card2.getProperties().get("Element").equals("Water")
                && card2.getProperties().get("Type").equals("Spell")) {
                    card1.setDamage(-1);
                }
                specialFound = true;
                break;
            case "Kraken":
                if(card2.getProperties().get("Type").equals("Spell")) {
                    card2.setDamage(-1);
                }
                specialFound = true;
                break;
            case "FireElf":
                if(card2.getProperties().get("Special").equals("Dragon")) {
                    card2.setDamage(-1);
                }
                specialFound = true;
                break;
            default:
                break;
        }
        if(!specialFound) {

            switch(card2.getProperties().get("Special")) {

                case "Goblin":
                    if(card1.getProperties().get("Special").equals("Dragon")) {
                        card2.setDamage(0);
                    }
                    break;
                case "Wizzard":
                    if(card1.getProperties().get("Special").equals("Ork")) {
                        card1.setDamage(-1);
                    }
                    break;
                case "Knight":
                    if(card1.getProperties().get("Element").equals("Water")
                            && card1.getProperties().get("Type").equals("Spell")) {
                        card2.setDamage(-1);
                    }
                    break;
                case "Kraken":
                    if(card1.getProperties().get("Type").equals("Spell")) {
                        card1.setDamage(-1);
                    }
                    break;
                case "FireElf":
                    if(card1.getProperties().get("Special").equals("Dragon")) {
                        card1.setDamage(-1);
                    }
                    break;
                default:
                    break;
            }
        }

        ArrayList<BattleCard> newCards = new ArrayList<>();
        newCards.add(card1);
        newCards.add(card2);
        return newCards;
    }


    public ArrayList<BattleCard> processTypes(BattleCard card1, BattleCard card2) {

        if(card1.getProperties().get("Type").equals("Spell")) {

            switch(card1.getProperties().get("Element")) {
                case "Normal":
                    if(card2.getProperties().get("Element").equals("Water")) {
                        card1.setDamage(card1.getDamage() * 2);
                    }
                    else if(card2.getProperties().get("Element").equals("Fire")) {

                        if(card1.getDamage() != 0) {
                            card1.setDamage(card1.getDamage() / 2);
                        }

                    }


            }



        } else if(card2.getProperties().get("Type").equals("Spell")) {

        }

        return null;


    }




}
