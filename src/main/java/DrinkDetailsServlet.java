import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.bson.Document;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

@WebServlet("/getDrinkDetails")
public class DrinkDetailsServlet extends HttpServlet {

    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "ServiceLogs"; // Use the name of your collection
    private static ServiceLogger logger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis(); // Capture start time
        String status = "success";
        String idDrink = request.getParameter("idDrink");
        String sessionToken = request.getHeader("Authorization");
        if (idDrink == null || idDrink.trim().isEmpty()) {
            sendErrorResponse(response, "Drink ID parameter is required.");
            return;
        }

        JSONObject drinkDetails = getDrinkDetails(idDrink);

        if (drinkDetails != null) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            status = "error: Search term is required";
            logger.log(this.getClass().getSimpleName(), request.getHeader("User-Agent"), "/getDrinkDetails", "searchTerm=none", System.currentTimeMillis() - startTime, status);
            out.print(drinkDetails.toString());
            out.flush();
        } else {
            sendErrorResponse(response, "Drink details not found for ID: " + idDrink);
        }

        incrementUserDrinkRequestCount(sessionToken, idDrink);
    }

    private JSONObject getDrinkDetails(String idDrink) throws IOException {
        String apiURL = "https://www.thecocktaildb.com/api/json/v1/1/lookup.php?i=" + idDrink;
        URL url = new URL(apiURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            conn.disconnect();
            return null; // Handle non-200 response code appropriately
        }

        InputStream in = conn.getInputStream();
        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\A");
        String responseData = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        in.close();

        JSONObject drinkDetails = new JSONObject(responseData).getJSONArray("drinks").getJSONObject(0);
        conn.disconnect();

        // Extracting ingredients and measures
        JSONObject detailedDrink = new JSONObject();
        detailedDrink.put("idDrink", drinkDetails.getString("idDrink"));
        detailedDrink.put("strDrink", drinkDetails.getString("strDrink"));
        detailedDrink.put("strInstructions", drinkDetails.getString("strInstructions"));
        JSONObject ingredients = new JSONObject();

        for (int i = 1; i <= 15; i++) {
            String ingredient = drinkDetails.optString("strIngredient" + i);
            if (ingredient != null && !ingredient.isEmpty()) {
                String measure = drinkDetails.optString("strMeasure" + i, "N/A");
                ingredients.put(ingredient, measure);
            }
        }

        detailedDrink.put("ingredients", ingredients);
        return detailedDrink;
    }

    private void incrementUserDrinkRequestCount(String sessionToken, String idDrink) {
        // Strip "Bearer " prefix from the sessionToken to store only the token part
        String token = sessionToken.replace("Bearer ", "");
    
        // Connect to your MongoDB database
        MongoClient mongoClient = MongoClients.create(MONGO_CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        MongoCollection<Document> collection = database.getCollection("UserDrinkRequests");
        
        // Find the document for this token and drink ID
        Document userDrinkRequest = collection.find(Filters.and(Filters.eq("userID", token), Filters.eq("idDrink", idDrink))).first();
        if (userDrinkRequest != null) {
            // If it exists, increment the count
            int count = userDrinkRequest.getInteger("count");
            collection.updateOne(Filters.eq("_id", userDrinkRequest.get("_id")), new Document("$set", new Document("count", count + 1)));
        } else {
            // Otherwise, create a new document with a count of 1
            Document newUserDrinkRequest = new Document("userID", token)
                    .append("idDrink", idDrink)
                    .append("count", 1);
            collection.insertOne(newUserDrinkRequest);
        }
    } 

    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"" + errorMessage + "\"}");
        out.flush();
    }
}
