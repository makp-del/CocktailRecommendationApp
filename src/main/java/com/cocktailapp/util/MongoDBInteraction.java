package com.cocktailapp.util;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;

import java.util.Scanner;

public class MongoDBInteraction {

    private static final Logger logger = LoggerUtil.getLogger(MongoDBInteraction.class);
    public static void main(String[] args) {
        String connectionString = "<YOUR_MONGO_CONNECTION_STRING>";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("SimpleDB");
            MongoCollection<Document> collection = database.getCollection("YourCollectionName");

            try (// Prompt the user for a string
            Scanner scanner = new Scanner(System.in)) {
                logger.info("Enter a string to store in the database:");
                String userInput = scanner.nextLine();

                // Write the string to the MongoDB database
                Document doc = new Document("userInput", userInput);
                collection.insertOne(doc);
            }
            logger.info("Document inserted.");

            // Read all documents from the database
            logger.info("Reading all documents from the database:");
            for (Document document : collection.find()) {
                logger.info(document.getString("userInput"));
            }
        } catch (Exception e) {
            logger.error("An error occurred: " + e.getMessage());
        }
    }
}
