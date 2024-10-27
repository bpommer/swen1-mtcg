package edu.swen1.mtcg;

import edu.swen1.mtcg.server.Server;
import edu.swen1.mtcg.utils.Router;

import edu.swen1.mtcg.services.registration.RegistrationService;
import edu.swen1.mtcg.services.login.LoginService;

import java.io.IOException;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Server server = new Server(10001, setupRouter());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Router setupRouter() {

        Router router = new Router();
        router.newService("/users", new RegistrationService());
        router.newService("/sessions", new LoginService());

        return router;

    }

}