package edu.swen1.mtcg.services.trading;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.CardDataRepository;
import edu.swen1.mtcg.services.db.repository.TradeRepository;
import edu.swen1.mtcg.utils.Controller;

public class TradingController extends Controller {



    public Response getTradeListings() {
        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {
            Response res = new CardDataRepository(transactionUnit).getTradeData();
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"Internal Server Error\" }"
            );
        }
    }

    public Response newTradeOffer(User user, TradingDeal deal) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try {
            Response res = new TradeRepository(transactionUnit).createNewTrade(user, deal);

            // Rollback db if status code is not of success type
            if(res.getStatusCode() < 200 || res.getStatusCode() > 299) {
                transactionUnit.dbRollback();
            } else {
                transactionUnit.dbCommit();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            transactionUnit.dbRollback();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal Server Error");
        }


    }



    public Response revokeTradeOffer(User user, Request request) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try {
            Response res = new TradeRepository(transactionUnit).deleteTrade(user, )
        }




        return null;
    }




}
