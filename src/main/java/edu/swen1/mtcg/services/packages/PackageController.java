package edu.swen1.mtcg.services.packages;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.dbaccess.TransactionUnit;
import edu.swen1.mtcg.services.db.repository.PackageRepository;
import edu.swen1.mtcg.utils.Controller;
import org.json.JSONArray;

public class PackageController extends Controller {

    public Response addPackage(JSONArray pack) {

        TransactionUnit transactionUnit = new TransactionUnit();

        try(transactionUnit) {

            Response res = new PackageRepository(transactionUnit).registerPackage(pack);
            transactionUnit.dbCommit();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            transactionUnit.dbRollback();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"Internal Server Error\" }");

        }


    }


}
