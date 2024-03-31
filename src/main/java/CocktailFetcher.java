import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class CocktailFetcher {

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.thecocktaildb.com/api/json/v1/1/search.php?s=margarita"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body)
                .thenAccept(inputStream -> {
                    try (JsonReader jsonReader = Json.createReader(inputStream)) {
                        JsonObject jsonObject = jsonReader.readObject();
                        jsonObject.getJsonArray("drinks").forEach(drink -> {
                            JsonObject drinkObject = (JsonObject) drink;
                            System.out.println("Name: " + drinkObject.getString("strDrink"));
                            System.out.println("Image URL: " + drinkObject.getString("strDrinkThumb"));
                            System.out.println("Instructions: " + drinkObject.getString("strInstructions"));
                            System.out.println("Ingredients: ");
                            for (int i = 1; i <= 15; i++) {
                                String ingredient = drinkObject.getString("strIngredient" + i, null);
                                if (ingredient != null && !ingredient.isEmpty()) {
                                    System.out.println("- " + ingredient);
                                }
                            }
                            System.out.println("---------------------------------------------");
                        });
                    }
                })
                .join();
    }
}
