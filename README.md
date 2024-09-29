# CocktailApp

CocktailApp is a Java-based web application that allows users to search for cocktails by name or ingredients, retrieve detailed information about specific drinks, view logs and user data, and manage user authentication. The application also includes a “Drink of the Day” feature and keeps track of favorite drinks for each user.

## Features

### 1. **User Authentication**
- **LoginServlet.java**: Handles user login, authenticating credentials using MongoDB.
- **SignUpServlet.java**: Handles new user registrations, storing user credentials securely.
- **Session Management**: Manages user sessions using tokens for secure requests. Authorization headers are checked for valid tokens.

### 2. **Cocktail Search**
- **CocktailDetailsServlet.java**: Allows users to search for cocktails by name. It fetches data from a third-party API and displays the search results in a simplified JSON format.
- **IngredientDetailsServlet.java**: Allows users to search for cocktails based on ingredients. Users can input a list of comma-separated ingredients, and the application will return cocktails containing those ingredients.

### 3. **Drink of the Day**
- **GetDrinkOfTheDayServlet.java**: Aggregates user interaction data from MongoDB and identifies the most requested drink of the day. This drink is displayed to all users as the "Drink of the Day."

### 4. **Favorite Drinks**
- **GetMyFavouriteDrinkServlet.java**: Retrieves the top 5 favorite drinks of the currently authenticated user. This servlet uses MongoDB to track how many times a user has requested a specific drink and ranks them accordingly.

### 5. **Drink Details**
- **DrinkDetailsServlet.java**: Retrieves detailed information about a specific drink based on the drink’s ID. The details include ingredients, instructions, and other related data fetched from a third-party API.

### 6. **Dashboard and Logs**
- **DashboardServlet.java**: Displays a dashboard for admins showing user activity and service logs. It fetches the logs and user information from MongoDB, allowing admins to track user interactions and application performance.
- **Service Logs**: All service interactions are logged to MongoDB using the **ServiceLogger.java** class. Each request, including the time taken and status, is stored for audit and performance monitoring purposes.

### 7. **MongoDB Interaction**
- **MongoDBInteraction.java**: This utility class manages interactions with the MongoDB database, handling CRUD operations for user data, logs, and drink requests.

## Technology Stack

- **Java**: Core backend logic with servlets.
- **MongoDB**: Used to store user data, service logs, and track user interactions.
- **JSP**: Provides a dashboard interface for admins.
- **HTML/CSS**: A simple user interface for the public-facing pages.
- **Third-Party API**: Integrates with [TheCocktailDB](https://www.thecocktaildb.com/) API to fetch cocktail-related data.
- **SLF4J/Logback**: Used for logging application activity and interactions with MongoDB.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   ├── com/
│   │   │   └── cocktailapp/
│   │   │       ├── servlet/
│   │   │       │   ├── CocktailDetailsServlet.java
│   │   │       │   ├── DashboardServlet.java
│   │   │       │   ├── DrinkDetailsServlet.java
│   │   │       │   ├── GetDrinkOfTheDayServlet.java
│   │   │       │   ├── GetMyFavouriteDrinkServlet.java
│   │   │       │   ├── IngredientDetailsServlet.java
│   │   │       │   ├── LoginServlet.java
│   │   │       │   ├── SignUpServlet.java
│   │   │       ├── util/
│   │   │       │   ├── MongoDBInteraction.java
│   │   │       │   ├── ServiceLogger.java
│   ├── resources/
│   │   └── dashboard.jsp
│   └── webapp/
│       ├── WEB-INF/
│       └── index.html
│── README.md
│── pom.xml
│── Dockerfile
│── .gitignore
│── LICENSE
```

## Endpoints

### 1. `/searchCocktail`
- **Description**: Search for cocktails by name.
- **Method**: `GET`
- **Parameters**:
    - `searchTerm`: The name of the cocktail.
- **Response**: Returns a JSON array with matching cocktails.

### 2. `/findByIngredients`
- **Description**: Search for cocktails by ingredients.
- **Method**: `GET`
- **Parameters**:
    - `ingredients`: Comma-separated list of ingredients.
- **Response**: Returns a JSON array of cocktails containing the specified ingredients.

### 3. `/getDrinkDetails`
- **Description**: Retrieve detailed information about a specific cocktail.
- **Method**: `GET`
- **Parameters**:
    - `idDrink`: The ID of the cocktail.
- **Response**: Detailed JSON object with the cocktail’s ingredients, instructions, and other details.

### 4. `/getDrinkOfTheDay`
- **Description**: Fetches the most requested drink of the day based on user interactions.
- **Method**: `GET`
- **Response**: A JSON object representing the "Drink of the Day."

### 5. `/getMyFavouriteDrink`
- **Description**: Retrieves the top 5 favorite drinks of the currently authenticated user.
- **Method**: `GET`
- **Response**: A JSON array containing the user's favorite drinks.

### 6. `/dashboard`
- **Description**: Displays service logs and user activity for admin use.
- **Method**: `GET`
- **Response**: Renders a JSP page displaying the logs and user data.

### 7. `/login`
- **Description**: Authenticates user credentials for login.
- **Method**: `POST`
- **Parameters**:
    - `username`: The user’s username.
    - `password`: The user’s password.
- **Response**: A session token is returned on successful login.

### 8. `/signup`
- **Description**: Handles new user registration.
- **Method**: `POST`
- **Parameters**:
    - `username`: The user’s username.
    - `password`: The user’s password.
- **Response**: A confirmation of successful registration.

## How to Run the Project

1. **Clone the repository**:
   ```bash
   git clone <repository_url>
   cd CocktailApp
   ```

2. **Set up MongoDB**:
    - Ensure MongoDB is installed and running.
    - Update the connection strings in the servlets with your MongoDB details.

3. **Build and run the application**:
    - Build the project using Maven:
      ```bash
      mvn clean install
      ```
    - Deploy the project to a servlet container like Tomcat.

4. **Access the application**:
    - Navigate to `http://localhost:8080` to start using CocktailApp.

## License

This project is licensed under the MIT License.

---
