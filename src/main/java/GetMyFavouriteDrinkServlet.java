import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Servlet implementation class GetMyFavouriteDrinkServlet
 * This servlet is responsible for retrieving the favorite drinks of a user.
 */
@WebServlet("/getMyFavouriteDrink")
public class GetMyFavouriteDrinkServlet extends HttpServlet {

    private MongoCollection<Document> collection;
    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "UserDrinkRequests"; // Use the name of your collection

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize MongoDB collection
        String mongoConnectionString = MONGO_CONNECTION_STRING;
        MongoClient mongoClient = MongoClients.create(mongoConnectionString);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        this.collection = database.getCollection(COLLECTION_NAME);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check authorization header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is missing or invalid.");
            return;
        }

        // Extract session token from authorization header
        String sessionToken = authorizationHeader.substring(7);

        // Prepare aggregation pipeline
        List<Document> pipeline = Arrays.asList(
            new Document("$match", new Document("userID", sessionToken)),
            new Document("$sort", new Document("count", -1)),
            new Document("$limit", 5)
        );

        // Execute aggregation
        List<Document> topDrinks = collection.aggregate(pipeline).into(new ArrayList<>());

        // Prepare response JSON
        JSONArray drinksArray = new JSONArray();
        for (Document drink : topDrinks) {
            String idDrink = drink.getString("idDrink");
            JSONObject drinkDetails = getDrinkDetails(idDrink);
            drinksArray.put(drinkDetails);
        }

        JSONObject responseObject = new JSONObject();
        responseObject.put("drinks", drinksArray);

        // Send response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(responseObject.toString());
        out.flush();
    }

    /**
     * Retrieves drink details from a third-party API based on drink ID.
     *
     * @param idDrink The ID of the drink to retrieve details for.
     * @return JSONObject containing drink details.
     * @throws IOException
     */
    private JSONObject getDrinkDetails(String idDrink) throws IOException {
        // Construct URL for third-party API with drink ID
        String apiURL = "https://www.thecocktaildb.com/api/json/v1/1/lookup.php?i=" + idDrink;
        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // If response code indicates success, parse JSON response from API
            Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
            String responseData = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            // Parse JSON response to JSONObject
            JSONObject response = new JSONObject(responseData);
            if (response.getJSONArray("drinks").length() > 0) {
                // Extract and return only the first drink object from the drinks array
                return response.getJSONArray("drinks").getJSONObject(0);
            } else {
                return new JSONObject().put("error", "No drinks found.");
            }
        } else {
            // If response code indicates failure, handle appropriately
            return new JSONObject().put("error", "Failed to fetch drink details from external API.");
        }
    }
}