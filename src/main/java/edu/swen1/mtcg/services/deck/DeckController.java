package edu.swen1.mtcg.services.deck;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.DeckRepository;
import edu.swen1.mtcg.utils.Controller;

public class DeckController extends Controller {


    // PUT /deck
    public Response changeDeck(User user) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try {
            Response res = new DeckRepository(transactionUnit).updateDeck(user);
            if(res.getStatusCode() < 200 || res.getStatusCode() > 299) {
                transactionUnit.dbRollback();
            } else {
                transactionUnit.dbCommit();
            }
            return res;
        } catch (Exception e) {
            transactionUnit.dbRollback();
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal Server Error");
        }

    }





}
