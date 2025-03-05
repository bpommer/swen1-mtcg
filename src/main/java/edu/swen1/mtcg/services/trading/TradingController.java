package edu.swen1.mtcg.services.trading;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.TradingDeal;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.TradeRepository;
import edu.swen1.mtcg.utils.Controller;
import org.json.JSONObject;

public class TradingController extends Controller {



    public Response getTrades() {
        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {
            Response res = new TradeRepository(transactionUnit).getTradeListings();
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

    public Response makeTrade(User user, JSONObject card, String tradeId) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {
            Response res = new TradeRepository(transactionUnit).executeTrade(user, card, tradeId);
            if(res.getStatusCode() < 200 || res.getStatusCode() > 299) {
                transactionUnit.dbRollback();
            } else {
                transactionUnit.dbCommit();
            }
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




    public Response revokeTradeOffer(User user, String tradeId) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {
            Response res = new TradeRepository(transactionUnit).deleteTrade(user, tradeId);
            if(res.getStatusCode() < 200 || res.getStatusCode() > 299) {
                transactionUnit.dbRollback();
            } else {
                transactionUnit.dbCommit();
            }
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




}
