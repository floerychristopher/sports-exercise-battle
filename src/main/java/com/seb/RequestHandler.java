package com.seb;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements Runnable {

    private final Socket clientSocket;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                // Read data from client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // PrintWriter to send data to client
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Read HTTP request
            String requestLine = in.readLine();
            if (requestLine == null) {
                return;
            }

            // Parse request method, path and HTTP version
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3) {
                sendResponse(out, 400, "Bad Request", "text/plain", "Invalid request format");
                return;
            }

            String method = requestParts[0];
            String path = requestParts[1];

            // Parse headers
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                int colonPos = headerLine.indexOf(":");
                if (colonPos > 0) {
                    String headerName = headerLine.substring(0, colonPos).trim();
                    String headerValue = headerLine.substring(colonPos + 1).trim();
                    headers.put(headerName.toLowerCase(), headerValue);
                }
            }

            // Parse request body if present
            StringBuilder requestBody = new StringBuilder();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                if (headers.containsKey("content-length")) {
                    int contentLength = Integer.parseInt(headers.get("content-length"));
                    char[] buffer = new char[contentLength];
                    in.read(buffer, 0, contentLength);
                    requestBody.append(buffer);
                }
            }

            // Log the request
            System.out.println("Received " + method + " request for " + path);

            // Route the request to the appropriate handler based on path
            routeRequest(out, method, path, headers, requestBody.toString());

        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void routeRequest(PrintWriter out, String method, String path, Map<String, String> headers, String body) {
        // This is where you'll implement routing logic to different controllers
        // For now, just return a placeholder response

        if (path.equals("/health")) {
            sendResponse(out, 200, "OK", "application/json", "{\"status\":\"up\"}");
            return;
        }

        // Default response for unimplemented endpoints
        sendResponse(out, 501, "Not Implemented", "text/plain", "Endpoint not implemented yet");
    }

    private void sendResponse(PrintWriter out, int statusCode, String statusText, String contentType, String body) {
        out.println("HTTP/1.1 " + statusCode + " " + statusText);
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + body.length());
        out.println(); // Empty line separating headers from body
        out.println(body);
        out.flush();
    }
}