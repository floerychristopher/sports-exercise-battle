package com.seb;

import com.seb.controller.StreakController;
import com.seb.controller.ProfileController;
import com.seb.controller.PushupController;
import com.seb.controller.TournamentController;
import com.seb.controller.UserController;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestHandler implements Runnable {

    private final Socket clientSocket;

    private final ObjectMapper mapper;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;

        this.mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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

        try {
            // Health check endpoint
            if (path.equals("/health")) {
                sendResponse(out, 200, "OK", "application/json", "{\"status\":\"up\"}");
                return;
            }

            // === USER ENDPOINTS ===

            // User registration endpoint
            if (method.equals("POST") && path.equals("/users")) {
                try {
                    Map<String, String> requestData = mapper.readValue(body, Map.class);
                    String username = requestData.get("Username");
                    String password = requestData.get("Password");

                    if (username == null || password == null) {
                        sendResponse(out, 400, "Bad Request", "application/json",
                                mapper.writeValueAsString(Map.of("success", false, "message", "Username and password are required")));
                        return;
                    }

                    UserController userController = new UserController();
                    Map<String, Object> result = userController.register(username, password);

                    int statusCode = (Boolean)result.get("success") ? 201 : 400;
                    sendResponse(out, statusCode, statusCode == 201 ? "Created" : "Bad Request",
                            "application/json", mapper.writeValueAsString(result));
                    return;
                } catch (Exception e) {
                    sendResponse(out, 500, "Internal Server Error", "application/json",
                            mapper.writeValueAsString(Map.of("success", false, "message", "Server error: " + e.getMessage())));
                    return;
                }
            }

            // User login endpoint
            if (method.equals("POST") && path.equals("/sessions")) {
                try {
                    Map<String, String> requestData = mapper.readValue(body, Map.class);
                    String username = requestData.get("Username");
                    String password = requestData.get("Password");

                    if (username == null || password == null) {
                        sendResponse(out, 400, "Bad Request", "application/json",
                                mapper.writeValueAsString(Map.of("success", false, "message", "Username and password are required")));
                        return;
                    }

                    UserController userController = new UserController();
                    Map<String, Object> result = userController.login(username, password);

                    int statusCode = (Boolean)result.get("success") ? 200 : 401;
                    sendResponse(out, statusCode, statusCode == 200 ? "OK" : "Unauthorized",
                            "application/json", mapper.writeValueAsString(result));
                    return;
                } catch (Exception e) {
                    sendResponse(out, 500, "Internal Server Error", "application/json",
                            mapper.writeValueAsString(Map.of("success", false, "message", "Server error: " + e.getMessage())));
                    return;
                }
            }

            // All other endpoints require authentication
            if (!path.equals("/users") && !path.equals("/sessions") && !path.equals("/health")) {
                String authToken = headers.get("authorization");
                if (authToken == null) {
                    sendResponse(out, 401, "Unauthorized", "application/json",
                            mapper.writeValueAsString(Map.of("success", false, "message", "Authentication required")));
                    return;
                }

                // Extract username from token format "Basic username-sebToken"
                UserController userController = new UserController();
                String requestingUsername = userController.getUsernameFromToken(authToken);

                // Validate token and get user ID
                Optional<Integer> userIdOpt = userController.getUserIdFromToken(authToken);

                if (!userIdOpt.isPresent() || requestingUsername == null) {
                    sendResponse(out, 401, "Unauthorized", "application/json",
                            mapper.writeValueAsString(Map.of("success", false, "message", "Invalid authentication token")));
                    return;
                }

                int userId = userIdOpt.get();

                // User profile endpoints
                if (path.startsWith("/users/")) {
                    ProfileController profileController = new ProfileController();
                    String profileUsername = path.substring("/users/".length());

                    // Security check - users can only access their own profiles
                    if (!profileUsername.equals(requestingUsername)) {
                        sendResponse(out, 403, "Forbidden", "application/json",
                                mapper.writeValueAsString(Map.of("success", false, "message", "You can only access your own profile")));
                        return;
                    }

                    if (method.equals("GET")) {
                        // Get profile
                        Map<String, Object> result = profileController.getProfile(userId);
                        sendResponse(out, 200, "OK", "application/json", mapper.writeValueAsString(result));
                        return;
                    } else if (method.equals("PUT")) {
                        // Update profile
                        Map<String, String> requestData = mapper.readValue(body, Map.class);

                        // Extract Name, Bio, Image as per curl script
                        String name = requestData.get("Name");
                        String bio = requestData.get("Bio");
                        String image = requestData.get("Image");

                        Map<String, Object> result = profileController.updateProfile(userId, name, bio, image);
                        sendResponse(out, 200, "OK", "application/json", mapper.writeValueAsString(result));
                        return;
                    }
                }

                // Stats endpoint: /stats -> /stats (unchanged path)
                if (path.equals("/stats")) {
                    PushupController pushupController = new PushupController();

                    if (method.equals("GET")) {
                        Map<String, Object> result = pushupController.getUserStats(userId);
                        sendResponse(out, 200, "OK", "application/json", mapper.writeValueAsString(result));
                        return;
                    }
                }

                // Scoreboard endpoint: /scoreboard -> /score
                if (path.equals("/score")) {
                    ProfileController profileController = new ProfileController();

                    if (method.equals("GET")) {
                        Map<String, Object> result = profileController.getScoreboard();
                        sendResponse(out, 200, "OK", "application/json", mapper.writeValueAsString(result));
                        return;
                    }
                }

                // Pushup history: /pushups/history -> /history
                if (path.equals("/history")) {
                    PushupController pushupController = new PushupController();

                    if (method.equals("GET")) {
                        // Get pushup history
                        Map<String, Object> result = pushupController.getUserHistory(userId);
                        sendResponse(out, 200, "OK", "application/json", mapper.writeValueAsString(result));
                        return;
                    } else if (method.equals("POST")) {
                        // Record pushups - with updated fields from curl
                        Map<String, Object> requestData = mapper.readValue(body, Map.class);

                        // Extract fields as per curl script
                        String name = (String) requestData.get("Name");
                        Integer count = (Integer) requestData.get("Count");
                        Integer durationInSeconds = (Integer) requestData.get("DurationInSeconds");

                        if (count == null) {
                            sendResponse(out, 400, "Bad Request", "application/json",
                                    mapper.writeValueAsString(Map.of("success", false, "message", "Count is required")));
                            return;
                        }

                        Map<String, Object> result = pushupController.recordPushups(userId, count, durationInSeconds);
                        sendResponse(out, 200, "OK", "application/json", mapper.writeValueAsString(result));
                        return;
                    }
                }

                // Tournament endpoint: /tournaments -> /tournament
                if (path.equals("/tournament")) {
                    TournamentController tournamentController = new TournamentController();

                    if (method.equals("GET")) {
                        // Get active tournament
                        Map<String, Object> result = tournamentController.getActiveTournament();
                        sendResponse(out, 200, "OK", "application/json", mapper.writeValueAsString(result));
                        return;
                    }
                }

                // Other tournament endpoints should be updated similarly
                if (path.equals("/tournaments/recent")) {
                    // Update to match curl if needed
                    // ...
                }
            }

            // If we get here, the endpoint wasn't found
            sendResponse(out, 404, "Not Found", "application/json",
                    mapper.writeValueAsString(Map.of("success", false, "message", "Endpoint not found")));

        } catch (Exception e) {
            try {
                sendResponse(out, 500, "Internal Server Error", "application/json",
                        mapper.writeValueAsString(Map.of("success", false, "message", "Server error: " + e.getMessage())));
            } catch (Exception jsonEx) {
                sendResponse(out, 500, "Internal Server Error", "text/plain", "Server error");
            }
        }
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