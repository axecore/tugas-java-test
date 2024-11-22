package com.mycompany.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

import java.io.*;
import java.sql.*;
import java.util.*;

public class APIKasus implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (requestMethod.equals("GET") && path.equals("/kasus/api/get")) {
                handleGetPemohon(exchange);
            }else if (requestMethod.equals("POST") && path.equals("/kasus/api/tambah")) {
                handleAddKasus(exchange);
            }else if (requestMethod.equals("GET") && path.startsWith("/pemohon/api/get/")) {
                handleGetKasusDetails(exchange, path);
            }else if (requestMethod.equals("POST") && path.startsWith("/kasus/api/edit/")) {
                handleEditKasus(exchange, path);

            
                              
            }else {
                sendResponse(exchange, 404, "{\"message\": \"Not Found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error\"}");
        }
    }
    
    
    // Menangani POST /pemohon/api/tambah untuk menambahkan user
    private void handleAddKasus(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody();
             Reader reader = new InputStreamReader(inputStream)) {

            Gson gson = new Gson();
            Map<String, String> requestBody = gson.fromJson(reader, Map.class);

            String no_registrasi = requestBody.get("no_registrasi");
            String no_identitas = requestBody.get("no_identitas");
            String user_id = requestBody.get("user_id");
            String nama = requestBody.get("nama");
            String jenis_kasus = requestBody.get("jenis_kasus");
            String layanan = requestBody.get("layanan");
            String kronologis_kasis = requestBody.get("kronologis_kasis");

            addKasusToDatabase(no_registrasi, no_identitas, user_id, nama, jenis_kasus, layanan, kronologis_kasis);

            String response = "{\"message\": \"User added successfully\"}";
            sendResponse(exchange, 200, response);
        }
    }

    // Menangani GET /pemohon/api/get
    private void handleGetPemohon(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        OutputStream os = exchange.getResponseBody();

        List<Map<String, Object>> datas = getKasusFromDatabase();
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(datas);

        sendResponse(exchange, 200, jsonResponse);
        os.close();
    }
    
        // Menangani GET untuk mendapatkan detail
    private void handleGetKasusDetails(HttpExchange exchange, String path) throws IOException {
        // Ekstrak ID dari URL path
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid pemohon ID\"}");
            return;
        }

        try {
            int userId = Integer.parseInt(pathParts[pathParts.length - 1]);

            Map<String, Object> pemohonDetails = getKasusDetailsFromDatabase(userId);

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



    // Method untuk mengambil data pemohon dari database
    private List<Map<String, Object>> getKasusFromDatabase() {
        List<Map<String, Object>> datas = new ArrayList<>();
        String sql = "SELECT kasus.id as id, kasus.no_registrasi as no_registrasi, pemohon.no_identintas, " +
             "users.username, kasus.nama, kasus.jenis_kasus, kasus.layanan, kasus.kronologis_kasis " +
             "FROM kasus " +
             "LEFT JOIN users ON users.id = kasus.user_id " +
             "LEFT JOIN pemohon ON pemohon.no_identintas = kasus.no_identitas";
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", rs.getInt("id"));
                data.put("no_registrasi", rs.getString("no_registrasi"));
                data.put("no_identintas", rs.getString("no_identintas"));
                data.put("username", rs.getString("username"));
                data.put("nama", rs.getString("nama"));
                data.put("jenis_kasus", rs.getString("jenis_kasus"));
                data.put("layanan", rs.getString("layanan"));
                data.put("kronologis_kasis", rs.getString("kronologis_kasis"));
                datas.add(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datas;
    }
    
        // Menangani POST untuk mengedit
    private void handleEditKasus(HttpExchange exchange, String path) throws IOException {
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

                String no_registrasi = requestBody.get("no_registrasi");
                String no_identitas = requestBody.get("no_identitas");
                String user_id = requestBody.get("user_id");
                String nama = requestBody.get("nama");
                String jenis_kasus = requestBody.get("jenis_kasus");
                String layanan = requestBody.get("layanan");
                String kronologis_kasis = requestBody.get("kronologis_kasis");
              
                updateKasusInDatabase(id, no_registrasi, no_identitas, user_id, nama, jenis_kasus, layanan, kronologis_kasis);

                String response = "{\"message\": \"User updated successfully\"}";
                sendResponse(exchange, 200, response);
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID format\"}");
        }
    }
    
    
    // Method untuk menambahkan  ke database
    void addKasusToDatabase(String no_registrasi, String no_identitas,String user_id,String nama, String jenis_kasus, String layanan, String kronologis_kasis) {
        String sql = "INSERT INTO kasus (no_registrasi, no_identitas, user_id, nama, jenis_kasus, layanan, kronologis_kasis) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, no_registrasi);
            pstmt.setString(2, no_identitas);
            pstmt.setString(3, user_id);
            pstmt.setString(4, nama);
            pstmt.setString(5, jenis_kasus);
            pstmt.setString(6, layanan);
            pstmt.setString(7, kronologis_kasis);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
        // Method untuk mengambil detail dari database berdasarkan ID
    Map<String, Object> getKasusDetailsFromDatabase(int userId) {
        Map<String, Object> data = null;
        String sql = "SELECT kasus.id as id, kasus.no_registrasi as no_registrasi, pemohon.no_identintas, " +
             "users.username, kasus.nama, kasus.jenis_kasus, kasus.layanan, kasus.kronologis_kasis " +
             "FROM kasus " +
             "LEFT JOIN users ON users.id = kasus.user_id " +
             "LEFT JOIN pemohon ON pemohon.no_identintas = kasus.no_identitas WHERE kasus.id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                
                data = new HashMap<>();
                data.put("id", rs.getInt("id"));
                data.put("no_registrasi", rs.getString("no_registrasi"));
                data.put("nama", rs.getString("nama"));
                data.put("jenis_kasus", rs.getString("jenis_kasus"));
                data.put("layanan", rs.getString("layanan"));
                data.put("kronologis_kasis", rs.getString("kronologis_kasis"));
               
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    // Method untuk memperbarui data berdasarkan ID
    void updateKasusInDatabase(int id, String no_registrasi, String no_identitas, String user_id, String nama, String jenis_kasus, String layanan, String kronologis_kasis) {
        
        String sql = "UPDATE kasus SET no_registrasi = ?, no_identitas = ?, user_id = ?, nama = ?, jenis_kasus = ?, layanan = ?, kronologis_kasis = ? WHERE kasus.id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, no_registrasi);
            pstmt.setString(2, no_identitas);
            pstmt.setString(3, user_id);
            pstmt.setString(4, nama);
            pstmt.setString(5, jenis_kasus);
            pstmt.setString(6, layanan);
            pstmt.setString(7, kronologis_kasis);
            pstmt.setInt(8, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method untuk delete data berdasarkan ID
    void deleteKasusInDatabase(int userId) {
        String sql = "DELETE FROM kasus WHERE id = ?";
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
