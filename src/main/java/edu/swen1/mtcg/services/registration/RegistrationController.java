package edu.swen1.mtcg.services.registration;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.repository.SessionRepository;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.utils.Controller;


public class RegistrationController extends Controller {

    // POST /users
    public Response register(String username, String password) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {

            Response res = new SessionRepository(transactionUnit).registerUser(username, password);

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
    // GET /users/{username}
    public Response getUser(String username) {
        try {
            User res = SessionRepository.fetchUserFromName(username);
            if(res != null) {
                return new Response(HttpStatus.OK, ContentType.JSON, res.getUserData().toString());
            } else {
                return new Response(HttpStatus.NOT_FOUND, ContentType.TEXT, "User not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"Internal Server Error\" }"
            );
        }

    }

    // PUT /users/{username}
    public Response updateUser(String username, String newName, String newBio, String newImage) {
        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {
            Response res = new SessionRepository(transactionUnit).updateUser(username, newName, newBio, newImage);
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
