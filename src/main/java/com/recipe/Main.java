package com.recipe;

import com.recipe.model.Recipe;
import com.recipe.service.RecipeService;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        RecipeService recipeService = RecipeService.getInstance();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== Recipe Recommendation System ===");
            System.out.println("1. View all recipes");
            System.out.println("2. Search by ingredient");
            System.out.println("3. Search by category");
            System.out.println("4. Add new recipe");
            System.out.println("5. Get recipe recommendations");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1:
                    displayRecipes(recipeService.getAllRecipes());
                    break;
                    
                case 2:
                    System.out.print("Enter ingredient to search: ");
                    String ingredient = scanner.nextLine();
                    List<Recipe> recipesByIngredient = recipeService.searchByIngredient(ingredient);
                    displayRecipes(recipesByIngredient);
                    break;
                    
                case 3:
                    System.out.print("Enter category to search: ");
                    String category = scanner.nextLine();
                    List<Recipe> recipesByCategory = recipeService.searchByCategory(category);
                    displayRecipes(recipesByCategory);
                    break;
                    
                case 4:
                    addNewRecipe(scanner, recipeService);
                    break;
                    
                case 5:
                    if (recipeService.getAllRecipes().isEmpty()) {
                        System.out.println("No recipes available for recommendations.");
                        break;
                    }
                    System.out.println("Select a recipe to get recommendations for:");
                    List<Recipe> allRecipes = recipeService.getAllRecipes();
                    for (int i = 0; i < allRecipes.size(); i++) {
                        System.out.println((i + 1) + ". " + allRecipes.get(i).getName());
                    }
                    int recipeIndex = scanner.nextInt() - 1;
                    if (recipeIndex >= 0 && recipeIndex < allRecipes.size()) {
                        List<Recipe> recommendations = recipeService.recommendSimilarRecipes(allRecipes.get(recipeIndex));
                        System.out.println("\nRecommended Recipes:");
                        displayRecipes(recommendations);
                    }
                    break;
                    
                case 6:
                    System.out.println("Thank you for using Recipe Recommendation System!");
                    scanner.close();
                    return;
                    
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private static void displayRecipes(List<Recipe> recipes) {
        if (recipes.isEmpty()) {
            System.out.println("No recipes found.");
            return;
        }
        
        for (Recipe recipe : recipes) {
            System.out.println("\n=== " + recipe.getName() + " ===");
            System.out.println("Difficulty: " + recipe.getDifficulty());
            System.out.println("Cooking Time: " + recipe.getCookingTime() + " minutes");
            System.out.println("Categories: " + String.join(", ", recipe.getCategories()));
            System.out.println("Ingredients:");
            for (String ingredient : recipe.getIngredients()) {
                System.out.println("- " + ingredient);
            }
            System.out.println("Instructions:\n" + recipe.getInstructions());
            System.out.println("----------------------------------------");
        }
    }
    
    private static void addNewRecipe(Scanner scanner, RecipeService recipeService) {
        System.out.print("Enter recipe name: ");
        String name = scanner.nextLine();
        
        System.out.println("Enter ingredients (one per line, empty line to finish):");
        List<String> ingredients = new ArrayList<>();
        while (true) {
            String ingredient = scanner.nextLine();
            if (ingredient.isEmpty()) break;
            ingredients.add(ingredient);
        }
        
        System.out.println("Enter cooking instructions:");
        String instructions = scanner.nextLine();
        
        System.out.print("Enter cooking time (in minutes): ");
        int cookingTime = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        System.out.print("Enter difficulty (Easy/Medium/Hard): ");
        String difficulty = scanner.nextLine();
        
        Recipe newRecipe = new Recipe(name, ingredients, instructions, cookingTime, difficulty);
        
        System.out.println("Enter categories (one per line, empty line to finish):");
        List<String> categories = new ArrayList<>();
        while (true) {
            String category = scanner.nextLine();
            if (category.isEmpty()) break;
            categories.add(category);
        }
        newRecipe.setCategories(categories);
        
        recipeService.addRecipe(newRecipe);
        System.out.println("Recipe added successfully!");
    }
}