package com.recipe.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.recipe.model.Recipe;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeminiService {
    private static final GeminiService instance = new GeminiService();
    private final HttpClient client;
    private final Gson gson;
    private final String apiKey;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private GeminiService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.apiKey = loadApiKey();
    }

    public static GeminiService getInstance() {
        return instance;
    }

    private String loadApiKey() {
        try {
            Properties props = new Properties();
            props.load(Files.newBufferedReader(Paths.get(".env")));
            String key = props.getProperty("GEMINI_API_KEY");
            if (key == null || key.trim().isEmpty()) {
                throw new RuntimeException("GEMINI_API_KEY is not set in .env file");
            }
            return key.trim();
        } catch (IOException e) {
            logger.error("Failed to load GEMINI_API_KEY from .env file", e);
            throw new RuntimeException("Failed to load GEMINI_API_KEY from .env file", e);
        }
    }

    private JsonObject sendGeminiRequest(String prompt) throws IOException, InterruptedException {
        JsonObject promptPart = new JsonObject();
        promptPart.addProperty("text", prompt);

        JsonObject content = new JsonObject();
        content.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        parts.add(promptPart);
        content.add("parts", parts);

        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        contents.add(content);
        requestBody.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            logger.error("Gemini API request failed. Status: {}, Response: {}", 
                response.statusCode(), response.body());
            return null;
        }

        return gson.fromJson(response.body(), JsonObject.class);
    }

    public boolean testApiKey() {
        try {
            String prompt = "Hi, this is a test message. Please respond with 'API key is working' if you receive this.";
            
            JsonObject promptPart = new JsonObject();
            promptPart.addProperty("text", prompt);

            JsonObject content = new JsonObject();
            content.addProperty("role", "user");
            JsonArray parts = new JsonArray();
            parts.add(promptPart);
            content.add("parts", parts);

            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            contents.add(content);
            requestBody.add("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                logger.info("API key test successful. Response: {}", response.body());
                return true;
            }
            
            logger.error("API key test failed. Status: {}, Response: {}", 
                response.statusCode(), response.body());
            return false;
        } catch (Exception e) {
            logger.error("Error testing API key", e);
            return false;
        }
    }

    public Recipe generateRecipeByIngredient(String ingredient) {
        try {
            String prompt = String.format(
                "Create a detailed recipe that uses %s as a main ingredient. " +
                "Be creative but make sure it's a practical, real-world recipe. " +
                "Return ONLY the recipe in this exact JSON format without ANY other text or explanation:\n" +
                "{\n" +
                "  \"name\": \"Recipe Name\",\n" +
                "  \"ingredients\": [\"exact quantity + ingredient\", ...],\n" +
                "  \"instructions\": \"1. First step\\n2. Second step\\n3. Third step...\",\n" +
                "  \"timeInMinutes\": number,\n" +
                "  \"difficulty\": \"Easy/Medium/Hard\",\n" +
                "  \"categories\": [\"Cuisine Type\", \"Dish Type\", \"Dietary Category\"]\n" +
                "}", 
                ingredient
            );

            JsonObject response = sendGeminiRequest(prompt);
            if (response != null && response.has("candidates")) {
                JsonObject candidate = response.getAsJsonArray("candidates").get(0)
                    .getAsJsonObject().get("content").getAsJsonObject();
                String text = candidate.get("parts").getAsJsonArray().get(0)
                    .getAsJsonObject().get("text").getAsString();
                
                // Parse the response JSON into a Recipe object
                JsonObject recipeJson = gson.fromJson(text, JsonObject.class);
                Recipe recipe = new Recipe(
                    recipeJson.get("name").getAsString(),
                    gson.fromJson(recipeJson.get("ingredients"), List.class),
                    recipeJson.get("instructions").getAsString(),
                    recipeJson.get("timeInMinutes").getAsInt(),
                    recipeJson.get("difficulty").getAsString()
                );
                recipe.setCategories(gson.fromJson(recipeJson.get("categories"), List.class));
                return recipe;
            }
        } catch (Exception e) {
            logger.error("Failed to generate recipe", e);
        }
        return null;
    }

    public List<String> generateIngredients(String recipeName) {
        try {
            String prompt = String.format(
                "List all ingredients needed to make %s. Format as a simple list with quantities. " +
                "Example format:\n2 cups flour\n1 teaspoon salt\n3 eggs\nDo not include any other text, just the ingredients list.", 
                recipeName
            );

            JsonObject promptPart = new JsonObject();
            promptPart.addProperty("text", prompt);

            JsonObject content = new JsonObject();
            content.addProperty("role", "user");
            JsonArray parts = new JsonArray();
            parts.add(promptPart);
            content.add("parts", parts);

            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            contents.add(content);
            requestBody.add("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                    String generatedText = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("content")
                        .get("parts")
                        .getAsJsonArray()
                        .get(0)
                        .getAsJsonObject()
                        .get("text")
                        .getAsString()
                        .trim();

                    List<String> ingredients = new ArrayList<>();
                    for (String line : generatedText.split("\n")) {
                        if (!line.trim().isEmpty()) {
                            ingredients.add(line.trim());
                        }
                    }
                    return ingredients;
                }
            }
            
            logger.error("Failed to get ingredients from Gemini API. Status: {}, Response: {}", 
                response.statusCode(), response.body());
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error generating ingredients using Gemini API", e);
            return new ArrayList<>();
        }
    }

    public String generateRecipeDescription(Recipe recipe) {
        try {
            String prompt = String.format(
                "Generate a detailed, engaging description for this recipe:\nName: %s\nIngredients: %s\nInstructions: %s\n" +
                "Create a paragraph that describes this recipe in an appealing way, highlighting its key features, flavors, and what makes it special.",
                recipe.getName(),
                String.join(", ", recipe.getIngredients()),
                recipe.getInstructions()
            );

            JsonObject promptPart = new JsonObject();
            promptPart.addProperty("text", prompt);

            JsonObject content = new JsonObject();
            content.addProperty("role", "user");
            JsonArray parts = new JsonArray();
            parts.add(promptPart);
            content.add("parts", parts);

            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            contents.add(content);
            requestBody.add("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                    return jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("content")
                        .get("parts")
                        .getAsJsonArray()
                        .get(0)
                        .getAsJsonObject()
                        .get("text")
                        .getAsString()
                        .trim();
                }
            }
            
            logger.error("Failed to get description from Gemini API. Status: {}, Response: {}", 
                response.statusCode(), response.body());
            return "";

        } catch (Exception e) {
            logger.error("Error generating recipe description using Gemini API", e);
            return "";
        }
    }
}