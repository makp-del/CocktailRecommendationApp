package com.cocktailapp.util;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Scanner;

public class MongoDBInteraction {
    public static void main(String[] args) {
        String connectionString = "<YOUR_MONGO_CONNECTION_STRING>";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("SimpleDB");
            MongoCollection<Document> collection = database.getCollection("YourCollectionName");

            try (// Prompt the user for a string
            Scanner scanner = new Scanner(System.in)) {
                System.out.println("Enter a string to store in the database:");
                String userInput = scanner.nextLine();

                // Write the string to the MongoDB database
                Document doc = new Document("userInput", userInput);
                collection.insertOne(doc);
            }
            System.out.println("Document inserted.");

            // Read all documents from the database
            System.out.println("Reading all documents from the database:");
            for (Document document : collection.find()) {
                System.out.println(document.getString("userInput"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}