package edu.swen1.mtcg.services.db.models;

import java.util.HashMap;
import java.util.HashSet;

import static edu.swen1.mtcg.services.db.repository.CardDataRepository.getBattleProperties;


public class BattleCard extends Card {

    private String type;
    private String special;
    private String element;

    private int typeId = 0;
    private int specialId = 0;
    private int elementId = 0;


    public BattleCard(String id, String name, float damage, String element, String cardType, String special) {
        super(id, name, damage);

        this.type = cardType;
        this.element = element;
        this.special = special;
    }

    // Infer battle relevant data from regular entry

    public BattleCard(String id, String name, float damage) {
        super(id, name, damage);

        HashMap<String, Integer> typeList = getBattleProperties("cardtype");
        HashMap<String, Integer> specialList = getBattleProperties("specialtype");
        HashMap<String, Integer> elementList = getBattleProperties("element");
        boolean typeFound = false;
        boolean specialFound = false;
        boolean elementFound = false;

        if(typeList == null || specialList == null || elementList == null) {
            return;
        }


        for(String type : typeList.keySet()) {
            if(name.contains(type)) {
                this.type = type;
                this.typeId = typeList.get(type);
                typeFound = true;
                break;
            }
        }
        if(!typeFound) {
            this.type = "Monster";
            this.typeId = 1;
        }


        for(String specialType : specialList.keySet()) {
            if(name.contains(specialType)) {
                this.special = specialType;
                this.specialId = specialList.get(specialType);
                specialFound = true;
                break;
            }
        }
        if(!specialFound) {
            this.special = null;
            this.specialId = 0;
        }

        for(String element : elementList.keySet()) {
            if(name.contains(element)) {
                this.element = element;
                this.elementId = elementList.get(element);
                elementFound = true;
                break;
            }
        }
        if(!elementFound) {
            this.element = "Normal";
            this.elementId = 1;
        }









    }

    public int getTypeId() {return this.typeId;}
    public int getSpecialId() {return this.specialId;}
    public int getElementId() {return this.elementId;}




}
