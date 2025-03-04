package edu.swen1.mtcg.services.battles;

import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.BattleCard;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.utils.BattleCardFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.Callable;

public class BattleLogic implements Callable<Response> {

    User player1;
    User player2;

    ArrayList<BattleCard> deckP1 = new ArrayList<>();
    ArrayList<BattleCard> deckP2 = new ArrayList<>();

    StringBuilder battleLog = new StringBuilder();

    public int effectScale = 0;

    public final Map<Integer, String> effectScript = Map.of(
            1, "\nThe spell was highly effective against the monster.\n",
            -1, "\nThe spell was not very effective against the monster\n"
    );

    public boolean specialEffect = false;


    // Infer properties from player cards and load decks
    public BattleLogic(User player1, User player2) {

        this.player1 = new User(player1);
        this.player2 = new User(player2);

        JSONArray deckP1JSON = new JSONArray(player1.getDeck());
        JSONArray deckP2JSON = new JSONArray(player2.getDeck());

        for(int i = 0; i < deckP1JSON.length(); i++) {
            JSONObject tempP1 = deckP1JSON.getJSONObject(i);
            BattleCard tempCard = BattleCardFactory.buildBattleCard(tempP1);
            deckP1.add(tempCard);
        }

        for(int i = 0; i < deckP1JSON.length(); i++) {
            JSONObject tempP2 = deckP2JSON.getJSONObject(i);
            BattleCard tempCard = BattleCardFactory.buildBattleCard(tempP2);
            deckP2.add(tempCard);
        }

    }

    public Response call() {




        for(int i = 0; i < 100; i++) {

            Random random = new Random();
            effectScale = 0;
            specialEffect = false;
            // Check for win condition
            if(deckP1.size() == 0) {
                battleLog.append(player2.getUsername() + " won the battle\n");
                return processPostBattle(player2, player1);
            } else if (deckP2.size() == 0) {
                battleLog.append(player1.getUsername() + " won the battle\n");
                return processPostBattle(player1, player2);
            }

            effectScale = 0;

            // Determine index of next card by random
            int p1Index = random.nextInt(deckP1.size());
            int p2Index = random.nextInt(deckP2.size());

            // Fetch card based on index
            BattleCard p1Card = new BattleCard(deckP1.get(p1Index));
            BattleCard p2Card = new BattleCard(deckP2.get(p2Index));

            // Log cards used in current round
            battleLog.append("\nRound " + (i+1) + ": ")
                    .append(player1.getUsername() + "'s ")
                    .append(p1Card.getName())
                    .append(" (" + p1Card.getDamage() + " damage)")
                    .append(" vs " + player2.getUsername() + "'s ")
                    .append(p2Card.getName())
                    .append(" (" + p2Card.getDamage() + " damage)")
                    .append("\n");



            // Modify damage based on special
            if(p1Card.getProperties().get("Special") != null
            || p2Card.getProperties().get("Special") != null) {
                specialEffect = true;
                ArrayList<BattleCard> newCards = processSpecial(p1Card, p2Card);
                p1Card = newCards.get(0);
                p2Card = newCards.get(1);
            }

            // Modify damage based on type

            if((!Objects.equals(p1Card.getTypeId(), p2Card.getTypeId()))) {
                if(p1Card.getProperties().get("Type").equals("Spell")
                        && p2Card.getProperties().get("Type").equals("Monster")) {

                    HashMap<String, BattleCard> newCards = processTypes(p2Card, p1Card);
                    p1Card = newCards.get("Spell");
                    p2Card = newCards.get("Monster");

                }
                else if (p1Card.getProperties().get("Type").equals("Monster")
                        && p2Card.getProperties().get("Type").equals("Spell")) {

                    HashMap<String, BattleCard> newCards = processTypes(p1Card, p2Card);
                    p1Card = newCards.get("Monster");
                    p2Card = newCards.get("Spell");

                }

            }

            // After modification, execute battle based on damage
            if(p1Card.getDamage() > p2Card.getDamage()) {
                battleLog.append("\n").append(player1.getUsername() + "'s ")
                        .append(p1Card.getName())
                        .append(" defeates ")
                        .append(player2.getUsername() + "'s ")
                        .append(p2Card.getName())
                        .append("\n");

                if(effectScale != 0 && !specialEffect) {
                    battleLog.append(effectScript.get(effectScale));
                }


                battleLog.append("\n").append(player1.getUsername()).append(" wins round " + (i+1) + "\n");

                BattleCard rCard = deckP2.remove(p2Index);
                deckP1.add(rCard);

            }
            else if(p1Card.getDamage() < p2Card.getDamage()) {
                battleLog.append("\n").append(player2.getUsername() + "'s ")
                        .append(p2Card.getName()).append(" defeates ")
                        .append(player1.getUsername() + "'s ")
                        .append(p1Card.getName())
                        .append("\n");

                battleLog.append("\n").append(player2.getUsername()).append(" wins round " + (i+1) + "\n");

                if(effectScale != 0 && !specialEffect) {
                    battleLog.append(effectScript.get(effectScale));
                }

                BattleCard rCard = deckP1.remove(p1Index);
                deckP2.add(rCard);

            }
            else {
                battleLog.append("\n").append("Round " + (i+1) + " ends with a tie\n");
            }

        }

        return processTie(player1, player2);

    }


