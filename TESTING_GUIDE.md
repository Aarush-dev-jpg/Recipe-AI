# Testing & Verification Guide

## ✅ Complete Testing Checklist

### Phase 1: Setup Verification

- [ ] **Flask API Installed**
  ```bash
  cd gemini-flask-api
  pip install -r requirements.txt
  ```

- [ ] **Environment Variables Set**
  ```bash
  # Check .env file has:
  GEMINI_API_KEY=your_api_key_here
  ```

- [ ] **Flask Server Started**
  ```bash
  python app.py
  # Should show: * Running on http://0.0.0.0:5000
  ```

---

### Phase 2: API Endpoint Testing

#### Test 1: Health Check
```bash
curl http://localhost:5000/health
```

Expected Response:
```json
{
  "status": "healthy",
  "message": "Gemini Flask API is running",
  "gemini_status": "ready"
}
```

#### Test 2: Get All Recipes
```bash
curl http://localhost:5000/api/recipes
```

Expected Response: Array of recipe objects

#### Test 3: Search by Ingredient (Local)
```bash
curl "http://localhost:5000/api/search?ingredient=spaghetti"
```

Expected: Returns Spaghetti Bolognese recipe

#### Test 4: Search by Ingredient (AI Generation)
```bash
curl "http://localhost:5000/api/search?ingredient=mushrooms"
```

Expected: Generates new mushroom recipe via Gemini

#### Test 5: Search by Category
```bash
curl "http://localhost:5000/api/search?category=italian"
```

Expected: Returns recipes with Italian category

#### Test 6: Generate Recipe
```bash
curl -X POST http://localhost:5000/api/recipe/generate \
  -H "Content-Type: application/json" \
  -d '{"ingredient": "pasta"}'
```

Expected: Generates and returns a pasta-based recipe

#### Test 7: Test Gemini Connection
```bash
curl http://localhost:5000/api/gemini/test
```

Expected:
```json
{
  "status": "success",
  "message": "Gemini API is working",
  "response": "Gemini API is working"
}
```

---

### Phase 3: Frontend Testing

#### Test 1: Load Frontend
- Open browser: `http://localhost:8080`
- Should see: Recipe Recommendation System title
- Should see: All recipes loaded initially

#### Test 2: View Recipes
- Scroll down to see recipes
- Each recipe should show:
  - ✅ Recipe name
  - ✅ Difficulty level
  - ✅ Cooking time
  - ✅ Categories
  - ✅ Ingredients list
  - ✅ Instructions

#### Test 3: Search by Ingredient (Local)
1. Type "spaghetti" in search box
2. Ensure "Search by Ingredient" is selected
3. Click Search
4. Should display Spaghetti Bolognese

#### Test 4: Search by Ingredient (AI)
1. Type "mushrooms" in search box
2. Should show "Searching recipes..."
3. Should display AI-generated recipe
4. Should show 🤖 source badge

#### Test 5: Search by Category
1. Change dropdown to "Search by Category"
2. Type "italian"
3. Click Search
4. Should show Italian recipes

#### Test 6: Error Handling
1. Type invalid ingredient like "xyzabc"
2. Should still generate recipe via AI
3. Or show error message if API fails

#### Test 7: Recipe Actions
- Click "Get Similar Recipes" on any recipe
- Should display similar recipe
- Should update page content

#### Test 8: Share Recipe
- Click "Share" button on any recipe
- Should trigger native share or copy to clipboard
- Should work on mobile

#### Test 9: Responsive Design
1. Press F12 to open developer tools
2. Click responsive design mode
3. Test on different screen sizes:
   - Mobile (375px)
   - Tablet (768px)
   - Desktop (1024px+)
4. Verify layout adjusts properly

#### Test 10: Error Messages
1. Simulate API error by stopping Flask
2. Try searching
3. Should show error message
4. Should suggest checking API status

---

### Phase 4: Performance Testing

#### Test 1: Search Speed
- **Expected**: Local search <100ms
- **Actual**: ___________

#### Test 2: AI Generation Speed
- **Expected**: First generation 2-3 seconds
- **Actual**: ___________

#### Test 3: Multiple Searches
- Perform 10 consecutive searches
- Verify no slowdown
- Check console for memory leaks

#### Test 4: Network Tab
1. Open DevTools Network tab
2. Perform search
3. Verify:
   - [ ] Single API call made
   - [ ] Response time under 5 seconds
   - [ ] Proper status code (200)
   - [ ] Content-Type is application/json

---

### Phase 5: Browser Console Testing

#### Test 1: Check for Errors
1. Open DevTools (F12)
2. Go to Console tab
3. Should see NO red error messages
4. Should see INFO level logs

#### Test 2: Verify API Configuration
```javascript
// In console, type:
console.log(API_SOURCE);
console.log(API_BASE);
```

Expected Output:
```
"flask"
"http://localhost:5000"
```

#### Test 3: Manual API Call
```javascript
// Test search in console
fetch('http://localhost:5000/api/search?ingredient=chicken')
  .then(r => r.json())
  .then(data => console.log(data))
```

