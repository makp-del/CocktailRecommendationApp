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
import java.net.URLEncoder;
import java.net.URL;
import java.util.Scanner;

/**
 * Servlet implementation class IngredientDetailsServlet
 * This servlet is responsible for retrieving drinks based on ingredients.
 */
@WebServlet("/findByIngredients")
public class IngredientDetailsServlet extends HttpServlet {

    // MongoDB connection string and database/collection names
    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "ServiceLogs"; // Use the name of your collection
    private static ServiceLogger logger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String status = "success";
        long startTime = System.currentTimeMillis(); // Capture start time
        String ingredients = request.getParameter("ingredients");
        if (ingredients == null || ingredients.trim().isEmpty()) {
            status = "error: Search term is required";
            logger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/findByIngredients", "searchTerm=none", System.currentTimeMillis() - startTime, status);
            sendErrorResponse(response, "Ingredients parameter is required.");
            return;
        }

        // Split ingredients string and build search query
        String[] ingredientList = ingredients.split(",");
        StringBuilder sBuilder = new StringBuilder();
        for(String ing: ingredientList){
            sBuilder.append(ing.trim()).append("|");
        }
        sBuilder.deleteCharAt(sBuilder.length() - 1);
        JSONArray allDrinks = new JSONArray();

        // Iterate through each ingredient
        for (String ingredient : ingredientList) {
            ingredient = ingredient.trim();
            if (!ingredient.isEmpty()) {
                // Encode ingredient for URL
                String encodedIngredient = URLEncoder.encode(ingredient, "UTF-8");
                // Construct API URL with encoded ingredient
                String apiURL = "https://www.thecocktaildb.com/api/json/v1/1/filter.php?i=" + encodedIngredient;
                // Open connection to API URL
                URL url = new URL(apiURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // If response is successful, read data and format drinks
                    InputStream in = conn.getInputStream();
                    Scanner scanner = new Scanner(in);
                    scanner.useDelimiter("\\A");
                    if (scanner.hasNext()) {
                        String responseData = scanner.next();
                        JSONArray drinks = formatDrinksResponse(responseData);
                        for (int i = 0; i < drinks.length(); i++) {
                            allDrinks.put(drinks.getJSONObject(i));
                        }
                    }
                    scanner.close();
                    in.close();
                } else {
                    // If response code is not successful, send error response
                    sendErrorResponse(response, "Failed to fetch data for ingredient: " + ingredient);
                    return;
                }
                conn.disconnect();
            }
        }

        // Prepare response JSON object
        JSONObject result = new JSONObject();
        result.put("drinks", allDrinks);

        // Send response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(result.toString());
        logger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/findByIngredients", "searchTerm=" + sBuilder.toString(), System.currentTimeMillis() - startTime, status);
        out.flush();
    }

    /**
     * Formats drinks response from raw JSON data.
     *
     * @param rawData Raw JSON data received from API.
     * @return Formatted JSONArray containing drink details.
     */
    private JSONArray formatDrinksResponse(String rawData) {
        JSONArray formattedDrinks = new JSONArray();
        JSONObject rawResponse = new JSONObject(rawData);
        JSONArray rawDrinks = rawResponse.optJSONArray("drinks");

        if (rawDrinks != null) {
            // If raw drinks array is not null, iterate through each drink and format it
            for (int i = 0; i < rawDrinks.length(); i++) {
                JSONObject rawDrink = rawDrinks.getJSONObject(i);
                JSONObject formattedDrink = new JSONObject();
                formattedDrink.put("idDrink", rawDrink.optString("idDrink"));
                formattedDrink.put("strDrink", rawDrink.optString("strDrink"));
                formattedDrink.put("strDrinkThumb", rawDrink.optString("strDrinkThumb"));
                formattedDrinks.put(formattedDrink);
            }
        }
        return formattedDrinks;
    }

    /**
     * Sends error response with specified error message.
     *
     * @param response     HttpServletResponse object for sending response.
     * @param errorMessage Error message to be sent in the response.
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"" + errorMessage + "\"}");
        out.flush();
    }
}