import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class DashboardServlet
 * This servlet is responsible for retrieving and displaying dashboard data.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    
    // Logger for logging dashboard related information
    private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);
    
    // MongoDB connection string and database/collection names
    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "ServiceLogs"; // Use the name of your collection
    private static final String USERS_COLLECTION_NAME = "Users";
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Create MongoDB client and connect to the database
        var mongoClient = MongoClients.create(MONGO_CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        
        // Get collections from the database
        MongoCollection<Document> logsCollection = database.getCollection(COLLECTION_NAME);
        MongoCollection<Document> usersCollection = database.getCollection(USERS_COLLECTION_NAME);
        
        // Retrieve logs from logs collection
        FindIterable<Document> logsIterable = logsCollection.find();
        List<Document> logs = new ArrayList<>();
        logsIterable.forEach(logs::add);

        // Retrieve users from users collection
        FindIterable<Document> usersIterable = usersCollection.find();
        List<Document> users = new ArrayList<>();
        usersIterable.forEach(users::add);

        // Log the list of users
        logger.info("User list: {}", users);
        
        // Set attributes for request dispatch
        request.setAttribute("users", users);
        request.setAttribute("logs", logs);
        
        // Forward request to dashboard.jsp for rendering
        RequestDispatcher dispatcher = request.getRequestDispatcher("/dashboard.jsp");
        dispatcher.forward(request, response);
    }
}