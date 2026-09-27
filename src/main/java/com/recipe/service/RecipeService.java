package com.recipe.service;

import com.recipe.model.Recipe;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeService {
    private static final RecipeService instance = new RecipeService();
    private final List<Recipe> recipeDatabase;

    private RecipeService() {
        this.recipeDatabase = new ArrayList<>();
        initializeWithSampleRecipes();
    }

    public static RecipeService getInstance() {
        return instance;
    }

    private void initializeWithSampleRecipes() {
        // Italian Recipes
        List<String> spaghettiIngredients = Arrays.asList("400g spaghetti", "400g ground beef", "2 cans tomato sauce", "1 onion, diced", "3 cloves garlic, minced", "2 tbsp olive oil", "1 tsp dried oregano", "Salt and pepper to taste", "Grated Parmesan cheese");
        Recipe spaghetti = new Recipe("Spaghetti Bolognese", spaghettiIngredients,
                "1. Boil pasta according to package instructions\n2. Heat oil and sauté onion and garlic\n3. Add ground beef and brown\n4. Add tomato sauce and seasonings\n5. Simmer for 20 minutes\n6. Combine with pasta and serve with Parmesan", 45, "Easy");
        spaghetti.setCategories(Arrays.asList("Italian", "Pasta", "Main Course"));
        
        // Salads
        List<String> saladIngredients = Arrays.asList("2 cups mixed lettuce", "2 tomatoes, diced", "1 cucumber, sliced", "1/4 cup olive oil", "2 tbsp balsamic vinegar", "1/2 red onion, sliced", "100g feta cheese", "Salt and pepper to taste");
        Recipe salad = new Recipe("Mediterranean Garden Salad", saladIngredients,
                "1. Wash and chop all vegetables\n2. Combine in a large bowl\n3. Crumble feta cheese over top\n4. Mix olive oil and vinegar for dressing\n5. Season and toss before serving", 15, "Easy");
        salad.setCategories(Arrays.asList("Salad", "Vegetarian", "Healthy", "Mediterranean"));

        // Asian Recipes
        List<String> stirFryIngredients = Arrays.asList("500g chicken breast, diced", "2 cups mixed vegetables", "3 tbsp soy sauce", "2 tbsp oyster sauce", "2 cloves garlic, minced", "1 tbsp ginger, grated", "2 tbsp vegetable oil", "Cooked rice for serving");
        Recipe stirFry = new Recipe("Chicken Stir Fry", stirFryIngredients,
                "1. Heat oil in wok or large pan\n2. Add garlic and ginger, stir-fry until fragrant\n3. Add chicken and cook until golden\n4. Add vegetables and stir-fry until tender-crisp\n5. Add sauces and stir to combine\n6. Serve hot over rice", 25, "Medium");
        stirFry.setCategories(Arrays.asList("Asian", "Chinese", "Main Course", "Chicken"));

        // Mexican Recipes
        List<String> tacoIngredients = Arrays.asList("500g ground beef", "8 taco shells", "1 packet taco seasoning", "1 cup shredded lettuce", "1 cup diced tomatoes", "1 cup shredded cheese", "1/2 cup sour cream", "1 avocado, sliced", "Hot sauce to taste");
        Recipe tacos = new Recipe("Classic Beef Tacos", tacoIngredients,
                "1. Brown ground beef in a pan\n2. Add taco seasoning and cook according to package\n3. Warm taco shells in the oven\n4. Assemble tacos with meat and toppings\n5. Serve with sour cream and hot sauce", 30, "Easy");
        tacos.setCategories(Arrays.asList("Mexican", "Main Course", "Beef"));

        // Desserts
        List<String> brownieIngredients = Arrays.asList("200g dark chocolate", "180g butter", "220g sugar", "3 eggs", "110g flour", "30g cocoa powder", "1 tsp vanilla extract", "Pinch of salt");
        Recipe brownies = new Recipe("Fudgy Chocolate Brownies", brownieIngredients,
                "1. Preheat oven to 180°C (350°F)\n2. Melt chocolate and butter together\n3. Whisk in sugar and eggs\n4. Fold in flour, cocoa powder, and salt\n5. Pour into lined baking pan\n6. Bake for 25-30 minutes\n7. Cool before cutting", 45, "Medium");
        brownies.setCategories(Arrays.asList("Dessert", "Baking", "Chocolate"));

        // Breakfast
        List<String> pancakeIngredients = Arrays.asList("200g flour", "2 eggs", "300ml milk", "1 tbsp sugar", "2 tsp baking powder", "Pinch of salt", "Butter for cooking", "Maple syrup for serving");
        Recipe pancakes = new Recipe("Fluffy American Pancakes", pancakeIngredients,
                "1. Mix dry ingredients in a bowl\n2. Whisk eggs and milk separately\n3. Combine wet and dry ingredients\n4. Heat pan and add butter\n5. Cook pancakes until bubbles form\n6. Flip and cook other side\n7. Serve with maple syrup", 25, "Easy");
        pancakes.setCategories(Arrays.asList("Breakfast", "American", "Vegetarian"));

        // Add all recipes to database
        recipeDatabase.addAll(Arrays.asList(
            spaghetti,
            salad,
            stirFry,
            tacos,
            brownies,
            pancakes
        ));
    }

    public void addRecipe(Recipe recipe) {
        // If no ingredients are provided, try to generate them using Gemini
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            try {
                List<String> generatedIngredients = GeminiService.getInstance().generateIngredients(recipe.getName());
                if (!generatedIngredients.isEmpty()) {
                    recipe.setIngredients(generatedIngredients);
                }
            } catch (Exception e) {
                // If Gemini fails, continue with empty ingredients
                recipe.setIngredients(new ArrayList<>());
            }
        }
        recipeDatabase.add(recipe);
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipeDatabase);
    }

    public List<Recipe> searchByIngredient(String ingredient) {
        // First search local database
        List<Recipe> localResults = recipeDatabase.stream()
                .filter(recipe -> recipe.getIngredients().stream()
                        .anyMatch(i -> i.toLowerCase().contains(ingredient.toLowerCase())))
                .collect(Collectors.toList());

        // If no local results, try Gemini
        if (localResults.isEmpty()) {
            try {
                Recipe generatedRecipe = GeminiService.getInstance().generateRecipeByIngredient(ingredient);
                if (generatedRecipe != null) {
                    // Add to database for future searches
                    addRecipe(generatedRecipe);
                    localResults.add(generatedRecipe);
                }
            } catch (Exception e) {
                System.err.println("Failed to generate recipe using Gemini: " + e.getMessage());
            }
        }

        return localResults;
    }

    public List<Recipe> searchByCategory(String category) {
        return recipeDatabase.stream()
                .filter(recipe -> recipe.getCategories().stream()
                        .anyMatch(c -> c.toLowerCase().contains(category.toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<Recipe> recommendSimilarRecipes(Recipe recipe) {
        // Simple recommendation based on matching categories
        Set<String> recipeCategories = new HashSet<>(recipe.getCategories());
        return recipeDatabase.stream()
                .filter(r -> !r.getName().equals(recipe.getName())) // Exclude the same recipe
                .filter(r -> !Collections.disjoint(r.getCategories(), recipeCategories)) // Find recipes with matching categories
                .collect(Collectors.toList());
    }
}