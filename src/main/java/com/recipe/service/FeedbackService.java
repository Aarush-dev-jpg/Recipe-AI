package com.recipe.service;

import com.recipe.model.Feedback;
import java.util.*;
import java.util.logging.Logger;

public class FeedbackService {
    private static final Logger logger = Logger.getLogger(FeedbackService.class.getName());
    private static FeedbackService instance;
    private final List<Feedback> feedbackDatabase;
    private final Map<String, List<Feedback>> feedbackByRecipe; // Group feedback by recipe

    private FeedbackService() {
        this.feedbackDatabase = new ArrayList<>();
        this.feedbackByRecipe = new HashMap<>();
        logger.info("FeedbackService initialized");
    }

    public static synchronized FeedbackService getInstance() {
        if (instance == null) {
            instance = new FeedbackService();
        }
        return instance;
    }

    /**
     * Submit feedback for a recipe
     */
    public void submitFeedback(Feedback feedback) {
        logger.info("Submitting feedback for recipe: " + feedback.getRecipeName() + 
                   ", Rating: " + feedback.getRating() + "/5");
        
        feedbackDatabase.add(feedback);
        
        // Group by recipe
        String recipeId = feedback.getRecipeId();
        feedbackByRecipe.computeIfAbsent(recipeId, k -> new ArrayList<>()).add(feedback);
        
        logger.info("Feedback saved successfully. Total feedbacks: " + feedbackDatabase.size());
    }

    /**
     * Get all feedback for a specific recipe
     */
    public List<Feedback> getFeedbackForRecipe(String recipeId) {
        logger.info("Retrieving feedback for recipe ID: " + recipeId);
        return feedbackByRecipe.getOrDefault(recipeId, new ArrayList<>());
    }

    /**
     * Get average rating for a recipe
     */
    public double getAverageRating(String recipeId) {
        List<Feedback> feedbackList = getFeedbackForRecipe(recipeId);
        if (feedbackList.isEmpty()) {
            return 0.0;
        }
        
        double average = feedbackList.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
        
        logger.info("Average rating for recipe " + recipeId + ": " + average);
        return average;
    }

    /**
     * Get feedback by category (taste, difficulty, clarity, etc.)
     */
    public List<Feedback> getFeedbackByCategory(String category) {
        logger.info("Retrieving feedback by category: " + category);
        List<Feedback> result = new ArrayList<>();
        for (Feedback feedback : feedbackDatabase) {
            if (feedback.getCategory() != null && feedback.getCategory().equalsIgnoreCase(category)) {
                result.add(feedback);
            }
        }
        return result;
    }

    /**
     * Get all feedback for Gemini learning
     */
    public List<Feedback> getAllFeedback() {
        logger.info("Retrieving all feedback. Total: " + feedbackDatabase.size());
        return new ArrayList<>(feedbackDatabase);
    }

    /**
     * Get helpful feedback (rating >= 3) for AI improvement
     */
    public List<Feedback> getHelpfulFeedback() {
        logger.info("Retrieving helpful feedback (rating >= 3)");
        List<Feedback> helpful = new ArrayList<>();
        for (Feedback feedback : feedbackDatabase) {
            if (feedback.isHelpful()) {
                helpful.add(feedback);
            }
        }
        logger.info("Found " + helpful.size() + " helpful feedbacks");
        return helpful;
    }

    /**
     * Get negative feedback (rating < 3) for improvement areas
     */
    public List<Feedback> getNegativeFeedback() {
        logger.info("Retrieving negative feedback (rating < 3)");
        List<Feedback> negative = new ArrayList<>();
        for (Feedback feedback : feedbackDatabase) {
            if (!feedback.isHelpful()) {
                negative.add(feedback);
            }
        }
        logger.info("Found " + negative.size() + " negative feedbacks");
        return negative;
    }

    /**
     * Get suggested improvements from feedback
     */
    public List<String> getSuggestedImprovements(String recipeId) {
        logger.info("Collecting suggested improvements for recipe: " + recipeId);
        List<Feedback> feedbackList = getFeedbackForRecipe(recipeId);
        List<String> improvements = new ArrayList<>();
        
        for (Feedback feedback : feedbackList) {
            if (feedback.getSuggestedChanges() != null && !feedback.getSuggestedChanges().isEmpty()) {
                improvements.add(feedback.getSuggestedChanges());
            }
        }
        
        logger.info("Found " + improvements.size() + " improvement suggestions");
        return improvements;
    }

    /**
     * Get feedback statistics
     */
    public Map<String, Object> getFeedbackStatistics(String recipeId) {
        logger.info("Calculating feedback statistics for recipe: " + recipeId);
        List<Feedback> feedbackList = getFeedbackForRecipe(recipeId);
        Map<String, Object> stats = new HashMap<>();
        
        if (feedbackList.isEmpty()) {
            stats.put("total", 0);
            stats.put("averageRating", 0.0);
            stats.put("helpful", 0);
            stats.put("unhelpful", 0);
            return stats;
        }
        
        long helpful = feedbackList.stream().filter(Feedback::isHelpful).count();
        long unhelpful = feedbackList.size() - helpful;
        
        stats.put("total", feedbackList.size());
        stats.put("averageRating", getAverageRating(recipeId));
        stats.put("helpful", helpful);
        stats.put("unhelpful", unhelpful);
        
        // Category breakdown
        Map<String, Long> categoryCount = new HashMap<>();
        for (Feedback f : feedbackList) {
            categoryCount.put(f.getCategory(), categoryCount.getOrDefault(f.getCategory(), 0L) + 1);
        }
        stats.put("byCategory", categoryCount);
        
        logger.info("Statistics: " + stats);
        return stats;
    }

    /**
     * Get learning insights for Gemini
     */
    public String generateLearningInsights() {
        logger.info("Generating learning insights from all feedback");
        
        StringBuilder insights = new StringBuilder();
        insights.append("\n=== FEEDBACK LEARNING INSIGHTS ===\n");
        insights.append("Total Feedbacks: ").append(feedbackDatabase.size()).append("\n");
        
        if (feedbackDatabase.isEmpty()) {
            insights.append("No feedback available yet.\n");
            return insights.toString();
        }
        
        // Most common issues
        Map<String, Integer> issueCount = new HashMap<>();
        for (Feedback f : feedbackDatabase) {
            if (!f.isHelpful()) {
                String category = f.getCategory() != null ? f.getCategory() : "general";
                issueCount.put(category, issueCount.getOrDefault(category, 0) + 1);
            }
        }
        
        insights.append("\nMost Common Issues:\n");
        issueCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .forEach(e -> insights.append("  - ").append(e.getKey()).append(": ").append(e.getValue()).append(" times\n"));
        
        // High-rated recipes (what works well)
        Map<String, Double> recipeRatings = new HashMap<>();
        for (Feedback f : feedbackDatabase) {
            String recipeId = f.getRecipeId();
            List<Feedback> feedbackList = getFeedbackForRecipe(recipeId);
            double avgRating = feedbackList.stream()
                    .mapToInt(Feedback::getRating)
                    .average()
                    .orElse(0.0);
            recipeRatings.put(f.getRecipeName(), avgRating);
        }
        
        insights.append("\nHighest Rated Recipes:\n");
        recipeRatings.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(5)
            .forEach(e -> insights.append("  - ").append(e.getKey()).append(": ").append(String.format("%.1f", e.getValue())).append("/5\n"));
        
        insights.append("\n=== USE THIS TO IMPROVE FUTURE RECIPE GENERATION ===\n");
        return insights.toString();
    }

    public int getTotalFeedbackCount() {
        return feedbackDatabase.size();
    }
}
