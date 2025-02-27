package edu.swen1.mtcg.services.transactions;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.PackageRepository;
import edu.swen1.mtcg.utils.Controller;

public class TransactionsController extends Controller {
    public Response purchasePack(User user) {
        TransactionUnit transactionUnit = new TransactionUnit();
        try(transactionUnit) {
            if(user.getCoins() < 5) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT, "Not enough money for buying a card package");
            }
            Response res = new PackageRepository(transactionUnit).buyPack(user);
            if(res.getStatusCode() < 200 || res.getStatusCode() > 299) {
                transactionUnit.dbRollback();
            } else {
                transactionUnit.dbCommit();
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            transactionUnit.dbRollback();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"Internal Server Error\" }"
            );
        }




    }


}
