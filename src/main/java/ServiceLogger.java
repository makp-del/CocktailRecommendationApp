import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.Date;

public class ServiceLogger {
    private MongoCollection<Document> collection;

    public ServiceLogger(String connectionString, String dbName, String collectionName) {
        var mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);
    }

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
