package co.hackdevmentalists.abhinav.testapp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sockets {
    private static final String TAG = "CHECK";
    String clientSentence ;

    public static void main(String[] args) {
        new Sockets().startServer();
    }
    public void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(5657);
                    System.out.println("Waiting for clients to connect...");
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        clientProcessingPool.submit(new ClientTask(clientSocket));
                        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                        clientSentence = inFromClient.readLine();
                        System.out.println("Received: " + clientSentence);

                        /* close the socket and resume */
				/* listening for connections */
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Unable to process client request");
                    e.printStackTrace();
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

}


