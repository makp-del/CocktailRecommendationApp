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
import java.util.UUID;


/**
 * Servlet to handle user sign up.
 */
public class SignUpServlet extends HttpServlet {

    // MongoDB collection to store user data
    private MongoCollection<Document> collection;
    
    // MongoDB connection string
    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    
    // Database name
    private static final String DB_NAME = "CocktailDB";
    
    // Collection name to store user data
    private static final String COLLECTION_NAME = "Users";
    
    // Service logger instance
    private static ServiceLogger logger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);

    /**
     * Initialize the servlet.
     */
    public void init() throws ServletException {
        super.init();
        // Create MongoDB client and get the collection
        MongoClient mongoClient = MongoClients.create(MONGO_CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        this.collection = database.getCollection(COLLECTION_NAME);
    }

    /**
     * Handle POST request for user sign up.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // Basic validation
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
            return;
        }

        // Check if user already exists
        Document existingUser = collection.find(new Document("username", username)).first();
        if (existingUser != null) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("User already exists");
            return;
        }

        // Generate session token
        String sessionToken = UUID.randomUUID().toString();
        
        // Store the new user
        Document newUser = new Document("username", username)
                .append("password", password) // In a real-world application, password should be hashed
                .append("sessionToken", sessionToken);
        collection.insertOne(newUser);

        // Send success response with session token
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Success; SessionToken: " + sessionToken);
    }
}
