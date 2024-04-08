import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.Date;

/**
 * A class to log service information to MongoDB.
 */
public class ServiceLogger {
    
    // MongoDB collection to store service logs
    private MongoCollection<Document> collection;

    /**
     * Constructor to initialize the ServiceLogger.
     * @param connectionString MongoDB connection string
     * @param dbName           Name of the database
     * @param collectionName   Name of the collection
     */
    public ServiceLogger(String connectionString, String dbName, String collectionName) {
        var mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);
    }

    /**
     * Method to log service information to MongoDB.
     * @param servletName   Name of the servlet
     * @param deviceInfo    Information about the device
     * @param apiEndpoint   Endpoint of the API
     * @param requestParams Parameters of the request
     * @param responseTime  Time taken for the response
     * @param status        Status of the request
     */
    public void log(String servletName, String deviceInfo, String apiEndpoint, String requestParams, long responseTime, String status) {
        Document logEntry = new Document("timestamp", new Date())
                .append("servletName", servletName)
                .append("deviceInfo", deviceInfo)
                .append("apiEndpoint", apiEndpoint)
                .append("requestParams", requestParams)
                .append("responseTime", responseTime)
                .append("status", status);

        collection.insertOne(logEntry);
    }
}
