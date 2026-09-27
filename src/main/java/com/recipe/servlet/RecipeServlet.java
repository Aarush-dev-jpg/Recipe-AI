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
import java.io.BufferedReader;

public class RecipeServlet extends HttpServlet {
    private final RecipeService recipeService = RecipeService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        List<Recipe> recipes = recipeService.getAllRecipes();
        resp.getWriter().write(gson.toJson(recipes));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        BufferedReader reader = req.getReader();
        Recipe recipe = gson.fromJson(reader, Recipe.class);
        recipeService.addRecipe(recipe);
        
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(gson.toJson(recipe));
    }
}