package com.recipe.servlet;

import com.google.gson.Gson;
import com.recipe.model.Recipe;
import com.recipe.model.Description;
import com.recipe.service.GeminiService;
import com.recipe.service.RecipeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GeminiRecommendationServlet extends HttpServlet {
    private final RecipeService recipeService = RecipeService.getInstance();
    private final GeminiService geminiService = GeminiService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String recipeName = req.getParameter("recipe");
        if (recipeName == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Recipe name must be provided");
            return;
        }

        Recipe targetRecipe = recipeService.getAllRecipes().stream()
                .filter(r -> r.getName().equalsIgnoreCase(recipeName))
                .findFirst()
                .orElse(null);

        if (targetRecipe == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Recipe not found");
            return;
        }

        try {
            String description = geminiService.generateRecipeDescription(targetRecipe);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new Description(description)));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to generate recommendations: " + e.getMessage());
        }
    }
}