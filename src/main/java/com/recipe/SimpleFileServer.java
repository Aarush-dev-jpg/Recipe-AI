package com.recipe;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.recipe.service.RecipeService;
import com.recipe.service.GeminiService;
import com.recipe.model.Recipe;
import com.google.gson.Gson;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;

public class SimpleFileServer {
    private static final Gson gson = new Gson();
    private static final RecipeService recipeService = RecipeService.getInstance();
    private static final String WEBAPP_PATH = "src/main/webapp";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // API endpoints
        server.createContext("/api/recipes", new RecipesHandler());
        server.createContext("/api/search", new SearchHandler());
        
        // Static file handler for the root path
        server.createContext("/", new FileHandler());
        
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
            System.out.println("Search request received: " + exchange.getRequestURI());
            
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                System.out.println("Raw query: " + query);
                
                if (query == null || query.isEmpty()) {
                    System.out.println("Empty query received");
                    sendJsonResponse(exchange, "[]");
                    return;
                }

                List<Recipe> results = new ArrayList<>();
                String searchQuery = null;
                
                try {
                    // Support both q= and ingredient= parameters
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=", 2);
                        if (keyValue.length == 2) {
                            String key = keyValue[0];
                            String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                            
                            if ("q".equals(key) || "ingredient".equals(key)) {
                                searchQuery = value;
                                break;
                            }
                        }
                    }

                    if (searchQuery == null || searchQuery.isEmpty()) {
                        System.out.println("No search query found in parameters");
                        sendJsonResponse(exchange, "[]");
                        return;
                    }

                    System.out.println("Searching for: " + searchQuery);
                    
                    // First try local database
                    results = recipeService.searchByIngredient(searchQuery);
                    System.out.println("Found " + results.size() + " recipes by ingredient");
                    
                    if (results.isEmpty()) {
                        results = recipeService.searchByCategory(searchQuery);
                        System.out.println("Found " + results.size() + " recipes by category");
                    }
                    
                    // If still no results, try Gemini
                    if (results.isEmpty()) {
                        System.out.println("No local results, trying Gemini");
                        try {
                            Recipe generatedRecipe = GeminiService.getInstance().generateRecipeByIngredient(searchQuery);
                            if (generatedRecipe != null) {
                                results.add(generatedRecipe);
                                System.out.println("Added Gemini-generated recipe: " + generatedRecipe.getName());
                            }
                        } catch (Exception e) {
                            System.err.println("Error with Gemini: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    String response = gson.toJson(results);
                    System.out.println("Sending response with " + results.size() + " recipes");
                    sendJsonResponse(exchange, response);
                    
                } catch (Exception e) {
                    System.err.println("Error processing search: " + e.getMessage());
                    e.printStackTrace();
                    sendJsonResponse(exchange, "[]");
                }

                System.out.println("Found " + results.size() + " recipes");
                String response = gson.toJson(results);
                System.out.println("Response: " + response);
                sendJsonResponse(exchange, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                System.out.println("Handling OPTIONS request");
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

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html for root path
            if (path.equals("/")) {
                path = "/index.html";
            }

            Path filePath = Paths.get(WEBAPP_PATH, path.substring(1));
            
            if (!Files.exists(filePath)) {
                System.out.println("File not found: " + filePath);
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
            byte[] bytes = Files.readAllBytes(filePath);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
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

    private static void sendJsonResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        
        if (response == null || response.isEmpty()) {
            response = "[]"; // Return empty array instead of empty response
        }
        
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}