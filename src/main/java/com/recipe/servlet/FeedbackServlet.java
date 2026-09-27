package com.recipe.servlet;

import com.google.gson.Gson;
import com.recipe.model.Feedback;
import com.recipe.service.FeedbackService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FeedbackServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(FeedbackServlet.class.getName());
    private final Gson gson = new Gson();
    private final FeedbackService feedbackService = FeedbackService.getInstance();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json");

        try {
            // Read JSON from request body
            BufferedReader reader = new BufferedReader(request.getReader());
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            String jsonBody = jsonBuilder.toString();
            logger.info("Received feedback: " + jsonBody);

            // Parse feedback
            Feedback feedback = gson.fromJson(jsonBody, Feedback.class);

            // Validate
            if (feedback.getRecipeId() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
                logger.warning("Invalid feedback data");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(new HashMap<String, String>() {{
                    put("error", "Invalid feedback data. Rating must be 1-5");
                }}));
                return;
            }

            // Save feedback
            feedbackService.submitFeedback(feedback);

            // Response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Feedback submitted successfully");
            result.put("feedbackId", feedback.getId());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            logger.severe("Error processing feedback: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new HashMap<String, String>() {{
                put("error", "Error processing feedback: " + e.getMessage());
            }}));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Type", "application/json");

        try {
            String recipeId = request.getParameter("recipeId");
            String action = request.getParameter("action");

            logger.info("GET feedback request - recipeId: " + recipeId + ", action: " + action);

            if ("statistics".equals(action) && recipeId != null) {
                // Get statistics for specific recipe
                Map<String, Object> stats = feedbackService.getFeedbackStatistics(recipeId);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(stats));

            } else if ("insights".equals(action)) {
                // Get learning insights
                String insights = feedbackService.generateLearningInsights();
                Map<String, Object> result = new HashMap<>();
                result.put("insights", insights);
                result.put("totalFeedback", feedbackService.getTotalFeedbackCount());
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(result));

            } else if (recipeId != null) {
                // Get all feedback for recipe
                var feedbackList = feedbackService.getFeedbackForRecipe(recipeId);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(feedbackList));

            } else {
                // Get all feedback
                var allFeedback = feedbackService.getAllFeedback();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(allFeedback));
            }

        } catch (Exception e) {
            logger.severe("Error retrieving feedback: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new HashMap<String, String>() {{
                put("error", "Error retrieving feedback: " + e.getMessage());
            }}));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