    public ArrayList<BattleCard> processSpecial(BattleCard card1, BattleCard card2) {

        boolean specialFound = false;

        String card1Special = card1.getProperties().get("Special");
        String card2Special = card2.getProperties().get("Special");

        if(card1Special != null) {
            switch(card1Special) {

                case "Goblin":
                    if(card2Special != null && card2Special.equals("Dragon")) {
                        card1.setDamage(0);
                        battleLog.append("\nThe fearsome dragon makes the goblin run for its life!\n");
                    }
                    specialFound = true;
                    break;
                case "Wizzard":
                    if(card2Special != null && card2Special.equals("Ork")) {
                        card2.setDamage(0);
                        battleLog.append("\nWizzard has complete control over the ork!\n");

                    }
                    specialFound = true;
                    break;
                case "Knight":
                    if(card2.getProperties().get("Element").equals("Water")
                            && card2.getProperties().get("Type").equals("Spell")) {
                        card1.setDamage(0);
                        battleLog.append("\nThe knight drowned in the waves of the water spell!\n");
                    }
                    specialFound = true;
                    break;
                case "Kraken":
                    if(card2.getProperties().get("Type").equals("Spell")) {
                        card2.setDamage(0);
                        battleLog.append("\nSpells of this caliber are of no use against the kraken!\n");

                    }
                    specialFound = true;
                    break;
                case "FireElf":
                    if(card2Special != null && card2Special.equals("Dragon")) {
                        battleLog.append("\nThe Fire elfs precognition makes the dragon miss its attacks!\n");
                        card2.setDamage(0);
                    }
                    specialFound = true;
                    break;
                default:
                    break;
            }



        }

        if(!specialFound && card2Special != null) {
            switch(card2.getProperties().get("Special")) {

                case "Goblin":
                    if(card1Special != null && card1Special.equals("Dragon")) {
                        card2.setDamage(0);
                        battleLog.append("\nThe fearsome dragon makes the goblin run for its life!\n");
                    }
                    break;
                case "Wizzard":
                    if(card1Special != null && card1Special.equals("Ork")) {
                        card1.setDamage(0);
                        battleLog.append("\nWizzard has complete control over the ork!\n");

                    }
                    break;
                case "Knight":
                    if(card1.getProperties().get("Element").equals("Water")
                            && card1.getProperties().get("Type").equals("Spell")) {
                        card2.setDamage(0);
                        battleLog.append("\nThe knight drowned in the waves of the water spell!\n");

                    }
                    break;
                case "Kraken":
                    if(card1.getProperties().get("Type").equals("Spell")) {
                        card1.setDamage(0);
                        battleLog.append("\nSpells of this caliber are of no use against the kraken!\n");

                    }
                    break;
                case "FireElf":
                    if(card1Special != null && card1Special.equals("Dragon")) {
                        card1.setDamage(0);
                        battleLog.append("\nThe Fire elfs precognition makes the dragon miss its attacks!\n");

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

    // Nodify damage based on element
    public HashMap<String, BattleCard> processTypes(BattleCard monsterCard, BattleCard spellCard) {

            BattleCard spellCardCopy = new BattleCard(spellCard);

            String monsterElement = monsterCard.getProperties().get("Element");
            String spellElement = spellCard.getProperties().get("Element");

            switch(monsterElement) {

                case "Normal":
                    if(spellElement.equals("Fire")) {
                        spellCardCopy.setDamage(spellCardCopy.getDamage() * 2);
                        effectScale++;
                    }
                    else if(spellElement.equals("Water")) {
                        if(spellCardCopy.getDamage() > 0) {
                            spellCardCopy.setDamage(spellCardCopy.getDamage() / 2);
                        }
                        effectScale--;
                    }
                    break;

                case "Fire":
                    if(spellElement.equals("Water")) {
                        spellCardCopy.setDamage(spellCardCopy.getDamage() * 2);
                        effectScale++;
                    }
                    else if(spellElement.equals("Normal")) {
                        if(spellCardCopy.getDamage() > 0) {
                            spellCardCopy.setDamage(spellCardCopy.getDamage() / 2);
                        }
                        effectScale--;
                    }
                    break;

                case "Water":
                    if(spellElement.equals("Fire")) {
                        spellCardCopy.setDamage(spellCardCopy.getDamage() * 2);
                        effectScale++;
                    }
                    else if(spellElement.equals("Normal")) {
                        if(spellCardCopy.getDamage() > 0) {
                            spellCardCopy.setDamage(spellCardCopy.getDamage() / 2);
                        }
                        effectScale--;
                    }
                    break;
            }

            HashMap<String, BattleCard> newCards = new HashMap<>();
            newCards.put("Monster", monsterCard);
            newCards.put("Spell", spellCardCopy);
            return newCards;




    }

    public Response processPostBattle(User victory, User defeat) {

        BattleController controller = new BattleController();
        return controller.updateElo(victory, defeat, battleLog.toString());


    }

    public Response processTie(User user1, User user2) {
        BattleController controller = new BattleController();
        return controller.updateTie(user1, user2, battleLog.toString());
    }




}
