package edu.swen1.mtcg.services.login;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.utils.Controller;

public class LoginController extends Controller {

    public Response login(User user){
        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {
            Response res = new SessionRepository(transactionUnit).loginUser(user);
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
