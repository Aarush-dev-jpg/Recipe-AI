package com.recipe;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.recipe.service.RecipeService;
import com.recipe.model.Recipe;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SimpleWebServer {
    private static final Gson gson = new Gson();
    private static final RecipeService recipeService = RecipeService.getInstance();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // API endpoints
        server.createContext("/api/recipes", new RecipesHandler());
        server.createContext("/api/search", new SearchHandler());
        
        // Static file handler for the root path
        server.createContext("/", new StaticFileHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:8080");
    }

    static class RecipesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(recipeService.getAllRecipes());
                sendJsonResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
            exchange.close();
        }
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query == null || query.isEmpty()) {
                    exchange.sendResponseHeaders(400, -1); // Bad request
                    return;
                }

                String searchQuery = null;
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        if ("q".equals(keyValue[0])) {
                            searchQuery = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }

                if (searchQuery == null || searchQuery.isEmpty()) {
                    exchange.sendResponseHeaders(400, -1); // Bad request
                    return;
                }

                List<Recipe> results = recipeService.searchByIngredient(searchQuery);
                if (results.isEmpty()) {
                    results = recipeService.searchByCategory(searchQuery);
                }

                String response = gson.toJson(results);
                sendJsonResponse(exchange, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(204, -1);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
            exchange.close();
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html for root path
            if (path.equals("/")) {
                path = "/index.html";
            }

            // Get resource from classpath
            InputStream fileStream = getClass().getResourceAsStream("/static" + path);
            
            if (fileStream == null) {
                // Try alternative path
                fileStream = getClass().getResourceAsStream(path);
            }
            
            if (fileStream == null) {
                System.out.println("File not found: " + path);
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            // Set content type based on file extension
            String contentType = getContentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            
            // Read and send file content
            byte[] bytes = fileStream.readAllBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            } finally {
                fileStream.close();
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            return "application/octet-stream";
        }
    }
}