# Frontend Integration Guide

## Overview

The frontend has been updated to integrate with the **Flask Gemini API**. The application can work with either the Flask backend (recommended for Gemini AI features) or the Java backend.

## Configuration

### 1. Switch Between APIs

Edit `src/main/webapp/js/script.js` and change this line:

```javascript
const API_SOURCE = 'flask'; // Change to 'java' to use Java backend
```

- `'flask'`: Uses Flask API at `http://localhost:5000` (recommended - includes Gemini AI)
- `'java'`: Uses Java API at `http://localhost:8080` (local recipes only)

### 2. Flask API Setup (Recommended)

**Start the Flask API:**

```bash
cd gemini-flask-api
python -m venv venv
venv\Scripts\activate  # On Windows
pip install -r requirements.txt
python app.py
```

Flask will run on `http://localhost:5000`

### 3. Java API Setup (Optional)

**Start the Java server:**

```bash
cd ..
./gradlew clean build
java -jar build/libs/recipe-recommender.jar
```

Java server will run on `http://localhost:8080`

## Features Comparison

| Feature | Flask | Java |
|---------|-------|------|
| Search by Ingredient | ✅ | ✅ |
| Search by Category | ✅ | ✅ |
| AI Recipe Generation | ✅ | ❌ |
| Add Recipes | ✅ | ❌ |
| Get Recommendations | ✅ | ❌ |
| Local Database | ✅ | ✅ |

## Frontend Endpoints Used

### When using Flask (`API_SOURCE = 'flask'`):

- `GET http://localhost:5000/health` - Health check
- `GET http://localhost:5000/api/recipes` - Get all recipes
- `GET http://localhost:5000/api/search?ingredient=...` - Search by ingredient
- `GET http://localhost:5000/api/search?category=...` - Search by category
- `POST http://localhost:5000/api/recipe/generate` - Generate recipe with AI
- `GET http://localhost:5000/api/gemini/test` - Test Gemini connection

### When using Java (`API_SOURCE = 'java'`):

- `GET http://localhost:8080/api/recipes` - Get all recipes
- `GET http://localhost:8080/api/search?ingredient=...` - Search by ingredient
- `GET http://localhost:8080/api/search?category=...` - Search by category

## User Interface Updates

### New Features in Frontend:

1. **API Status Indicator** - Shows current API connection status
2. **Loading States** - Displays "Searching recipes..." while loading
3. **Error Handling** - Shows detailed error messages
4. **Recipe Source** - Displays 🤖 for AI-generated recipes
5. **Recipe Actions** - Share and Get Similar Recipes buttons
6. **Better Styling** - Improved CSS with loading/error states

## Testing the Integration

### 1. Test Flask Search:

```javascript
// In browser console
fetch('http://localhost:5000/api/search?ingredient=chicken')
  .then(r => r.json())
  .then(data => console.log(data))
```

### 2. Test Recipe Generation:

```javascript
// In browser console
fetch('http://localhost:5000/api/recipe/generate', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({ingredient: 'pasta'})
})
  .then(r => r.json())
  .then(data => console.log(data))
```

### 3. Check Gemini Connection:

```javascript
// In browser console
fetch('http://localhost:5000/api/gemini/test')
  .then(r => r.json())
  .then(data => console.log(data))
```

## Troubleshooting

### Problem: "Cannot find the API"

**Solution:**
1. Check if Flask is running: `http://localhost:5000/health`
2. Check if Java is running: `http://localhost:8080/api/recipes`
3. Verify `API_SOURCE` is set correctly in `script.js`
4. Check browser console (F12) for detailed errors

### Problem: Recipe search returns empty

**Solution:**
1. Make sure you're using Flask API (Flask has local recipes)
2. Check if search term matches exactly (case-insensitive)
3. If not found locally, Gemini should generate one
4. Check `http://localhost:5000/api/gemini/test` to verify Gemini works

### Problem: CORS error

**Solution:**
- Flask already has CORS enabled
- If using Java, add CORS headers to the Java backend
- Or use a CORS proxy

### Problem: Getting JSON parse errors

**Solution:**
1. Check Flask server logs for response format
2. Verify Gemini API is working: `http://localhost:5000/api/gemini/test`
3. Check `.env` file has correct `GEMINI_API_KEY`

## Architecture Diagram

```
┌─────────────────────────────────────┐
│        Browser/Frontend             │
│  (HTML, CSS, JavaScript)            │
└────────────┬────────────────────────┘
             │
             ├─────────────────────────────────────────┐
             │                                         │
             ▼                                         ▼
    ┌────────────────────┐              ┌─────────────────────┐
    │   Flask API        │              │   Java Backend      │
    │ (Port 5000)        │              │  (Port 8080)        │
    │                    │              │                     │
    │  - Search          │              │  - Search           │
    │  - Gemini AI       │              │  - Recipes          │
    │  - Generate        │              │  - Add Recipes      │
    └────────┬───────────┘              └──────────┬──────────┘
             │                                     │
             ▼                                     ▼
    ┌────────────────────┐              ┌─────────────────────┐
    │   Gemini API       │              │  In-Memory DB       │
    │ (Google Cloud)     │              │  (Local Database)   │
    └────────────────────┘              └─────────────────────┘
```

## Frontend JavaScript Functions

### Core Functions:

- `loadAllRecipes()` - Fetch all recipes from API
- `searchRecipes()` - Search recipes based on input
- `displayRecipes(recipes)` - Render recipes on page
- `addRecipe()` - Add/generate new recipe
- `getRecommendations(recipeName)` - Get similar recipes
- `shareRecipe(recipeName)` - Share recipe
- `checkAPIHealth()` - Verify API connection

### Configuration:

```javascript
const FLASK_API_BASE = 'http://localhost:5000';
const JAVA_API_BASE = '/';
const API_SOURCE = 'flask'; // Change this to switch APIs
```

## Running Everything Together

### Terminal 1: Start Flask API
```bash
cd gemini-flask-api
python app.py
```

### Terminal 2: Start Java Server (Optional)
```bash
./gradlew clean build
java -jar build/libs/recipe-recommender.jar
```

### Terminal 3: Open Frontend
```bash
# Open http://localhost:8080 (if Java server running)
# Or http://localhost:5000 (if serving frontend from Flask)
```

## Notes

- Flask API is recommended for full features (Gemini AI)
- Java backend can still be used if preferred
- Frontend automatically detects API issues and shows errors
- All API calls include comprehensive error handling
- Logs appear in browser console (F12) and server terminal

## API Response Format

All responses follow this JSON format:

```json
{
  "id": 1,
  "name": "Recipe Name",
  "ingredients": ["item 1", "item 2"],
  "instructions": "Step 1\nStep 2",
  "cookingTime": 30,
  "difficulty": "Easy",
  "categories": ["Italian", "Pasta"],
  "source": "gemini"  // or "local"
}
```

---

**Frontend Integration Complete! 🎉**

You can now search, generate recipes with AI, and manage them all through the web interface.
