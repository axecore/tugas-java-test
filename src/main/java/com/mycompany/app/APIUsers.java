package com.mycompany.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

import java.io.*;
import java.sql.*;
import java.util.*;

public class APIUsers implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (requestMethod.equals("GET") && path.equals("/users/api/get")) {
                handleGetUsers(exchange);
            } else if (requestMethod.equals("GET") && path.startsWith("/users/api/get/")) {
                handleGetUserDetails(exchange, path);  // Menangani permintaan GET detail user
            } else if (requestMethod.equals("POST") && path.equals("/users/api/tambah")) {
                handleAddUser(exchange);
            } else if (requestMethod.equals("POST") && path.startsWith("/users/api/edit/")) {
                handleEditUser(exchange, path);
            } else {
                sendResponse(exchange, 404, "{\"message\": \"Not Found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error\"}");
        }
    }

    // Menangani GET /users/api/get
    private void handleGetUsers(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        OutputStream os = exchange.getResponseBody();

        List<Map<String, Object>> users = getUsersFromDatabase();
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(users);

        sendResponse(exchange, 200, jsonResponse);
        os.close();
    }

    // Menangani GET /users/api/get/{id} untuk mendapatkan detail user
    private void handleGetUserDetails(HttpExchange exchange, String path) throws IOException {
        // Ekstrak ID dari URL path
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID\"}");
            return;
        }

        try {
            int userId = Integer.parseInt(pathParts[pathParts.length - 1]);

            Map<String, Object> userDetails = getUserDetailsFromDatabase(userId);

            if (userDetails != null) {
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(userDetails);
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 404, "{\"message\": \"User not found\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID format\"}");
        }
    }

    // Method untuk mengambil detail pengguna dari database berdasarkan ID
    Map<String, Object> getUserDetailsFromDatabase(int userId) {
        Map<String, Object> user = null;
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("password", rs.getString("password"));
                user.put("level", rs.getString("level"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Menangani POST /users/api/tambah untuk menambahkan user
    private void handleAddUser(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody();
             Reader reader = new InputStreamReader(inputStream)) {

            Gson gson = new Gson();
            Map<String, String> requestBody = gson.fromJson(reader, Map.class);

            String username = requestBody.get("username");
            String level = requestBody.get("level");
            String password = requestBody.get("password");

            if (username == null || level == null || username.isEmpty() || level.isEmpty()) {
                sendResponse(exchange, 400, "{\"message\": \"Username and level must be provided\"}");
                return;
            }

            addUserToDatabase(username, level, password);

            String response = "{\"message\": \"User added successfully\"}";
            sendResponse(exchange, 200, response);
        }
    }

    // Menangani POST /users/api/edit/{id} untuk mengedit user
    private void handleEditUser(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathParts[pathParts.length - 1]);

            try (InputStream inputStream = exchange.getRequestBody();
                 Reader reader = new InputStreamReader(inputStream)) {

                Gson gson = new Gson();
                Map<String, String> requestBody = gson.fromJson(reader, Map.class);

                String username = requestBody.get("username");
                String level = requestBody.get("level");
                String password = requestBody.get("password");

                if (username == null || level == null || username.isEmpty() || level.isEmpty()) {
                    sendResponse(exchange, 400, "{\"message\": \"Username and level must be provided\"}");
                    return;
                }

                updateUserInDatabase(id, username, level);

                String response = "{\"message\": \"User updated successfully\"}";
                sendResponse(exchange, 200, response);
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID format\"}");
        }
    }
    

    // Method untuk mengambil data pengguna dari database
    private List<Map<String, Object>> getUsersFromDatabase() {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT id, username, level FROM users";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("level", rs.getString("level"));
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    // Menangani POST /users/api/edit/{id} untuk mengedit user
    private void handleDeleteUser(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID\"}");
            return;
        }

        try {
            int id = Integer.parseInt(pathParts[pathParts.length - 1]);

            try (InputStream inputStream = exchange.getRequestBody();
                 Reader reader = new InputStreamReader(inputStream)) {

                Gson gson = new Gson();
                Map<String, String> requestBody = gson.fromJson(reader, Map.class);

                String username = requestBody.get("username");
                String level = requestBody.get("level");
                String password = requestBody.get("password");

                if (username == null || level == null || username.isEmpty() || level.isEmpty()) {
                    sendResponse(exchange, 400, "{\"message\": \"Username and level must be provided\"}");
                    return;
                }

                updateUserInDatabase(id, username, level);

                String response = "{\"message\": \"User updated successfully\"}";
                sendResponse(exchange, 200, response);
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID format\"}");
        }
    }

    // Method untuk menambahkan user ke database
    void addUserToDatabase(String username, String level, String password) {
        String sql = "INSERT INTO users (username, level, password) VALUES (?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, level);
            pstmt.setString(3, password);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method untuk memperbarui data pengguna berdasarkan ID
    void updateUserInDatabase(int id, String username, String level) {
        
        String sql = "UPDATE users SET username = ?, level = ? WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, level);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    void deleteUserInDatabase(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Helper method to send response
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
