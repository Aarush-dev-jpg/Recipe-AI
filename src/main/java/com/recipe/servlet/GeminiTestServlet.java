package com.recipe.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.recipe.service.GeminiService;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class GeminiTestServlet extends HttpServlet {
    private final GeminiService geminiService = GeminiService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        boolean isWorking = geminiService.testApiKey();
        
        JsonObject response = new JsonObject();
        response.addProperty("status", isWorking ? "active" : "inactive");
        response.addProperty("message", isWorking ? 
            "Gemini API key is working correctly" : 
            "Gemini API key test failed. Check server logs for details.");
        
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(response));
    }
}