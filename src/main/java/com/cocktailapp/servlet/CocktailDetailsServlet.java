package com.cocktailapp.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import com.cocktailapp.util.LoggerUtil;
import com.cocktailapp.util.ServiceLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Servlet implementation class com.cocktailapp.servlet.CocktailDetailsServlet
 * This servlet is responsible for handling requests related to cocktail search.
 */
@WebServlet("/searchCocktail")
public class CocktailDetailsServlet extends HttpServlet {

    private static final Logger logger = LoggerUtil.getLogger(CocktailDetailsServlet.class);
    
    // MongoDB connection string and database/collection names
    private static final String MONGO_CONNECTION_STRING = "<YOUR_MONGO_CONNECTION_STRING>";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "ServiceLogs"; // Use the name of your collection
    
    // Initialize com.cocktailapp.util.ServiceLogger with MongoDB connection details
    private static final ServiceLogger serviceLogger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis(); // Capture start time
        
        // Retrieve search term from request parameter
        String searchTerm = request.getParameter("searchTerm");
        String status = "success"; // Default status
        
        // Check if search term is empty or null
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            status = "error: Search term is required";
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, status);
            // Log error status and request details
            logger.error("Search term is required");
            serviceLogger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/searchCocktail", "searchTerm=none", System.currentTimeMillis() - startTime, status);
            return;
        }
        
        // Construct URL for third-party API with search term
        String thirdPartyApiUrl = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=" + searchTerm;
        URL url = new URL(thirdPartyApiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        
        // Get response code from the third-party API
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            // If response code indicates failure, set status and send error response
            status = "error: Failed to fetch data from the third-party API";
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, status);
        } else {
            // If response code indicates success, parse JSON response from API
            JSONObject result = getJsonObject(url);

            // Set response content type to JSON
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(result.toString());
            
            // Log request details along with status and response time
            logger.info("Search term: " + searchTerm);
            serviceLogger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/findByIngredients", "searchTerm=" + searchTerm, System.currentTimeMillis() - startTime, status);
            out.flush();
        }
        
        long endTime = System.currentTimeMillis(); // Capture end time
        // Log the request details
        logger.info("Search term: " + searchTerm);
        serviceLogger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/searchCocktail", "searchTerm=" + searchTerm, endTime - startTime, status);
    }

    private static JSONObject getJsonObject(URL url) throws IOException {
        Scanner scanner = new Scanner(url.openStream());
        String data = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        scanner.close();

        // Convert JSON response to JSONObject
        JSONObject jsonResponse = new JSONObject(data);
        JSONArray drinksArray = jsonResponse.optJSONArray("drinks");
        JSONArray simplifiedDrinksArray = new JSONArray();

        // Process each drink object and construct simplified drink objects
        if (drinksArray != null) {
            for (int i = 0; i < drinksArray.length(); i++) {
                JSONObject drink = drinksArray.getJSONObject(i);
                JSONObject simplifiedDrink = new JSONObject();
                simplifiedDrink.put("idDrink", drink.getString("idDrink"));
                simplifiedDrink.put("strDrink", drink.getString("strDrink"));
                simplifiedDrink.put("strDrinkThumb", drink.getString("strDrinkThumb"));
                simplifiedDrinksArray.put(simplifiedDrink);
            }
        }

        // Construct JSON response containing simplified drink details
        JSONObject result = new JSONObject();
        result.put("drinks", simplifiedDrinksArray);
        return result;
    }
}