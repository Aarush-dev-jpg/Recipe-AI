package com.recipe.servlet;

import com.google.gson.Gson;
import com.recipe.model.Recipe;
import com.recipe.service.RecipeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class RecommendationServlet extends HttpServlet {
    private final RecipeService recipeService = RecipeService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String recipeName = req.getParameter("recipe");
        if (recipeName == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Recipe name must be provided");
            return;
        }

        List<Recipe> allRecipes = recipeService.getAllRecipes();
        Recipe targetRecipe = allRecipes.stream()
                .filter(r -> r.getName().equalsIgnoreCase(recipeName))
                .findFirst()
                .orElse(null);

        if (targetRecipe == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Recipe not found");
            return;
        }

        List<Recipe> recommendations = recipeService.recommendSimilarRecipes(targetRecipe);
        
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(recommendations));
    }
}