Should return array of recipes

#### Test 4: Test Gemini
```javascript
// Check Gemini connection
fetch('http://localhost:5000/api/gemini/test')
  .then(r => r.json())
  .then(data => console.log(data))
```

Should return success status

---

### Phase 6: Integration Testing

#### Test 1: End-to-End Search
1. Load page
2. Search for "pizza"
3. Get result
4. Click "Get Similar Recipes"
5. Get another recipe
6. Verify flow works smoothly

#### Test 2: Multiple API Calls
1. Perform 5 different searches
2. Verify each returns different results
3. Check Flask logs for all requests

#### Test 3: Mixed Searches
1. Search local recipe "spaghetti" → Should be fast
2. Search AI recipe "mushrooms" → Should be slower
3. Verify both work correctly

#### Test 4: Rapid Searches
1. Perform 3 searches within 5 seconds
2. Verify no race conditions
3. Last search result should be displayed

---

## 📋 Response Validation

### Recipe Object Structure

Every recipe should have:
```json
{
  "id": <number>,
  "name": "<string>",
  "ingredients": ["<string>", ...],
  "instructions": "<string with \\n>",
  "cookingTime": <number>,
  "difficulty": "<Easy|Medium|Hard>",
  "categories": ["<string>", ...],
  "source": "<local|gemini>"  // source field indicates if AI-generated
}
```

### Validation Checklist
- [ ] ID is unique
- [ ] Name is non-empty string
- [ ] Ingredients is non-empty array
- [ ] Instructions contains newlines for steps
- [ ] Cooking time is positive number
- [ ] Difficulty is one of 3 options
- [ ] Categories is non-empty array
- [ ] Source indicates origin

---

## 🚨 Issue Resolution

### Issue: API Returns 404
**Solution**: 
```bash
# Check Flask is running
curl http://localhost:5000/health

# Verify endpoint exists
# Check endpoint path spelling in frontend
```

### Issue: Gemini Returns Error
**Solution**:
```bash
# Check API key in .env
# Verify GEMINI_API_KEY is set correctly

# Test Gemini directly
curl http://localhost:5000/api/gemini/test
```

### Issue: CORS Error
**Solution**:
- Flask CORS is pre-enabled
- Verify frontend is calling correct URL
- Check browser console for exact error

### Issue: Empty Search Results
**Solution**:
- Try different ingredient name
- Check if AI generation is triggered
- Verify Gemini API is working
- Check Flask logs for errors

### Issue: Slow Response
**Solution**:
- First Gemini call is slower (2-3s)
- Subsequent calls should be faster
- Check network tab for bottleneck
- Verify Flask is not CPU-bound

---

## 📊 Test Results Template

```
Test Date: ___________
Tester: ___________
Environment: Flask API on port 5000

Phase 1 - Setup:
  Health Check: [ PASS / FAIL ]
  API Endpoints: [ PASS / FAIL ]
  Environment Variables: [ PASS / FAIL ]

Phase 2 - API Tests:
  Health Check: [ PASS / FAIL ]
  Get Recipes: [ PASS / FAIL ]
  Local Search: [ PASS / FAIL ]
  AI Search: [ PASS / FAIL ]
  Category Search: [ PASS / FAIL ]
  Generate Recipe: [ PASS / FAIL ]
  Gemini Test: [ PASS / FAIL ]

Phase 3 - Frontend:
  Load Page: [ PASS / FAIL ]
  View Recipes: [ PASS / FAIL ]
  Local Search: [ PASS / FAIL ]
  AI Search: [ PASS / FAIL ]
  Category Search: [ PASS / FAIL ]
  Similar Recipes: [ PASS / FAIL ]
  Share Recipe: [ PASS / FAIL ]
  Responsive: [ PASS / FAIL ]

Phase 4 - Performance:
  Local Search Speed: ___________ms
  AI Generation Speed: ___________ms
  Multiple Searches: [ PASS / FAIL ]
  Network Tab: [ PASS / FAIL ]

Phase 5 - Console:
  No Errors: [ YES / NO ]
  Proper Logging: [ YES / NO ]
  Manual Tests: [ PASS / FAIL ]

Phase 6 - Integration:
  End-to-End: [ PASS / FAIL ]
  Multiple Calls: [ PASS / FAIL ]
  Mixed Searches: [ PASS / FAIL ]
  Rapid Searches: [ PASS / FAIL ]

Overall Result: [ ✅ PASS / ❌ FAIL ]
Issues Found: ___________
Notes: ___________
```

---

## 🎯 Success Criteria

All of the following must be true:
- [ ] Flask API starts without errors
- [ ] Frontend loads recipes on page load
- [ ] Local search works and is fast
- [ ] AI generation works and generates valid recipes
- [ ] Search results display correctly
- [ ] No JavaScript errors in console
- [ ] No network errors (200 status codes)
- [ ] UI is responsive on mobile
- [ ] Error messages are user-friendly
- [ ] Performance is acceptable

---

**Testing Complete! ✅**

If all tests pass, the system is ready for production.
