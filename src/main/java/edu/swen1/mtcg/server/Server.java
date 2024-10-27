package edu.swen1.mtcg.server;

import edu.swen1.mtcg.utils.RequestHandler;
import edu.swen1.mtcg.utils.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private int port;
    private Router router;
    public Server(int port, Router router) {
        this.port = port;
        this.router = router;
    }


    public void start() throws IOException {
        final ExecutorService executor = Executors.newFixedThreadPool(10);

        System.out.println("Starting server on port " + this.port);
        System.out.println("Server running at http://localhost:" + this.port);

        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            while(true) {
                final Socket clientConnection = serverSocket.accept();
                final RequestHandler socketHandler = new RequestHandler(clientConnection, this.router);
                System.out.println("New client connection from " + clientConnection.getRemoteSocketAddress());
                executor.submit(socketHandler);
            }

        }

    }

}
