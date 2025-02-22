package edu.swen1.mtcg.services.battles;

import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BattlesService implements IService {

    List<Thread> threadList = new ArrayList<>();

    @Override
    public Response handleRequest(Request request) {


        CountDownLatch latch = new CountDownLatch(2);






        return null;
    }
    public synchronized void waitForOpponent() {
        threadList = new ArrayList<>();
    }



}
