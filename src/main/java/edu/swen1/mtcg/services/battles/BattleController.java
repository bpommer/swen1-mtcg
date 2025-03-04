package edu.swen1.mtcg.services.battles;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.Controller;

public class BattleController extends Controller {
    public Response updateElo(User victory, User defeat, String log) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try {
            Response res = new SessionRepository(transactionUnit).updateElo(victory, defeat, log);
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

    public Response updateTie(User user1, User user2, String log) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try {
            Response res = new SessionRepository(transactionUnit).updateTie(user1, user2, log);
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
