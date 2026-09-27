"""
Flask application for recipe search with Gemini AI integration
"""
from flask import Flask, request, jsonify
from flask_cors import CORS
from google import genai
import os
import json
import logging
from dotenv import load_dotenv
from datetime import datetime

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize Flask app
app = Flask(__name__)
CORS(app)

# Initialize Gemini client
API_KEY = os.getenv('GEMINI_API_KEY')
if not API_KEY:
    logger.error("GEMINI_API_KEY not found in environment variables")
    raise ValueError("GEMINI_API_KEY is required")

client = genai.Client(api_key=API_KEY)
logger.info("Gemini client initialized successfully")

# In-memory feedback database for learning
FEEDBACK_DATABASE = []
RECIPE_FEEDBACK_INDEX = {}  # Map recipe to feedbacks

# In-memory recipe database (for fallback)
RECIPE_DATABASE = [
    {
        "id": 1,
        "name": "Spaghetti Bolognese",
        "ingredients": ["400g spaghetti", "400g ground beef", "2 cans tomato sauce", "1 onion", "3 cloves garlic"],
        "instructions": "1. Boil pasta\n2. Brown beef\n3. Add sauce\n4. Simmer for 20 minutes\n5. Combine and serve",
        "cookingTime": 45,
        "difficulty": "Easy",
        "categories": ["Italian", "Pasta", "Main Course"]
    },
    {
        "id": 2,
        "name": "Chicken Stir Fry",
        "ingredients": ["500g chicken", "2 cups vegetables", "3 tbsp soy sauce", "2 tbsp oyster sauce", "rice"],
        "instructions": "1. Heat wok\n2. Add chicken\n3. Add vegetables\n4. Add sauces\n5. Serve with rice",
        "cookingTime": 25,
        "difficulty": "Medium",
        "categories": ["Asian", "Chinese", "Main Course"]
    },
    {
        "id": 3,
        "name": "Mediterranean Salad",
        "ingredients": ["2 cups lettuce", "2 tomatoes", "1 cucumber", "feta cheese", "olive oil", "vinegar"],
        "instructions": "1. Chop vegetables\n2. Mix in bowl\n3. Add feta\n4. Dress with oil and vinegar\n5. Serve",
        "cookingTime": 15,
        "difficulty": "Easy",
        "categories": ["Salad", "Vegetarian", "Healthy"]
    }
]


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    logger.info("Health check requested")
    return jsonify({
        "status": "healthy",
        "message": "Gemini Flask API is running",
        "gemini_status": "ready"
    }), 200


@app.route('/api/recipes', methods=['GET'])
def get_all_recipes():
    """Get all recipes from the database"""
    logger.info("GET /api/recipes - Retrieving all recipes")
    try:
        return jsonify(RECIPE_DATABASE), 200
    except Exception as e:
        logger.error(f"Error retrieving recipes: {str(e)}")
        return jsonify({"error": str(e)}), 500


@app.route('/api/search', methods=['GET'])
def search_recipes():
    """
    Search for recipes by ingredient or category
    Query parameters:
        - ingredient: search by ingredient name
        - category: search by category name
    """
    ingredient = request.args.get('ingredient', '').strip()
    category = request.args.get('category', '').strip()
    
    logger.info(f"GET /api/search - ingredient: {ingredient}, category: {category}")
    
    if not ingredient and not category:
        logger.warning("Search requested without ingredient or category parameter")
        return jsonify({"error": "Must provide either 'ingredient' or 'category' parameter"}), 400
    
    try:
        results = []
        
        # Search in local database first
        if ingredient:
            logger.info(f"Searching local database for ingredient: {ingredient}")
            for recipe in RECIPE_DATABASE:
                for ing in recipe.get("ingredients", []):
                    if ingredient.lower() in ing.lower():
                        results.append(recipe)
                        logger.info(f"Found local recipe: {recipe['name']}")
                        break
        
        elif category:
            logger.info(f"Searching local database for category: {category}")
            for recipe in RECIPE_DATABASE:
                for cat in recipe.get("categories", []):
                    if category.lower() in cat.lower():
                        results.append(recipe)
                        logger.info(f"Found local recipe: {recipe['name']}")
                        break
        
        # If no local results found, use Gemini to generate a recipe
        if not results:
            logger.info(f"No local results found. Requesting Gemini to generate recipe for: {ingredient or category}")
            generated_recipe = generate_recipe_with_gemini(ingredient or category)
            
            if generated_recipe:
                results.append(generated_recipe)
                logger.info(f"Gemini generated recipe: {generated_recipe['name']}")
            else:
                logger.warning(f"Gemini failed to generate recipe for: {ingredient or category}")
        
        logger.info(f"Returning {len(results)} recipe(s)")
        return jsonify(results), 200
    
    except Exception as e:
        logger.error(f"Error searching recipes: {str(e)}", exc_info=True)
        return jsonify({"error": str(e)}), 500


