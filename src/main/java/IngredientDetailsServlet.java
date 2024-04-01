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

@WebServlet("/findByIngredients")
public class IngredientDetailsServlet extends HttpServlet {

    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "ServiceLogs"; // Use the name of your collection
    private static ServiceLogger logger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = "success";
        long startTime = System.currentTimeMillis(); // Capture start time
        String ingredients = request.getParameter("ingredients");
        if (ingredients == null || ingredients.trim().isEmpty()) {
            status = "error: Search term is required";
            logger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/findByIngredients", "searchTerm=none", System.currentTimeMillis() - startTime, status);
            sendErrorResponse(response, "Ingredients parameter is required.");
            return;
        }

        String[] ingredientList = ingredients.split(",");
        StringBuilder sBuilder = new StringBuilder();
        for(String ing: ingredientList){
            sBuilder.append(ing.trim()).append("|");
        }
        sBuilder.deleteCharAt(sBuilder.length() - 1);
        JSONArray allDrinks = new JSONArray();

        for (String ingredient : ingredientList) {
            ingredient = ingredient.trim();
            if (!ingredient.isEmpty()) {
                String encodedIngredient = URLEncoder.encode(ingredient, "UTF-8");
                String apiURL = "https://www.thecocktaildb.com/api/json/v1/1/filter.php?i=" + encodedIngredient;
                URL url = new URL(apiURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
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
                    sendErrorResponse(response, "Failed to fetch data for ingredient: " + ingredient);
                    return;
                }
                conn.disconnect();
            }
        }

        JSONObject result = new JSONObject();
        result.put("drinks", allDrinks);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(result.toString());
        logger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/findByIngredients", "searchTerm=" + sBuilder.toString(), System.currentTimeMillis() - startTime, status);
        out.flush();
    }

    // Inside IngredientSearchService servlet

    // private JSONObject getDrinkDetails(String idDrink) throws IOException {
    //     String apiURL = "https://www.thecocktaildb.com/api/json/v1/1/lookup.php?i=" + idDrink;
    //     URL url = new URL(apiURL);
    //     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    //     conn.setRequestMethod("GET");
    //     conn.connect();

    //     InputStream in = conn.getInputStream();
    //     Scanner scanner = new Scanner(in);
    //     scanner.useDelimiter("\\A");
    //     String responseData = scanner.hasNext() ? scanner.next() : "";
    //     scanner.close();
    //     in.close();

    //     JSONObject drinkDetails = new JSONObject(responseData).getJSONArray("drinks").getJSONObject(0);
    //     JSONObject detailedDrink = new JSONObject();

    //     detailedDrink.put("idDrink", drinkDetails.getString("idDrink"));
    //     detailedDrink.put("strDrink", drinkDetails.getString("strDrink"));
    //     detailedDrink.put("strInstructions", drinkDetails.getString("strInstructions"));
    //     JSONArray ingredients = new JSONArray();

    //     for (int i = 1; i <= 15; i++) {
    //         String ingredient = drinkDetails.optString("strIngredient" + i);
    //         if (ingredient != null && !ingredient.isEmpty()) {
    //             JSONObject ingDetail = new JSONObject();
    //             ingDetail.put("strIngredient" + i, ingredient);
    //             ingDetail.put("strMeasure" + i, drinkDetails.optString("strMeasure" + i, ""));
    //             ingredients.put(ingDetail);
    //         }
    //     }

    //     detailedDrink.put("ingredients", ingredients);
    //     conn.disconnect();
    //     return detailedDrink;
    // }

    // private JSONArray formatDrinksResponse(String rawData) throws IOException {
    //     JSONArray formattedDrinks = new JSONArray();
    //     JSONObject rawResponse = new JSONObject(rawData);
    //     JSONArray rawDrinks = rawResponse.optJSONArray("drinks");

    //     if (rawDrinks != null) {
    //         for (int i = 0; i < rawDrinks.length(); i++) {
    //             JSONObject rawDrink = rawDrinks.getJSONObject(i);
    //             String idDrink = rawDrink.getString("idDrink");
    //             JSONObject drinkDetails = getDrinkDetails(idDrink);
    //             formattedDrinks.put(drinkDetails);
    //         }
    //     }
    //     return formattedDrinks;
    // }

    private JSONArray formatDrinksResponse(String rawData) {
        JSONArray formattedDrinks = new JSONArray();
        JSONObject rawResponse = new JSONObject(rawData);
        JSONArray rawDrinks = rawResponse.optJSONArray("drinks");
    
        if (rawDrinks != null) {
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
    

    // private JSONArray formatDrinksResponse(String rawData) {
    // JSONArray formattedDrinks = new JSONArray();
    // JSONObject rawResponse = new JSONObject(rawData);
    // JSONArray rawDrinks = rawResponse.optJSONArray("drinks");

    // if (rawDrinks != null) {
    // for (int i = 0; i < rawDrinks.length(); i++) {
    // JSONObject rawDrink = rawDrinks.getJSONObject(i);
    // JSONObject formattedDrink = new JSONObject();

    // formattedDrink.put("idDrink", rawDrink.optString("idDrink"));
    // formattedDrink.put("strDrink", rawDrink.optString("strDrink"));
    // formattedDrink.put("strDrinkThumb", rawDrink.optString("strDrinkThumb"));
    // // Add other fields as needed...

    // formattedDrinks.put(formattedDrink);
    // }
    // }
    // return formattedDrinks;
    // }

    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"" + errorMessage + "\"}");
        out.flush();
    }
}
