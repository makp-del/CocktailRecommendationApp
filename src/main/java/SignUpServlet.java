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


@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {

    private MongoCollection<Document> collection;
    private static final String MONGO_CONNECTION_STRING = "mongodb+srv://manjunathkp1298:2Xg3NY1C5rBlnbHa@dismprojectcluster.6ct1xxu.mongodb.net/?retryWrites=true&w=majority&appName=DISMProjectCluster";
    private static final String DB_NAME = "CocktailDB"; // Use the name of your database
    private static final String COLLECTION_NAME = "Users"; // Use the name of your collection
    private static ServiceLogger logger = new ServiceLogger(MONGO_CONNECTION_STRING, DB_NAME, COLLECTION_NAME);

    public void init() throws ServletException {
        super.init();
        MongoClient mongoClient = MongoClients.create(MONGO_CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        this.collection = database.getCollection(COLLECTION_NAME);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // Basic validation
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
            return;
        }

        // Check if user already exists
        Document existingUser = collection.find(Filters.eq("username", username)).first();
        if (existingUser != null) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("User already present");
            return;
        }

        String sessionToken = UUID.randomUUID().toString();
        // Store the new user
        Document newUser = new Document("username", username)
                                .append("password", password)
                                .append("sessionToken", sessionToken); // In a real-world application, password should be hashed
        collection.insertOne(newUser);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Success; SessionToken: " + sessionToken);
    }
}
