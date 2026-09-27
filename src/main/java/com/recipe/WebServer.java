package com.recipe;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.HandlerList;

public class WebServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        // Create the ServletContextHandler for API endpoints
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Add servlets
        context.addServlet(new ServletHolder(new com.recipe.servlet.RecipeServlet()), "/api/recipes");
        context.addServlet(new ServletHolder(new com.recipe.servlet.SearchServlet()), "/api/search");
        context.addServlet(new ServletHolder(new com.recipe.servlet.RecommendationServlet()), "/api/recommend");
        context.addServlet(new ServletHolder(new com.recipe.servlet.GeminiRecommendationServlet()), "/api/gemini/describe");
        context.addServlet(new ServletHolder(new com.recipe.servlet.GeminiTestServlet()), "/api/gemini/test");
        context.addServlet(new ServletHolder(new com.recipe.servlet.FeedbackServlet()), "/api/feedback");

        // Create ResourceHandler for static files
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase("src/main/webapp");

        // Combine handlers
        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);
        handlers.addHandler(context);

        server.setHandler(handlers);

        server.start();
        System.out.println("Server started at http://localhost:8080");
        System.out.println("📁 Static files: src/main/webapp");
        System.out.println("🔗 API endpoints: /api/*");
        System.out.println("💬 Feedback API: /api/feedback");
        server.join();
    }
}