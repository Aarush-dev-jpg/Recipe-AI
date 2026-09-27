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

public class SearchServlet extends HttpServlet {
    private final RecipeService recipeService = RecipeService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String ingredient = req.getParameter("ingredient");
        String category = req.getParameter("category");
        
        List<Recipe> results;
        if (ingredient != null) {
            results = recipeService.searchByIngredient(ingredient);
        } else if (category != null) {
            results = recipeService.searchByCategory(category);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Must provide either ingredient or category parameter");
            return;
        }
        
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(results));
    }
}