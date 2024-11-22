package com.mycompany.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

import java.io.*;
import java.sql.*;
import java.util.*;

public class APIPemohon implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (requestMethod.equals("GET") && path.equals("/pemohon/api/get")) {
                handleGetPemohon(exchange);
            }else if (requestMethod.equals("POST") && path.equals("/pemohon/api/tambah")) {
                handleAddPemohon(exchange);
            }else if (requestMethod.equals("GET") && path.startsWith("/pemohon/api/get/")) {
                handleGetPemohonDetails(exchange, path);
            }else if (requestMethod.equals("POST") && path.startsWith("/pemohon/api/edit/")) {
                handleEditPemohon(exchange, path);
            
                
                
                
            }else {
                sendResponse(exchange, 404, "{\"message\": \"Not Found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error\"}");
        }
    }

    // Menangani GET /pemohon/api/get
    private void handleGetPemohon(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        OutputStream os = exchange.getResponseBody();

        List<Map<String, Object>> datas = getPemohonFromDatabase();
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(datas);

        sendResponse(exchange, 200, jsonResponse);
        os.close();
    }

    // Menangani POST /pemohon/api/tambah untuk menambahkan user
    private void handleAddPemohon(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody();
             Reader reader = new InputStreamReader(inputStream)) {

            Gson gson = new Gson();
            Map<String, String> requestBody = gson.fromJson(reader, Map.class);

            String no_identintas = requestBody.get("no_identintas");
            String nama = requestBody.get("nama");
            String jk = requestBody.get("jk");
            String alamat = requestBody.get("alamat");
            String tempat_lahir = requestBody.get("tempat_lahir");
            String tgl_lahir = requestBody.get("tgl_lahir");
            String pekerjaan = requestBody.get("pekerjaan");
            String agama = requestBody.get("agama");
            String kewarganegaraan = requestBody.get("kewarganegaraan");
            String status = requestBody.get("status");
            String no_telpon = requestBody.get("no_telpon");
            String email = requestBody.get("email");

            addPemohonToDatabase(no_identintas, nama, jk, alamat, tempat_lahir, tgl_lahir, pekerjaan, agama, kewarganegaraan, status, no_telpon, email);

            String response = "{\"message\": \"User added successfully\"}";
            sendResponse(exchange, 200, response);
        }
    }
    
    // Menangani GET untuk mendapatkan detail
    private void handleGetPemohonDetails(HttpExchange exchange, String path) throws IOException {
        // Ekstrak ID dari URL path
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid pemohon ID\"}");
            return;
        }

        try {
            int userId = Integer.parseInt(pathParts[pathParts.length - 1]);

            Map<String, Object> pemohonDetails = getPemohonDetailsFromDatabase(userId);

            if (pemohonDetails != null) {
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(pemohonDetails);
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 404, "{\"message\": \"User not found\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID format\"}");
        }
    }
    
    // Menangani POST untuk mengedit
    private void handleEditPemohon(HttpExchange exchange, String path) throws IOException {
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

                String no_identintas = requestBody.get("no_identintas");
                String nama = requestBody.get("nama");
                String jk = requestBody.get("jk");
                String alamat = requestBody.get("alamat");
                String tempat_lahir = requestBody.get("tempat_lahir");
                String tgl_lahir = requestBody.get("tgl_lahir");
                String pekerjaan = requestBody.get("pekerjaan");
                String agama = requestBody.get("agama");
                String kewarganegaraan = requestBody.get("kewarganegaraan");
                String status = requestBody.get("status");
                String no_telpon = requestBody.get("no_telpon");
                String email = requestBody.get("email");

                updatePemohonInDatabase(id, no_identintas, nama, jk, alamat, tempat_lahir, tgl_lahir, pekerjaan, agama, kewarganegaraan, status, no_telpon, email);

                String response = "{\"message\": \"User updated successfully\"}";
                sendResponse(exchange, 200, response);
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID format\"}");
        }
    }


   
    // Method untuk mengambil data pemohon dari database
    private List<Map<String, Object>> getPemohonFromDatabase() {
        List<Map<String, Object>> datas = new ArrayList<>();
        String sql = "SELECT * FROM pemohon";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", rs.getInt("id"));
                data.put("no_identintas", rs.getString("no_identintas"));
                data.put("nama", rs.getString("nama"));
                data.put("jk", rs.getString("jk"));
                data.put("alamat", rs.getString("alamat"));
                data.put("tempat_lahir", rs.getString("tempat_lahir"));
                data.put("tgl_lahir", rs.getString("tgl_lahir"));
                data.put("pekerjaan", rs.getString("pekerjaan"));
                data.put("agama", rs.getString("agama"));
                data.put("kewarganegaraan", rs.getString("kewarganegaraan"));
                data.put("status", rs.getString("status"));
                data.put("no_telpon", rs.getString("no_telpon"));
                data.put("email", rs.getString("email"));
                datas.add(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datas;
    }
    
    // Method untuk menambahkan  ke database
    void addPemohonToDatabase(String no_identintas,String nama, String jk, String alamat, String tempat_lahir, String tgl_lahir, String pekerjaan, String agama, String kewarganegaraan, String status, String no_telpon, String email) {
        String sql = "INSERT INTO pemohon (no_identintas, nama, jk, alamat, tempat_lahir, tgl_lahir, pekerjaan, agama, kewarganegaraan, status, no_telpon, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, no_identintas);
            pstmt.setString(2, nama);
            pstmt.setString(3, jk);
            pstmt.setString(4, alamat);
            pstmt.setString(5, tempat_lahir);
            pstmt.setString(6, tgl_lahir);
            pstmt.setString(7, pekerjaan);
            pstmt.setString(8, agama);
            pstmt.setString(9, kewarganegaraan);
            pstmt.setString(10, status);
            pstmt.setString(11, no_telpon);
            pstmt.setString(12, email);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
   
    // Method untuk mengambil detail dari database berdasarkan ID
    Map<String, Object> getPemohonDetailsFromDatabase(int userId) {
        Map<String, Object> data = null;
        String sql = "SELECT * FROM pemohon WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                
                data = new HashMap<>();
                data.put("id", rs.getInt("id"));
                data.put("no_identintas", rs.getString("no_identintas"));
                data.put("nama", rs.getString("nama"));
                data.put("alamat", rs.getString("alamat"));
                data.put("jk", rs.getString("jk"));
                data.put("tempat_lahir", rs.getString("tempat_lahir"));
                data.put("tgl_lahir", rs.getString("tgl_lahir"));
                data.put("pekerjaan", rs.getString("pekerjaan"));
                data.put("agama", rs.getString("agama"));
                data.put("kewarganegaraan", rs.getString("kewarganegaraan"));
                data.put("status", rs.getString("status"));
                data.put("no_telpon", rs.getString("no_telpon"));
                data.put("email", rs.getString("email"));
               
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    // Method untuk memperbarui data berdasarkan ID
    void updatePemohonInDatabase(int id, String no_identintas,String nama, String jk, String alamat, String tempat_lahir, String tgl_lahir, String pekerjaan, String agama, String kewarganegaraan, String status, String no_telpon, String email) {
        
        String sql = "UPDATE pemohon SET no_identintas = ?, nama = ?, jk = ?, alamat = ?, tempat_lahir = ?, tgl_lahir = ?, pekerjaan = ?, agama = ?, kewarganegaraan = ?, status = ?, no_telpon = ?, email = ? WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, no_identintas);
            pstmt.setString(2, nama);
            pstmt.setString(3, jk);
            pstmt.setString(4, alamat);
            pstmt.setString(5, tempat_lahir);
            pstmt.setString(6, tgl_lahir);
            pstmt.setString(7, pekerjaan);
            pstmt.setString(8, agama);
            pstmt.setString(9, kewarganegaraan);
            pstmt.setString(10, status);
            pstmt.setString(11, no_telpon);
            pstmt.setString(12, email);
            pstmt.setInt(13, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Method untuk delete data berdasarkan ID
    void deletePemohonInDatabase(int userId) {
        String sql = "DELETE FROM pemohon WHERE id = ?";
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
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes("UTF-8").length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes("UTF-8"));
        }
    }

}
