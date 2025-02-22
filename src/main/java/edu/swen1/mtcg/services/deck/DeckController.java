package edu.swen1.mtcg.services.deck;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.Deck;
import edu.swen1.mtcg.services.db.repository.DeckRepository;
import edu.swen1.mtcg.utils.Controller;
import org.json.JSONArray;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class DeckController extends Controller {


    // PUT /deck
    public Response changeDeck(JSONArray newDeck, JSONArray newStack, int userId) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try {
            Response res = new DeckRepository(transactionUnit).updateDeck(newDeck, newStack, userId);
            transactionUnit.dbCommit();
            return res;
        } catch (Exception e) {
            transactionUnit.dbRollback();
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal Server Error");
        }

    }

    public Response fetchDeck(int userId, HashMap<String, String> params) {
        TransactionUnit transactionUnit = new TransactionUnit();
        try {
            Response res = new DeckRepository(transactionUnit).getDeck(userId, params);
            return res;
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal Server Error");
        }

    }



}
