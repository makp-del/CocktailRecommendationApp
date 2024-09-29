package com.cocktailapp.servlet;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;

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
 * Servlet implementation class com.cocktailapp.servlet.GetMyFavouriteDrinkServlet
 * This servlet is responsible for retrieving the favorite drinks of a user.
 */
@WebServlet("/getMyFavouriteDrink")
public class GetMyFavouriteDrinkServlet extends HttpServlet {

    private MongoCollection<Document> collection;
    private static final String MONGO_CONNECTION_STRING = "<YOUR_MONGO_CONNECTION_STRING>";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "UserDrinkRequests"; // Use the name of your collection

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize MongoDB collection
        MongoClient mongoClient = MongoClients.create(MONGO_CONNECTION_STRING);
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
     */
    private JSONObject getDrinkDetails(String idDrink) throws IOException {
        // Construct URL for third-party API with drink ID
        String apiURL = "https://www.thecocktaildb.com/api/json/v1/1/lookup.php?i=" + idDrink;
        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (// If response code indicates success, parse JSON response from API
            Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A")) {
                String responseData = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                // Parse JSON response to JSONObject
                JSONObject response = new JSONObject(responseData);
                if (!response.getJSONArray("drinks").isEmpty()) {
                    // Extract and return only the first drink object from the drinks array
                    scanner.close();
                    return response.getJSONArray("drinks").getJSONObject(0);
                } else {
                    // If no drinks found, return an error message
                    scanner.close();
                    return new JSONObject().put("error", "No drinks found.");
                }
            } catch (JSONException e) {
                // If an exception occurs during JSON parsing, return an error message
                return new JSONObject().put("error", "Failed to parse drink details.");
            }
        } else {
            // If response code indicates failure, handle appropriately
            return new JSONObject().put("error", "Failed to fetch drink details from external API.");
        }
    }
}