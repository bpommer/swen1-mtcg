package edu.swen1.mtcg.utils;

import edu.swen1.mtcg.http.ContentType;
import edu.swen1.mtcg.http.HttpStatus;
import edu.swen1.mtcg.server.Request;
import edu.swen1.mtcg.server.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class RequestHandler implements Runnable {

    private Socket socket;
    private Router router;
    private PrintWriter out;
    private BufferedReader in;

    public RequestHandler(Socket socket, Router router) throws IOException {
        this.socket = socket;
        this.router = router;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.out = new PrintWriter(this.socket.getOutputStream(), true);

    }



    @Override
    public void run() {
        try {
            Response newResponse;
            Request newRequest = new RequestBuilder().buildRequest(this.in);


            if(newRequest.getPath() == null) {
                newResponse = new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "[]"
                );
                System.out.println("Call bad request");

            } else {
                System.out.println("Call default request");

                newResponse = this.router.getService(newRequest.getRoute())
                        .handleRequest(newRequest);

            }

            out.write(newResponse.getMessage());


        }
        catch (IOException e) {
            System.err.println(Thread.currentThread().getName() + "Error: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
