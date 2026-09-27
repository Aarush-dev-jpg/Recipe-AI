# Quick Start Guide - Recipe Recommender

## 🚀 Get Started in 3 Steps

### Step 1: Start the Flask API (Recommended)

```bash
cd gemini-flask-api
pip install -r requirements.txt
python app.py
```

**Output should show:**
```
* Running on http://0.0.0.0:5000
```

### Step 2: Open the Frontend

Open your browser and go to:
```
http://localhost:8080
```

### Step 3: Start Using!

1. ✅ View all recipes by default
2. 🔍 Search for an ingredient (e.g., "chicken")
3. 🤖 If not found, AI will generate a recipe
4. ➕ Add your own recipes

---

## 📋 What You Can Do

| Action | Steps |
|--------|-------|
| **Search for a recipe** | Type ingredient → Select type → Click Search |
| **Generate recipe with AI** | Search for any ingredient (if not found, AI creates one) |
| **View recipe details** | Recipes show ingredients, instructions, difficulty, cooking time |
| **Get similar recipes** | Click "Get Similar Recipes" button |
| **Share a recipe** | Click "Share" button |
| **Add new recipe** | Click "Add Recipe" button and fill the form |

---

## 🔧 Configuration

### Use Flask API (Default - Recommended)
```javascript
// In script.js - Already set to this
const API_SOURCE = 'flask';
```

### Or Use Java Backend
```javascript
// In script.js - Change to this
const API_SOURCE = 'java';
```

Then start Java server:
```bash
./gradlew clean build
java -jar build/libs/recipe-recommender.jar
```

---

## 🧪 Test the API

### Check Flask Health
```bash
curl http://localhost:5000/health
```

### Search for Recipes
```bash
curl "http://localhost:5000/api/search?ingredient=chicken"
```

### Generate Recipe with AI
```bash
curl -X POST http://localhost:5000/api/recipe/generate \
  -H "Content-Type: application/json" \
  -d '{"ingredient": "pasta"}'
```

---

## 📝 Project Structure

```
recipe-recommender/
├── src/main/webapp/          # Frontend (HTML, CSS, JS)
│   ├── index.html            # Main page
│   ├── css/style.css         # Styling
│   └── js/script.js          # Frontend logic
├── gemini-flask-api/         # Flask backend
│   ├── app.py                # Flask server
│   ├── requirements.txt       # Python dependencies
│   ├── .env                  # API keys
│   └── README.md             # Flask API docs
├── src/main/java/            # Java backend (optional)
├── build.gradle              # Java build config
└── FRONTEND_INTEGRATION_GUIDE.md  # Detailed guide
```

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Cannot reach API" | Make sure Flask is running on `http://localhost:5000` |
| "Recipe not found" | Try different ingredient, or let AI generate one |
| "CORS error" | Flask CORS is enabled, make sure you're using Flask API |
| "No recipes display" | Check console (F12) for errors, restart Flask |
| "Gemini not working" | Check `.env` has correct `GEMINI_API_KEY` |

---

## 📚 API Endpoints

### Flask (Port 5000)
- `GET /health` - Check API status
- `GET /api/recipes` - Get all recipes
- `GET /api/search?ingredient=X` - Search recipes
- `POST /api/recipe/generate` - Generate with AI
- `GET /api/gemini/test` - Test Gemini connection

### Java (Port 8080)
- `GET /api/recipes` - Get all recipes
- `GET /api/search?ingredient=X` - Search recipes

---

## 💡 Tips

1. **First Time?** Start with Flask API (includes sample recipes)
2. **Search Tips** - Try "chicken", "pasta", "salad", "italian"
3. **AI Generation** - Search for any ingredient, AI will create a recipe
4. **Debug** - Press F12 in browser to see console logs

---

## 📖 Full Documentation

- **Flask API**: See `gemini-flask-api/README.md`
- **Frontend Integration**: See `FRONTEND_INTEGRATION_GUIDE.md`
- **Java Backend**: See `README.md`

---

## 🎉 Ready?

Just run the Flask API and open your browser!

```bash
# Terminal 1
cd gemini-flask-api
python app.py

# Then open in browser
http://localhost:8080
```

**Happy cooking! 🍳**