@app.route('/api/search/advanced', methods=['POST'])
def advanced_search():
    """
    Advanced search endpoint using Gemini AI
    JSON body:
    {
        "query": "search query",
        "filters": {
            "difficulty": "Easy/Medium/Hard",
            "maxCookingTime": number,
            "dietary": "Vegetarian/Vegan/etc"
        }
    }
    """
    try:
        data = request.get_json()
        query = data.get('query', '').strip()
        filters = data.get('filters', {})
        
        logger.info(f"POST /api/search/advanced - query: {query}, filters: {filters}")
        
        if not query:
            return jsonify({"error": "Query is required"}), 400
        
        # Build prompt with filters
        prompt = f"Find recipes for: {query}"
        if filters.get('difficulty'):
            prompt += f" with {filters['difficulty']} difficulty"
        if filters.get('maxCookingTime'):
            prompt += f" that takes less than {filters['maxCookingTime']} minutes"
        if filters.get('dietary'):
            prompt += f" that is {filters['dietary']}"
        
        logger.info(f"Calling Gemini with prompt: {prompt}")
        generated_recipe = generate_recipe_with_gemini(prompt)
        
        if generated_recipe:
            logger.info(f"Advanced search generated recipe: {generated_recipe['name']}")
            return jsonify([generated_recipe]), 200
        else:
            logger.warning("Advanced search failed to generate recipe")
            return jsonify({"error": "Could not generate recipe for query"}), 500
    
    except Exception as e:
        logger.error(f"Error in advanced search: {str(e)}", exc_info=True)
        return jsonify({"error": str(e)}), 500


@app.route('/api/gemini/test', methods=['GET'])
def test_gemini():
    """Test Gemini API connection"""
    logger.info("GET /api/gemini/test - Testing Gemini API")
    try:
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents="Respond with 'Gemini API is working' if you receive this message.",
        )
        
        logger.info(f"Gemini test response: {response.text}")
        return jsonify({
            "status": "success",
            "message": "Gemini API is working",
            "response": response.text
        }), 200
    
    except Exception as e:
        logger.error(f"Gemini API test failed: {str(e)}", exc_info=True)
        return jsonify({
            "status": "error",
            "message": "Gemini API test failed",
            "error": str(e)
        }), 500


@app.route('/api/recipe/generate', methods=['POST'])
def generate_recipe():
    """
    Generate a recipe using Gemini AI
    JSON body:
    {
        "ingredient": "ingredient name" or
        "description": "recipe description"
    }
    """
    try:
        data = request.get_json()
        ingredient = data.get('ingredient', '').strip()
        description = data.get('description', '').strip()
        
        search_query = ingredient or description
        
        logger.info(f"POST /api/recipe/generate - query: {search_query}")
        
        if not search_query:
            return jsonify({"error": "Either 'ingredient' or 'description' is required"}), 400
        
        generated_recipe = generate_recipe_with_gemini(search_query)
        
        if generated_recipe:
            logger.info(f"Generated recipe: {generated_recipe['name']}")
            return jsonify(generated_recipe), 200
        else:
            logger.warning("Failed to generate recipe")
            return jsonify({"error": "Could not generate recipe"}), 500
    
    except Exception as e:
        logger.error(f"Error generating recipe: {str(e)}", exc_info=True)
        return jsonify({"error": str(e)}), 500


