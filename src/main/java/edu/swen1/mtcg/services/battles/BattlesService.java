package edu.swen1.mtcg.services.battles;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.http.RestMethod;
import edu.swen1.mtcg.server.IService;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;
import edu.swen1.mtcg.services.db.models.User;
import edu.swen1.mtcg.services.db.repository.SessionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;


public class BattlesService implements IService {
    private final Object lock = new Object();
    private List<User> validatedUsers = new ArrayList<>();
    private final CyclicBarrier barrier = new CyclicBarrier(2);
    private final Semaphore queueSemaphore = new Semaphore(2);
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private Callable<Response> battlesCall = null;
    private Future<Response> battlesFuture = null;


    @Override
    public Response handleRequest(Request request) {

        if(request.getMethod() == RestMethod.POST) {

            try {
                // Let only two threads pass and block the rest

                synchronized (lock) {
                    try {
                        queueSemaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Thread interrupted.\n");
                    }

                }

                // Check for each thread if requirements are met to enter battle
                // Release a semaphore permit if checks fail for the user
                synchronized (lock) {
                    User foundUser = SessionRepository.fetchUserFromToken(request.getHeaderMap().getAuthHeader());
                    if (foundUser == null) {
                        queueSemaphore.release();
                        return new Response(HttpStatus.UNAUTHORIZED, ContentType.TEXT, "Token missing or invalid.\n");
                    }

                    if(foundUser.getDeck().isEmpty() || foundUser.getDeck().length() != 4) {
                        queueSemaphore.release();
                        return new Response(HttpStatus.FORBIDDEN, ContentType.TEXT, "Deck does not contain 4 cards.\n");
                    }
                    validatedUsers.add(foundUser);
                }
                // Wait for two threads to arrive
                barrier.await();

                // One thread initializes callable
                // and remove validated users from list
                synchronized (lock) {
                    if(battlesCall == null) {
                        battlesCall = new BattleLogic(validatedUsers.get(0), validatedUsers.get(1));
                        sleep(200);
                        validatedUsers = new ArrayList<>();
                        battlesFuture = threadPool.submit(battlesCall);
                    }
                }

                // Make both threads wait for result
                Response result = battlesFuture.get();

                // Clean up callable and future for next duel
                // and release queue semaphore
                battlesCall = null;
                battlesFuture = null;

                queueSemaphore.release(2);

                return result;


            } catch(Exception e) {
                e.printStackTrace();
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.TEXT, "Internal server error.\n");
            }
        }
        return new Response(HttpStatus.NOT_IMPLEMENTED, ContentType.TEXT, "Not implemented.\n");

    }




}
