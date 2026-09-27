import java.util.*;
import java.util.stream.Collectors;

public class RecipeService {
    private List<Recipe> recipeDatabase;

    public RecipeService() {
        this.recipeDatabase = new ArrayList<>();
        initializeWithSampleRecipes();
    }

    private void initializeWithSampleRecipes() {
        // Add some sample recipes
        List<String> spaghettiIngredients = Arrays.asList("spaghetti", "tomato sauce", "ground beef", "onion", "garlic");
        Recipe spaghetti = new Recipe("Spaghetti Bolognese", spaghettiIngredients,
                "1. Cook pasta\n2. Brown meat\n3. Add sauce\n4. Combine and serve", 30, "Easy");
        spaghetti.setCategories(Arrays.asList("Italian", "Pasta", "Main Course"));
        
        List<String> saladIngredients = Arrays.asList("lettuce", "tomato", "cucumber", "olive oil", "vinegar");
        Recipe salad = new Recipe("Garden Salad", saladIngredients,
                "1. Chop vegetables\n2. Mix in bowl\n3. Add dressing", 10, "Easy");
        salad.setCategories(Arrays.asList("Salad", "Vegetarian", "Healthy"));

        recipeDatabase.add(spaghetti);
        recipeDatabase.add(salad);
    }

    public void addRecipe(Recipe recipe) {
        recipeDatabase.add(recipe);
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipeDatabase);
    }

    public List<Recipe> searchByIngredient(String ingredient) {
        return recipeDatabase.stream()
                .filter(recipe -> recipe.getIngredients().stream()
                        .anyMatch(i -> i.toLowerCase().contains(ingredient.toLowerCase())))
                .collect(Collectors.toList());
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