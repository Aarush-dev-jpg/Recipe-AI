# Recipe Recommender - Gemini Flask API

A Flask-based REST API for recipe search and generation using Google's Gemini AI.

## Features

- **Local Recipe Search**: Search recipes by ingredient or category from the local database
- **AI Recipe Generation**: When local recipes aren't found, Gemini AI generates new recipes
- **Advanced Search**: Use AI for complex queries with filters
- **Recipe Generation**: Generate recipes on-demand with Gemini
- **Health Check**: Monitor API health
- **Comprehensive Logging**: Track all operations and errors

## Installation

### Prerequisites
- Python 3.8 or higher
- pip (Python package manager)

### Setup

1. **Clone or navigate to the project directory**:
```bash
cd gemini-flask-api
```

2. **Create a virtual environment** (optional but recommended):
```bash
# On Windows
python -m venv venv
venv\Scripts\activate

# On macOS/Linux
python -m venv venv
source venv/bin/activate
```

3. **Install dependencies**:
```bash
pip install -r requirements.txt
```

4. **Configure environment variables**:
   - Create or update the `.env` file with your Gemini API key:
   ```
   GEMINI_API_KEY=your_api_key_here
   FLASK_ENV=development
   FLASK_DEBUG=True
   ```

5. **Run the application**:
```bash
python app.py
```

The API will be available at `http://localhost:5000`

## API Endpoints

### 1. Health Check
```
GET /health
```
Returns the health status of the API.

**Response**:
```json
{
  "status": "healthy",
  "message": "Gemini Flask API is running",
  "gemini_status": "ready"
}
```

### 2. Get All Recipes
```
GET /api/recipes
```
Returns all recipes from the local database.

**Response**:
```json
[
  {
    "id": 1,
    "name": "Spaghetti Bolognese",
    "ingredients": [...],
    "instructions": "...",
    "cookingTime": 45,
    "difficulty": "Easy",
    "categories": ["Italian", "Pasta", "Main Course"]
  }
]
```

### 3. Search Recipes
```
GET /api/search?ingredient=chicken
GET /api/search?category=italian
```

Searches for recipes by ingredient or category. If no local matches are found, Gemini generates a recipe.

**Query Parameters**:
- `ingredient` (string): Search by ingredient name
- `category` (string): Search by category name

**Response**:
```json
[
  {
    "id": 1,
    "name": "Recipe Name",
    "ingredients": [...],
    "instructions": "...",
    "cookingTime": 30,
    "difficulty": "Medium",
    "categories": ["Category1", "Category2"]
  }
]
```

### 4. Advanced Search
```
POST /api/search/advanced
```

Advanced search with AI and filters.

**Request Body**:
```json
{
  "query": "healthy dinner",
  "filters": {
    "difficulty": "Easy",
    "maxCookingTime": 30,
    "dietary": "Vegetarian"
  }
}
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "Recipe Name",
    ...
  }
]
```

### 5. Generate Recipe
```
POST /api/recipe/generate
```

Generate a new recipe using Gemini AI.

**Request Body**:
```json
{
  "ingredient": "mushrooms"
}
```
OR
```json
{
  "description": "quick breakfast for 2 people"
}
```

**Response**:
```json
{
  "id": 4,
  "name": "Recipe Name",
  "ingredients": [...],
  "instructions": "...",
  "cookingTime": 25,
  "difficulty": "Easy",
  "categories": ["Category1", "Category2"],
  "source": "gemini"
}
```

### 6. Test Gemini API
```
GET /api/gemini/test
```

Test the connection to the Gemini API.

**Response**:
```json
{
  "status": "success",
  "message": "Gemini API is working",
  "response": "Gemini API is working"
}
```

## Example Usage

### Using cURL

1. **Get all recipes**:
```bash
curl http://localhost:5000/api/recipes
```

2. **Search for a recipe**:
```bash
curl "http://localhost:5000/api/search?ingredient=chicken"
```

3. **Generate a recipe**:
```bash
curl -X POST http://localhost:5000/api/recipe/generate \
  -H "Content-Type: application/json" \
  -d '{"ingredient": "pasta"}'
```

4. **Advanced search**:
```bash
curl -X POST http://localhost:5000/api/search/advanced \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Italian pasta",
    "filters": {
      "difficulty": "Easy",
      "maxCookingTime": 30
    }
  }'
```

### Using Python

```python
import requests

# Search for recipes
response = requests.get('http://localhost:5000/api/search?ingredient=chicken')
recipes = response.json()

# Generate a recipe
response = requests.post('http://localhost:5000/api/recipe/generate',
                         json={"ingredient": "mushrooms"})
recipe = response.json()

print(recipe['name'])
print(recipe['instructions'])
```

### Using JavaScript/Fetch

```javascript
// Search for recipes
fetch('http://localhost:5000/api/search?ingredient=chicken')
  .then(res => res.json())
  .then(data => console.log(data));

// Generate a recipe
fetch('http://localhost:5000/api/recipe/generate', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({ingredient: 'pasta'})
})
  .then(res => res.json())
  .then(data => console.log(data));
```

## Logging

All requests and operations are logged to the console with timestamps and severity levels (INFO, WARNING, ERROR).

Check the console output to:
- Monitor API activity
- Debug search issues
- Track Gemini API calls and responses
- View error details

Example log output:
```
2024-10-24 10:30:45,123 - __main__ - INFO - GET /api/search - ingredient: chicken, category: 
2024-10-24 10:30:45,124 - __main__ - INFO - Searching local database for ingredient: chicken
2024-10-24 10:30:45,125 - __main__ - INFO - Found local recipe: Chicken Stir Fry
2024-10-24 10:30:45,126 - __main__ - INFO - Returning 1 recipe(s)
```

## Error Handling

The API returns appropriate HTTP status codes and error messages:

- `400`: Bad Request (missing required parameters)
- `404`: Endpoint not found
- `500`: Internal Server Error
- `200`: Success

Error Response Format:
```json
{
  "error": "Description of the error"
}
```

## Architecture

- **Frontend**: JavaScript/HTML (Vue, React, etc.)
- **API Gateway**: Flask (Python) on port 5000
- **AI Engine**: Google Gemini 2.5 Flash
- **Database**: In-memory (easily replaceable with SQLite, PostgreSQL, etc.)

## Integration with Java Backend

This Flask API can be used as a fallback or replacement for the Java-based search, or run alongside it:

1. **Option 1**: Use Flask for search/generation, keep Java for main app
2. **Option 2**: Call Flask from Java for AI features
3. **Option 3**: Fully migrate to Flask

### Cross-Origin Setup

CORS is enabled by default to allow requests from the Java app and other frontends.

## Troubleshooting

### "GEMINI_API_KEY not found"
- Ensure `.env` file exists in the same directory as `app.py`
- Verify the API key is correct

### "Failed to parse Gemini response"
- Check the logs for the full response
- Gemini might be returning non-JSON content
- Ensure your prompt clearly specifies JSON format

### No local recipes in search results
- The local database has a few default recipes
- Search uses case-insensitive substring matching
- If not found, Gemini should generate one

## Performance Tips

1. Cache generated recipes in a real database
2. Implement rate limiting for Gemini API calls
3. Use async processing for long-running operations
4. Add caching layer (Redis) for frequent queries

## License

MIT

## Support

For issues or questions, check the logs first. The application provides detailed logging for debugging.
