package com.mycompany.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

import java.io.*;
import java.sql.*;
import java.util.*;

public class APIJadwal implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (requestMethod.equals("GET") && path.equals("/jadwal/api/get")) {
                handleGetJadwal(exchange);
            }else if (requestMethod.equals("POST") && path.equals("/jadwal/api/tambah")) {
                handleAddJadwal(exchange);
            }else if (requestMethod.equals("GET") && path.startsWith("/jadwal/api/get/")) {
                handleGetJadwalDetails(exchange, path);
            }else if (requestMethod.equals("POST") && path.startsWith("/kasus/api/edit/")) {
                handleEditJadwal(exchange, path);

            
                              
            }else {
                sendResponse(exchange, 404, "{\"message\": \"Not Found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"message\": \"Internal Server Error\"}");
        }
    }
    
            // Menangani POST untuk mengedit
    private void handleEditJadwal(HttpExchange exchange, String path) throws IOException {
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

                String user_id = requestBody.get("user_id");
                String tanggal = requestBody.get("tanggal");
                String hari = requestBody.get("hari");
                String jam = requestBody.get("jam");
                String nama_pemohon = requestBody.get("nama_pemohon");
                String nama_advokat = requestBody.get("nama_advokat");
                String asisten_advokat = requestBody.get("asisten_advokat");
                String layanan = requestBody.get("layanan");
                String no_registrasi = requestBody.get("no_registrasi");
              
                updateJadwalInDatabase(id, user_id, tanggal, hari, jam, nama_pemohon, nama_advokat, asisten_advokat, layanan, no_registrasi);

                String response = "{\"message\": \"User updated successfully\"}";
                sendResponse(exchange, 200, response);
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid user ID format\"}");
        }
    }
    
    // Menangani POST /pemohon/api/tambah untuk menambahkan user
    private void handleAddJadwal(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody();
             Reader reader = new InputStreamReader(inputStream)) {

            Gson gson = new Gson();
            Map<String, String> requestBody = gson.fromJson(reader, Map.class);

            String user_id = requestBody.get("user_id");
            String tanggal = requestBody.get("tanggal");
            String hari = requestBody.get("hari");
            String jam = requestBody.get("jam");
            String nama_pemohon = requestBody.get("nama_pemohon");
            String nama_advokat = requestBody.get("nama_advokat");
            String asisten_advokat = requestBody.get("asisten_advokat");
            String layanan = requestBody.get("layanan");
            String no_registrasi = requestBody.get("no_registrasi");

            addJadwalToDatabase(user_id, tanggal, hari, jam, nama_pemohon, nama_advokat, asisten_advokat, layanan, no_registrasi);

            String response = "{\"message\": \"User added successfully\"}";
            sendResponse(exchange, 200, response);
        }
    }

    // Menangani GET /pemohon/api/get
    private void handleGetJadwal(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        OutputStream os = exchange.getResponseBody();

        List<Map<String, Object>> datas = getJadwalFromDatabase();
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(datas);

        sendResponse(exchange, 200, jsonResponse);
        os.close();
    }
    
        // Menangani GET untuk mendapatkan detail
    private void handleGetJadwalDetails(HttpExchange exchange, String path) throws IOException {
        // Ekstrak ID dari URL path
        String[] pathParts = path.split("/");
        if (pathParts.length < 4) {
            sendResponse(exchange, 400, "{\"message\": \"Invalid pemohon ID\"}");
            return;
        }

        try {
            int userId = Integer.parseInt(pathParts[pathParts.length - 1]);

            Map<String, Object> pemohonDetails = getJadwalDetailsFromDatabase(userId);

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
    private List<Map<String, Object>> getJadwalFromDatabase() {
        List<Map<String, Object>> datas = new ArrayList<>();
        String sql = "SELECT jadwal.id, users.username, jadwal.tanggal, jadwal.hari, jadwal.jam, jadwal.nama_pemohon, jadwal.nama_advokat, jadwal.asisten_advokat, jadwal.layanan " +
                     "FROM jadwal " +
                     "LEFT JOIN users ON users.id = jadwal.user_id";

        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", rs.getInt("id"));
                data.put("username", rs.getString("username"));
                data.put("tanggal", rs.getString("tanggal"));
                data.put("hari", rs.getString("hari"));
                data.put("jam", rs.getString("jam"));
                data.put("nama_pemohon", rs.getString("nama_pemohon"));
                data.put("nama_advokat", rs.getString("nama_advokat"));
                data.put("asisten_advokat", rs.getString("asisten_advokat"));
                data.put("layanan", rs.getString("layanan"));
                datas.add(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datas;
    }
        
    // Method untuk menambahkan  ke database
    void addJadwalToDatabase(String user_id, String tanggal, String hari, String jam, String nama_pemohon, String nama_advokat, String asisten_advokat, String layanan, String no_registrasi) {
        String sql = "INSERT INTO jadwal (user_id, tanggal, hari, jam, nama_pemohon, nama_advokat, asisten_advokat, layanan, kasus_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user_id);
            pstmt.setString(2, tanggal);
            pstmt.setString(3, hari);
            pstmt.setString(4, jam);
            pstmt.setString(5, nama_pemohon);
            pstmt.setString(6, nama_advokat);
            pstmt.setString(7, asisten_advokat);
            pstmt.setString(8, layanan);
            pstmt.setString(9, no_registrasi);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
        // Method untuk mengambil detail dari database berdasarkan ID
    Map<String, Object> getJadwalDetailsFromDatabase(int userId) {
        Map<String, Object> data = null;
        String sql = "SELECT jadwal.id, users.username, kasus.no_registrasi, kasus.id AS kasus_id, jadwal.tanggal, jadwal.hari, jadwal.jam, jadwal.nama_pemohon, jadwal.nama_advokat, jadwal.asisten_advokat, jadwal.layanan " +
                     "FROM jadwal " +
                     "LEFT JOIN users ON users.id = jadwal.user_id " +
                     "LEFT JOIN kasus ON kasus.id = jadwal.kasus_id WHERE jadwal.id = ?";

        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                
                data = new HashMap<>();
                data.put("id", rs.getInt("id"));
                data.put("username", rs.getString("username"));
                data.put("tanggal", rs.getString("tanggal"));
                data.put("hari", rs.getString("hari"));
                data.put("jam", rs.getString("jam"));
                data.put("nama_pemohon", rs.getString("nama_pemohon"));
                data.put("nama_advokat", rs.getString("nama_advokat"));
                data.put("asisten_advokat", rs.getString("asisten_advokat"));
                data.put("layanan", rs.getString("layanan"));
                data.put("no_registrasi", rs.getString("no_registrasi"));
                data.put("kasus_id", rs.getString("kasus_id"));
               
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    // Method untuk memperbarui data berdasarkan ID
    void updateJadwalInDatabase(int id, String user_id, String tanggal, String hari, String jam, String nama_pemohon, String nama_advokat, String asisten_advokat, String layanan, String no_registrasi) {
        
        String sql = "UPDATE jadwal SET user_id = ?, tanggal = ?, hari = ?, jam = ?, nama_pemohon = ?, nama_advokat = ?, asisten_advokat = ?, layanan = ?, kasus_id = ?  WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user_id);
            pstmt.setString(2, tanggal);
            pstmt.setString(3, hari);
            pstmt.setString(4, jam);
            pstmt.setString(5, nama_pemohon);
            pstmt.setString(6, nama_advokat);
            pstmt.setString(7, asisten_advokat);
            pstmt.setString(8, layanan);
            pstmt.setString(9, no_registrasi);
            pstmt.setInt(10, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method untuk delete data berdasarkan ID
    void deleteJadwalInDatabase(int userId) {
        String sql = "DELETE FROM jadwal WHERE id = ?";
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
