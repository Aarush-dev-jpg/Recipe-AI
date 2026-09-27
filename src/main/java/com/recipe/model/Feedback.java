package com.recipe.model;

import java.time.LocalDateTime;

public class Feedback {
    private String id;
    private String recipeId;
    private String recipeName;
    private int rating; // 1-5 stars
    private String comment;
    private String category; // taste, difficulty, clarity, ingredients, instructions
    private LocalDateTime createdAt;
    private boolean helpful; // Was the recipe helpful?
    private String suggestedChanges; // User suggestions for improvement

    public Feedback() {
    }

    public Feedback(String recipeId, String recipeName, int rating, String comment, String category) {
        this.id = "feedback_" + System.currentTimeMillis();
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.rating = rating;
        this.comment = comment;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.helpful = rating >= 3;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isHelpful() {
        return helpful;
    }

    public void setHelpful(boolean helpful) {
        this.helpful = helpful;
    }

    public String getSuggestedChanges() {
        return suggestedChanges;
    }

    public void setSuggestedChanges(String suggestedChanges) {
        this.suggestedChanges = suggestedChanges;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id='" + id + '\'' +
                ", recipeId='" + recipeId + '\'' +
                ", recipeName='" + recipeName + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", category='" + category + '\'' +
                ", helpful=" + helpful +
                ", createdAt=" + createdAt +
                '}';
    }
}
