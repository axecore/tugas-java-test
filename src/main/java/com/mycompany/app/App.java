package com.mycompany.app;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class App {
    public static void main(String[] args) throws IOException {

        // Buat server HTTP di port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Tambahkan handler untuk melayani file di folder "src/main/java/public"
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String requestPath = exchange.getRequestURI().getPath();
                String filePath;

                // Tentukan path untuk index.html atau users.html
                if (requestPath.equals("/")) {
                    filePath = "src/main/java/public/index.html";
                } else if (requestPath.equals("/users")) {
                    filePath = "src/main/java/public/users.html";
                } else if (requestPath.equals("/users/tambah")) {
                    filePath = "src/main/java/public/usersTambah.html";
                } else if (requestPath.equals("/users/edit")){
                    filePath = "src/main/java/public/usersEdit.html";
                } else if (requestPath.equals("/pemohon")) {
                    filePath = "src/main/java/public/pemohon.html";
                } else if (requestPath.equals("/pemohon/tambah")) {
                    filePath = "src/main/java/public/pemohonTambah.html";
                } else if (requestPath.equals("/pemohon/edit")) {
                    filePath = "src/main/java/public/pemohonEdit.html";
                }else if (requestPath.equals("/kasus")) {
                    filePath = "src/main/java/public/kasus.html";
                }else if (requestPath.equals("/kasus/tambah")) {
                    filePath = "src/main/java/public/kasusTambah.html";
                }else if (requestPath.equals("/kasus/edit")) {
                    filePath = "src/main/java/public/kasusEdit.html";
                }else if (requestPath.equals("/jadwal")) {
                    filePath = "src/main/java/public/jadwal.html";
                }else if (requestPath.equals("/jadwal/tambah")) {
                    filePath = "src/main/java/public/jadwalTambah.html";
                }else if (requestPath.equals("/jadwal/edit")) {
                    filePath = "src/main/java/public/jadwalEdit.html";
                }
                
                
                
                
                
                else {
                    filePath = "src/main/java/public" + requestPath;
                }

                Path path = Path.of(filePath);
                if (Files.exists(path)) {
                    byte[] fileBytes = Files.readAllBytes(path);
                    String contentType = filePath.endsWith(".html") ? "text/html" : "text/plain";

                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, fileBytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(fileBytes);
                    }
                } else {
                    String response = "404 Not Found";
                    exchange.sendResponseHeaders(404, response.getBytes().length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            }
        });

        // Tambahkan handler untuk API /users/api/get
        server.createContext("/users/api/get", new APIUsers());

        // Tambahkan handler untuk API /users/api/get/{id}
        server.createContext("/users/api/get/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");

                if (pathParts.length > 4) {
                    int userId = Integer.parseInt(pathParts[4]); // Ambil ID dari URL
                    if ("GET".equals(exchange.getRequestMethod())) {
                        // Ambil data user dari database berdasarkan ID
                        Map<String, Object> userDetails = new APIUsers().getUserDetailsFromDatabase(userId);

                        if (userDetails != null) {
                            // Kirimkan user data sebagai JSON
                            Gson gson = new Gson();
                            String response = gson.toJson(userDetails);
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, response.getBytes().length);

                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response.getBytes());
                            }
                        } else {
                            // Jika user tidak ditemukan
                            String response = "{\"message\": \"User not found\"}";
                            exchange.sendResponseHeaders(404, response.getBytes().length);

                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response.getBytes());
                            }
                        }
                    }
                }
            }
        });

        // Tambahkan handler untuk API /users/api/tambah
        server.createContext("/users/api/tambah", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Baca data JSON dari request body
                    InputStream requestBody = exchange.getRequestBody();
                    String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                    // Parse JSON untuk mendapatkan username dan level
                    Gson gson = new Gson();
                    Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                    String username = userData.get("username");
                    String level = userData.get("level");
                    String password = userData.get("password");


                    // Tambahkan user ke database
                    new APIUsers().addUserToDatabase(username, level, password);

                    // Kirim respons sukses
                    String response = "{\"message\": \"User berhasil ditambahkan\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        });

        // Tambahkan handler untuk API /users/api/edit/{id}
        server.createContext("/users/api/edit/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
//                                    System.out.println(pathParts[4]);
                if (pathParts.length > 3) {
                    
//                    int userId = Integer.parseInt(pathParts[4]); // Ambil ID dari URLSystem.out.println("ok");
                    if ("POST".equals(exchange.getRequestMethod())) {
                       
                        // Baca data JSON dari request body
                        InputStream requestBody = exchange.getRequestBody();
                        String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                        // Update user di database
                        Gson gson = new Gson();
                        Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                        int userId = Integer.parseInt(pathParts[4]);
                        String username = userData.get("username");
                        String level = userData.get("level");
                        String password = userData.get("password");
                        new APIUsers().updateUserInDatabase(userId, username, level);

                        // Kirim respons sukses
                        String response = "{\"message\": \"User berhasil diupdate\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            }
        });
        
        // Tambahkan handler untuk API /users/api/delete/{id}
        server.createContext("/users/api/delete/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
//                                    System.out.println(pathParts[4]);
                if (pathParts.length > 3) {
                    if ("POST".equals(exchange.getRequestMethod())) {
                       
                        // Baca data JSON dari request body
                        InputStream requestBody = exchange.getRequestBody();
                        String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                        // Update user di database
                        Gson gson = new Gson();
                        Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                        int userId = Integer.parseInt(pathParts[4]);
                        new APIUsers().deleteUserInDatabase(userId);

                        // Kirim respons sukses
                        String response = "{\"message\": \"User berhasil delete\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            }
        });
        
        
        
        
        
        
        
        
        
        
 
        
        // pemohon
        
        // Tambahkan handler untuk API
        server.createContext("/pemohon/api/get", new APIPemohon());
        
        // Tambahkan handler untuk API /pemohon/api/tambah
        server.createContext("/pemohon/api/tambah", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Baca data JSON dari request body
                    InputStream requestBody = exchange.getRequestBody();
                    String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                    // Parse JSON untuk mendapatkan username dan level
                    Gson gson = new Gson();
                    Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                    String no_identintas = userData.get("no_identintas");
                    String nama = userData.get("nama");
                    String jk = userData.get("jk");
                    String alamat = userData.get("alamat");
                    String tempat_lahir = userData.get("tempat_lahir");
                    String tgl_lahir = userData.get("tgl_lahir");
                    String pekerjaan = userData.get("pekerjaan");
                    String agama = userData.get("agama");
                    String kewarganegaraan = userData.get("kewarganegaraan");
                    String status = userData.get("status");
                    String no_telpon = userData.get("no_telpon");
                    String email = userData.get("email");
                    


                    // Tambahkan user ke database
                    new APIPemohon().addPemohonToDatabase(no_identintas, nama, jk, alamat, tempat_lahir, tgl_lahir, pekerjaan, agama, kewarganegaraan, status, no_telpon, email);

                    // Kirim respons sukses
                    String response = "{\"message\": \"Pemohon berhasil ditambahkan\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        });
        
         // Tambahkan handler untuk API /pemohon/api/edit/{id}
        server.createContext("/pemohon/api/edit/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
//                                    System.out.println(pathParts[4]);
                if (pathParts.length > 3) {
                    
//                    int userId = Integer.parseInt(pathParts[4]); // Ambil ID dari URLSystem.out.println("ok");
                    if ("POST".equals(exchange.getRequestMethod())) {
                       
                        // Baca data JSON dari request body
                        InputStream requestBody = exchange.getRequestBody();
                        String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                        // Update user di database
                        Gson gson = new Gson();
                        Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                        int userId = Integer.parseInt(pathParts[4]);
                        String no_identintas = userData.get("no_identintas");
                        String nama = userData.get("nama");
                        String jk = userData.get("jk");
                        String alamat = userData.get("alamat");
                        String tempat_lahir = userData.get("tempat_lahir");
                        String tgl_lahir = userData.get("tgl_lahir");
                        String pekerjaan = userData.get("pekerjaan");
                        String agama = userData.get("agama");
                        String kewarganegaraan = userData.get("kewarganegaraan");
                        String status = userData.get("status");
                        String no_telpon = userData.get("no_telpon");
                        String email = userData.get("email");
                        new APIPemohon().updatePemohonInDatabase(userId, no_identintas, nama, jk, alamat, tempat_lahir, tgl_lahir, pekerjaan, agama, kewarganegaraan, status, no_telpon, email);

                        // Kirim respons sukses
                        String response = "{\"message\": \"User berhasil diupdate\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            }
        });
        
        // Tambahkan handler untuk API /pemohon/api/delete/{id}
        server.createContext("/pemohon/api/delete/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
//                                    System.out.println(pathParts[4]);
                if (pathParts.length > 3) {
                    if ("POST".equals(exchange.getRequestMethod())) {
                       
                        // Baca data JSON dari request body
                        InputStream requestBody = exchange.getRequestBody();
                        String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                        // Update user di database
                        Gson gson = new Gson();
                        Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                        int userId = Integer.parseInt(pathParts[4]);
                        new APIPemohon().deletePemohonInDatabase(userId);

                        // Kirim respons sukses
                        String response = "{\"message\": \"User berhasil delete\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            }
        });
        
        
        
        // kasus
        // Tambahkan handler untuk API
        server.createContext("/kasus/api/get", new APIKasus());

                // Tambahkan handler untuk API /pemohon/api/tambah
        server.createContext("/kasus/api/tambah", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Baca data JSON dari request body
                    InputStream requestBody = exchange.getRequestBody();
                    String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                    // Parse JSON untuk mendapatkan username dan level
                    Gson gson = new Gson();
                    Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                    String no_registrasi = userData.get("no_registrasi");
                    String no_identitas = userData.get("no_identitas");
                    String user_id = userData.get("user_id");
                    String nama = userData.get("nama");
                    String jenis_kasus = userData.get("jenis_kasus");
                    String layanan = userData.get("layanan");
                    String kronologis_kasis = userData.get("kronologis_kasis");
                   
                    


                    // Tambahkan user ke database
                    new APIKasus().addKasusToDatabase(no_registrasi, no_identitas, user_id, nama, jenis_kasus, layanan, kronologis_kasis);

                    // Kirim respons sukses
                    String response = "{\"message\": \"Kasus berhasil ditambahkan\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        });
        
                // Tambahkan handler untuk API /kasus/api/get/{id}
        server.createContext("/kasus/api/get/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");

                if (pathParts.length > 4) {
                    int userId = Integer.parseInt(pathParts[4]); // Ambil ID dari URL
                    if ("GET".equals(exchange.getRequestMethod())) {
                        // Ambil data user dari database berdasarkan ID
                        Map<String, Object> userDetails = new APIKasus().getKasusDetailsFromDatabase(userId);

                        if (userDetails != null) {
                            // Kirimkan user data sebagai JSON
                            Gson gson = new Gson();
                            String response = gson.toJson(userDetails);
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, response.getBytes().length);

                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response.getBytes());
                            }
                        } else {
                            // Jika user tidak ditemukan
                            String response = "{\"message\": \"User not found\"}";
                            exchange.sendResponseHeaders(404, response.getBytes().length);

                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response.getBytes());
                            }
                        }
                    }
                }
            }
        });
        
                 // Tambahkan handler untuk API /kasus/api/edit/{id}
        server.createContext("/kasus/api/edit/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
//                                    System.out.println(pathParts[4]);
                if (pathParts.length > 3) {
                    
//                    int userId = Integer.parseInt(pathParts[4]); // Ambil ID dari URLSystem.out.println("ok");
                    if ("POST".equals(exchange.getRequestMethod())) {
                       
                        // Baca data JSON dari request body
                        InputStream requestBody = exchange.getRequestBody();
                        String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                        // Update user di database
                        Gson gson = new Gson();
                        Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                        int userId = Integer.parseInt(pathParts[4]);
                        String no_registrasi = userData.get("no_registrasi");
                        String no_identitas = userData.get("no_identitas");
                        String user_id = userData.get("user_id");
                        String nama = userData.get("nama");
                        String jenis_kasus = userData.get("jenis_kasus");
                        String layanan = userData.get("layanan");
                        String kronologis_kasis = userData.get("kronologis_kasis");
                        new APIKasus().updateKasusInDatabase(userId, no_registrasi, no_identitas, user_id, nama, jenis_kasus, layanan, kronologis_kasis);

                        // Kirim respons sukses
                        String response = "{\"message\": \"User berhasil diupdate\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            }
        });
        
                // Tambahkan handler untuk API /kasus/api/delete/{id}
        server.createContext("/kasus/api/delete/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
//                                    System.out.println(pathParts[4]);
                if (pathParts.length > 3) {
                    if ("POST".equals(exchange.getRequestMethod())) {
                       
                        // Baca data JSON dari request body
                        InputStream requestBody = exchange.getRequestBody();
                        String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                        // Update user di database
                        Gson gson = new Gson();
                        Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                        int userId = Integer.parseInt(pathParts[4]);
                        new APIKasus().deleteKasusInDatabase(userId);

                        // Kirim respons sukses
                        String response = "{\"message\": \"User berhasil delete\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            }
        });
        
        
        
        
        //jadwal
        server.createContext("/jadwal/api/get", new APIJadwal());
        
                        // Tambahkan handler untuk API /pemohon/api/tambah
        server.createContext("/jadwal/api/tambah", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Baca data JSON dari request body
                    InputStream requestBody = exchange.getRequestBody();
                    String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                    // Parse JSON untuk mendapatkan username dan level
                    Gson gson = new Gson();
                    Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                    String user_id = userData.get("user_id");
                    String tanggal = userData.get("tanggal");
                    String hari = userData.get("hari");
                    String jam = userData.get("jam");
                    String nama_pemohon = userData.get("nama_pemohon");
                    String nama_advokat = userData.get("nama_advokat");
                    String asisten_advokat = userData.get("asisten_advokat");
                    String layanan = userData.get("layanan");
                    String no_registrasi = userData.get("no_registrasi");
 
                    // Tambahkan user ke database
                    new APIJadwal().addJadwalToDatabase(user_id, tanggal, hari, jam, nama_pemohon, nama_advokat, asisten_advokat, layanan, no_registrasi);

                    // Kirim respons sukses
                    String response = "{\"message\": \"Kasus berhasil ditambahkan\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        });
        
                         // Tambahkan handler untuk API /kasus/api/edit/{id}
        server.createContext("/jadwal/api/edit/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
//                                    System.out.println(pathParts[4]);
                if (pathParts.length > 3) {
                    
//                    int userId = Integer.parseInt(pathParts[4]); // Ambil ID dari URLSystem.out.println("ok");
                    if ("POST".equals(exchange.getRequestMethod())) {
                       
                        // Baca data JSON dari request body
                        InputStream requestBody = exchange.getRequestBody();
                        String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                        // Update user di database
                        Gson gson = new Gson();
                        Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                        int userId = Integer.parseInt(pathParts[4]);
                        String user_id = userData.get("user_id");
                        String tanggal = userData.get("tanggal");
                        String hari = userData.get("hari");
                        String jam = userData.get("jam");
                        String nama_pemohon = userData.get("nama_pemohon");
                        String nama_advokat = userData.get("nama_advokat");
                        String asisten_advokat = userData.get("asisten_advokat");
                        String layanan = userData.get("layanan");
                        String no_registrasi = userData.get("no_registrasi");
                        new APIJadwal().updateJadwalInDatabase(userId, user_id, tanggal, hari, jam, nama_pemohon, nama_advokat, asisten_advokat, layanan, no_registrasi);

                        // Kirim respons sukses
                        String response = "{\"message\": \"User berhasil diupdate\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            }
        });
        
                        // Tambahkan handler untuk API /kasus/api/delete/{id}
        server.createContext("/jadwal/api/delete/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                String[] pathParts = path.split("/");
//                                    System.out.println(pathParts[4]);
                if (pathParts.length > 3) {
                    if ("POST".equals(exchange.getRequestMethod())) {
                       
                        // Baca data JSON dari request body
                        InputStream requestBody = exchange.getRequestBody();
                        String jsonInput = new String(requestBody.readAllBytes(), "UTF-8");

                        // Update user di database
                        Gson gson = new Gson();
                        Map<String, String> userData = gson.fromJson(jsonInput, Map.class);
                        int userId = Integer.parseInt(pathParts[4]);
                        new APIJadwal().deleteJadwalInDatabase(userId);

                        // Kirim respons sukses
                        String response = "{\"message\": \"User berhasil delete\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                }
            }
        });
        
        
        // Jalankan server
        server.start();
        System.out.println("Server is running at http://localhost:8080");
    }
}