def generate_recipe_with_gemini(search_query):
    """
    Generate a recipe using Gemini AI
    
    Args:
        search_query: The ingredient or query to search for
    
    Returns:
        dict: Recipe object or None if generation fails
    """
    try:
        prompt = f"""Generate a detailed recipe for a dish that uses or is related to: {search_query}

Please respond with ONLY a valid JSON object (no markdown, no extra text) in this exact format:
{{
  "name": "Recipe Name",
  "ingredients": ["ingredient 1 with quantity", "ingredient 2 with quantity", ...],
  "instructions": "1. First step\\n2. Second step\\n3. Third step...",
  "cookingTime": number_in_minutes,
  "difficulty": "Easy" or "Medium" or "Hard",
  "categories": ["category1", "category2", "category3"]
}}"""
        
        logger.info(f"Sending prompt to Gemini: {search_query}")
        
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=prompt,
        )
        
        response_text = response.text.strip()
        logger.info(f"Gemini response: {response_text[:200]}...")
        
        # Remove markdown code blocks if present
        if response_text.startswith("```json"):
            response_text = response_text[7:]
        if response_text.startswith("```"):
            response_text = response_text[3:]
        if response_text.endswith("```"):
            response_text = response_text[:-3]
        
        response_text = response_text.strip()
        
        # Parse JSON response
        recipe_data = json.loads(response_text)
        
        # Add id
        recipe_data['id'] = len(RECIPE_DATABASE) + 1
        recipe_data['source'] = 'gemini'
        
        logger.info(f"Successfully generated recipe: {recipe_data.get('name')}")
        return recipe_data
    
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse Gemini response as JSON: {str(e)}")
        logger.error(f"Response text: {response_text if 'response_text' in locals() else 'N/A'}")
        return None
    except Exception as e:
        logger.error(f"Error generating recipe with Gemini: {str(e)}", exc_info=True)
        return None


