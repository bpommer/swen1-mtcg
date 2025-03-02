package edu.swen1.mtcg.services.db.models;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@Getter
public class TradingDeal {
    private String tradeid;
    private String cardid;
    private String type;
    private float mindamage;
    @Getter @Setter
    private Integer ownerId;
    @Getter @Setter
    private JSONObject targetCard = null;

    public TradingDeal(String tradeid, String cardid, String type, float mindamage) {
        this.tradeid = tradeid;
        this.cardid = cardid;
        this.type = type;
        this.mindamage = mindamage;
        this.ownerId = null;
    }

    public TradingDeal(String tradeid, String cardid, String type, float mindamage, int ownerid) {
        this.tradeid = tradeid;
        this.cardid = cardid;
        this.type = type;
        this.mindamage = mindamage;
        this.ownerId = ownerid;

    }



    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        json.put("Id", tradeid);
        json.put("CardToTrade", cardid);
        json.put("Type", type);
        json.put("MinimumDamage", mindamage);
        return json;
    }
}
