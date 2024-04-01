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

@WebServlet("/getDrinkOfTheDay")
public class GetDrinkOfTheDayServlet extends HttpServlet {

    private MongoCollection<Document> collection;
    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "UserDrinkRequests"; // Use the name of your collection
    private static ServiceLogger logger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MongoClient mongoClient = MongoClients.create(MONGO_CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        // Aggregate to find the most requested drink
        Bson aggregation = Aggregates.sortByCount("$idDrink");
        Document mostRequestedDrink = collection.aggregate(Arrays.asList(aggregation, Aggregates.sort(Sorts.descending("count")), Aggregates.limit(1)))
                .first();

        if (mostRequestedDrink != null) {
            String idDrink = mostRequestedDrink.getString("_id");
            JSONObject drinkDetails = getDrinkDetails(idDrink);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(drinkDetails.toString());
            out.flush();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No drink of the day found.");
        }
    }

    private JSONObject getDrinkDetails(String idDrink) throws IOException {
        // Assuming you fetch drink details from an external API
        String apiURL = "https://www.thecocktaildb.com/api/json/v1/1/lookup.php?i=" + idDrink;
        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
            String responseData = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            // Assume the API returns the drink details directly
            return new JSONObject(responseData);
        } else {
            // Handle non-200 response
            return new JSONObject().put("error", "Failed to fetch drink details from external API.");
        }
    }
}
