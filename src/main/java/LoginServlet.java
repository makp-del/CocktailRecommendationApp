import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Servlet implementation class LoginServlet
 * This servlet handles user login functionality.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // MongoDB connection string and database/collection names
    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "Users"; // Use the name of your collection
    private static ServiceLogger logger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);
    private MongoCollection<Document> collection;

    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        MongoClient mongoClient = MongoClients.create(MONGO_CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        this.collection = database.getCollection(COLLECTION_NAME);
    }
    
    /**
     * Handles HTTP POST requests for user login.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Basic validation
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
            return;
        }

        // Authenticate the user
        Document user = collection.find(Filters.and(Filters.eq("username", username), Filters.eq("password", password))).first();
        if (user == null) {
            // No matching user found
            Document existingUser = collection.find(Filters.eq("username", username)).first();
            if (existingUser == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("User not present");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Failure");
            }
            return;
        }

        // If user is authenticated, retrieve session token and send success response
        String sessionToken = user.getString("sessionToken");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Success; SessionToken: " + sessionToken);
    }
}
