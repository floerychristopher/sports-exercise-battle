package com.seb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 10001;
    private final ExecutorService threadPool;
    private boolean running;

    public Server() {
        // Thread pool for handling client connections
        this.threadPool = Executors.newFixedThreadPool(10);
        this.running = false;
    }

    public void start() {
        running = true;
        ServerSocket serverSocket = null;

        try {
            // Initialize server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("SEB Server started on port " + PORT);

            // Test DB connection
            if (com.seb.config.DatabaseConfig.getInstance().testConnection()) {
                System.out.println("Database connection successful!");
            } else {
                System.err.println("WARNING: Database connection failed!");
                System.exit(1);
            }

            // Loop for accepting client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // Submit client handling to thread pool
                    threadPool.submit(new RequestHandler(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + PORT);
            e.printStackTrace();
        } finally {
            System.out.println("SEB Server shutting down...");
            stop(serverSocket);
        }
    }

    public void stop(ServerSocket serverSocket) {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }

        threadPool.shutdown();
        System.out.println("Server stopped");
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}