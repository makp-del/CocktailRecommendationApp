import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CocktailService extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Example: Fetching data from a third-party API
        String searchTerm = request.getParameter("searchTerm");
        String thirdPartyApiUrl = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=" + searchTerm;

        URL url = new URL(thirdPartyApiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        // Check if the connection is successful
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {
            Scanner scanner = new Scanner(url.openStream());
            StringBuilder data = new StringBuilder();
            while (scanner.hasNext()) {
                data.append(scanner.nextLine());
            }
            scanner.close();

            // Set the response content type and write the response
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(data);
            out.flush();
        }
    }

    // You can add doPost, doPut, doDelete methods as needed based on your requirements.
}
