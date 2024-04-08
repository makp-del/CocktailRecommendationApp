import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Date; // Import Date for timestamp

/**
 * Servlet implementation class CocktailDetailsServlet
 * This servlet is responsible for handling requests related to cocktail search.
 */
@WebServlet("/searchCocktail")
public class CocktailDetailsServlet extends HttpServlet {
    
    // MongoDB connection string and database/collection names
    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "ServiceLogs"; // Use the name of your collection
    
    // Initialize ServiceLogger with MongoDB connection details
    private static ServiceLogger logger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);
    
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
            logger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/searchCocktail", "searchTerm=none", System.currentTimeMillis() - startTime, status);
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
            
            // Set response content type to JSON
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(result.toString());
            
            // Log request details along with status and response time
            logger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/findByIngredients", "searchTerm=" + searchTerm, System.currentTimeMillis() - startTime, status);
            out.flush();
        }
        
        long endTime = System.currentTimeMillis(); // Capture end time
        // Log the request details
        logger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/searchCocktail", "searchTerm=" + searchTerm, endTime - startTime, status);
    }
}