@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors"""
    logger.warning(f"404 Not Found: {request.path}")
    return jsonify({"error": "Endpoint not found"}), 404


@app.errorhandler(500)
def internal_error(error):
    """Handle 500 errors"""
    logger.error(f"500 Internal Server Error: {str(error)}", exc_info=True)
    return jsonify({"error": "Internal server error"}), 500


# ============ FEEDBACK ENDPOINTS FOR LEARNING ============

@app.route('/api/feedback', methods=['POST'])
def submit_feedback():
    """
    Submit feedback for a recipe for AI learning
    Request: { recipeId, recipeName, rating, comment, category, suggestedChanges }
    """
    try:
        data = request.json
        logger.info(f"Received feedback for recipe: {data.get('recipeName')} - Rating: {data.get('rating')}/5")
        
        # Validate feedback
        if not data.get('recipeId') or not data.get('rating'):
            return jsonify({"error": "Missing required fields"}), 400
        
        if not 1 <= data.get('rating', 0) <= 5:
            return jsonify({"error": "Rating must be between 1 and 5"}), 400
        
        # Create feedback object
        feedback = {
            "id": f"feedback_{len(FEEDBACK_DATABASE)}",
            "recipeId": data.get('recipeId'),
            "recipeName": data.get('recipeName'),
            "rating": data.get('rating'),
            "comment": data.get('comment', ''),
            "category": data.get('category', 'general'),  # taste, difficulty, clarity, ingredients, instructions
            "suggestedChanges": data.get('suggestedChanges', ''),
            "timestamp": datetime.now().isoformat(),
            "helpful": data.get('rating', 0) >= 3
        }
        
        # Store feedback
        FEEDBACK_DATABASE.append(feedback)
        if feedback['recipeId'] not in RECIPE_FEEDBACK_INDEX:
            RECIPE_FEEDBACK_INDEX[feedback['recipeId']] = []
        RECIPE_FEEDBACK_INDEX[feedback['recipeId']].append(feedback)
        
        logger.info(f"Feedback saved successfully. Total feedbacks: {len(FEEDBACK_DATABASE)}")
        
        return jsonify({
            "success": True,
            "message": "Feedback submitted successfully",
            "feedbackId": feedback['id']
        }), 201
        
    except Exception as e:
        logger.error(f"Error submitting feedback: {str(e)}", exc_info=True)
        return jsonify({"error": str(e)}), 500


@app.route('/api/feedback', methods=['GET'])
def get_feedback():
    """
    Get feedback data
    Query params:
        - recipeId: Get feedback for specific recipe
        - action: statistics, insights, all
    """
    try:
        recipe_id = request.args.get('recipeId')
        action = request.args.get('action', 'all')
        
        logger.info(f"Getting feedback - recipeId: {recipe_id}, action: {action}")
        
        if action == 'statistics' and recipe_id:
            # Get statistics for specific recipe
            feedbacks = RECIPE_FEEDBACK_INDEX.get(recipe_id, [])
            if not feedbacks:
                return jsonify({
                    "total": 0,
                    "averageRating": 0,
                    "helpful": 0,
                    "unhelpful": 0
                }), 200
            
            ratings = [f['rating'] for f in feedbacks]
            avg_rating = sum(ratings) / len(ratings) if ratings else 0
            helpful_count = sum(1 for f in feedbacks if f['helpful'])
            
            stats = {
                "total": len(feedbacks),
                "averageRating": round(avg_rating, 1),
                "helpful": helpful_count,
                "unhelpful": len(feedbacks) - helpful_count
            }
            logger.info(f"Statistics for recipe {recipe_id}: {stats}")
            return jsonify(stats), 200
            
        elif action == 'insights':
            # Get learning insights
            if not FEEDBACK_DATABASE:
                return jsonify({
                    "insights": "No feedback available yet for learning.",
                    "totalFeedback": 0
                }), 200
            
            insights = generate_learning_insights()
            return jsonify({
                "insights": insights,
                "totalFeedback": len(FEEDBACK_DATABASE)
            }), 200
            
        elif recipe_id:
            # Get all feedback for specific recipe
            feedbacks = RECIPE_FEEDBACK_INDEX.get(recipe_id, [])
            logger.info(f"Retrieved {len(feedbacks)} feedbacks for recipe {recipe_id}")
            return jsonify(feedbacks), 200
            
        else:
            # Get all feedback
            logger.info(f"Retrieved all {len(FEEDBACK_DATABASE)} feedbacks")
            return jsonify(FEEDBACK_DATABASE), 200
            
    except Exception as e:
        logger.error(f"Error getting feedback: {str(e)}", exc_info=True)
        return jsonify({"error": str(e)}), 500


def generate_learning_insights():
    """Generate insights from feedback for AI learning"""
    if not FEEDBACK_DATABASE:
        return "No feedback available yet."
    
    insights = "\n=== FEEDBACK LEARNING INSIGHTS ===\n"
    insights += f"Total Feedbacks: {len(FEEDBACK_DATABASE)}\n"
    
    # Most common issues
    issue_count = {}
    for feedback in FEEDBACK_DATABASE:
        if not feedback['helpful']:
            category = feedback.get('category', 'general')
            issue_count[category] = issue_count.get(category, 0) + 1
    
    if issue_count:
        insights += "\nMost Common Issues:\n"
        for category, count in sorted(issue_count.items(), key=lambda x: x[1], reverse=True):
            insights += f"  - {category}: {count} times\n"
    
    # High-rated recipes
    recipe_ratings = {}
    for recipe_id, feedbacks in RECIPE_FEEDBACK_INDEX.items():
        ratings = [f['rating'] for f in feedbacks]
        avg_rating = sum(ratings) / len(ratings) if ratings else 0
        recipe_ratings[recipe_id] = avg_rating
    
    if recipe_ratings:
        insights += "\nHighest Rated Recipes:\n"
        for recipe_id, rating in sorted(recipe_ratings.items(), key=lambda x: x[1], reverse=True)[:5]:
            insights += f"  - {recipe_id}: {rating:.1f}/5\n"
    
    insights += "\n=== USE THIS TO IMPROVE FUTURE RECIPE GENERATION ===\n"
    return insights



if __name__ == '__main__':
    logger.info("Starting Flask application on http://0.0.0.0:5000")
    app.run(host='0.0.0.0', port=5000, debug=True)
