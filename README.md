# Recipe Recommendation System

A web-based recipe recommendation system that allows users to manage recipes and get recommendations based on categories and ingredients.

## Features

- View all recipes through a web interface
- Search recipes by ingredient or category
- Add new recipes with a user-friendly form
- Get recipe recommendations based on similar categories
- RESTful API endpoints for all operations

## Project Structure

```
recipe-recommender/
└── src/
    ├── main/
        ├── java/
        │   └── com/
        │       └── recipe/
        │           ├── model/
        │           │   └── Recipe.java
        │           ├── service/
        │           │   └── RecipeService.java
        │           ├── servlet/
        │           │   ├── RecipeServlet.java
        │           │   ├── SearchServlet.java
        │           │   └── RecommendationServlet.java
        │           └── WebServer.java
        └── webapp/
            ├── css/
            │   └── style.css
            ├── js/
            │   └── script.js
            └── index.html
```

## Requirements

- Java 11 or higher
- Jetty (included in dependencies)

## Dependencies

- Jetty (Web Server)
- Servlet API
- Gson (JSON processing)

## How to Run

1. First, download the required JAR files:
   - jetty-server-9.4.44.v20210927.jar
   - jetty-servlet-9.4.44.v20210927.jar
   - jetty-util-9.4.44.v20210927.jar
   - jetty-http-9.4.44.v20210927.jar
   - jetty-io-9.4.44.v20210927.jar
   - jetty-security-9.4.44.v20210927.jar
   - jetty-webapp-9.4.44.v20210927.jar
   - jetty-xml-9.4.44.v20210927.jar
   - servlet-api-4.0.1.jar
   - gson-2.8.9.jar

2. Place them in a `lib` folder in the project root.

3. Compile the project:
```bash
javac -cp "lib/*" src/main/java/com/recipe/*/*.java
```

4. Run the web server:
```bash
java -cp "lib/*:src/main/java" com.recipe.WebServer
```

5. Open your web browser and navigate to:
```
http://localhost:8080
```

## API Endpoints

- GET `/api/recipes` - Get all recipes
- POST `/api/recipes` - Add a new recipe
- GET `/api/search?ingredient={ingredient}` - Search by ingredient
- GET `/api/search?category={category}` - Search by category
- GET `/api/recommend?recipe={recipeName}` - Get recipe recommendations

## Usage

The system provides a command-line interface with the following options:

1. View all recipes
2. Search by ingredient
3. Search by category
4. Add new recipe
5. Get recipe recommendations
6. Exit

Follow the on-screen prompts to interact with the system.