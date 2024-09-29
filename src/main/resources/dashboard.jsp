<%@ page import="java.util.List" %>
<%@ page import="org.bson.Document" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Dashboard</title>
    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.1/css/all.min.css">
    <style>
        body {
            padding: 20px;
            background-color: #f7f7f7;
        }
        .dashboard-table-container {
            overflow-y: auto;
            height: 400px; /* Set a fixed height for the container */
            margin-bottom: 40px;
        }
        .dashboard-table {
            width: 100%;
            background-color: #ffffff;
            border-collapse: collapse;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        .dashboard-title {
            text-align: center;
            margin-bottom: 30px;
            color: #333;
            font-size: 24px;
            font-weight: bold;
        }
        .status-success {
            color: #28a745;
        }
        .status-error {
            color: #dc3545;
        }
        .fas {
            margin-right: 5px;
        }
        table, th, td {
            border: 1px solid black;
        }
        th, td {
            padding: 8px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>

<div class="dashboard-title">
    <i class="fas fa-network-wired"></i>Service Logs Dashboard
</div>
<div class="dashboard-table-container">
    <table class="table dashboard-table">
        <thead class="thead-dark">
            <tr>
                <th>Timestamp</th>
                <th>Device Info</th>
                <th>API Endpoint</th>
                <th>Request Params</th>
                <th>Response Time (ms)</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody>
            <% 
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<Document> logs = (List<Document>) request.getAttribute("logs");
            for (Document log : logs) { 
                Date timestamp = log.getDate("timestamp"); 
                String formattedTimestamp = timestamp != null ? dateFormat.format(timestamp) : "N/A";
            %>
            <tr>
                <td><%= formattedTimestamp %></td>
                <td><%= log.getString("deviceInfo") %></td>
                <td><%= log.getString("apiEndpoint") %></td>
                <td><%= log.getString("requestParams") %></td>
                <td><%= log.get("responseTime", Long.class) %></td>
                <td class="<%= "success".equals(log.getString("status")) ? "status-success" : "status-error" %>">
                    <%= log.getString("status") %>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>

<div class="dashboard-title">
    <i class="fas fa-users"></i>User Details Dashboard
</div>
<div class="dashboard-table-container">
    <table class="table dashboard-table">
        <thead class="thead-dark">
            <tr>
                <th>Username</th>
                <th>Password</th> <!-- Reminder: Displaying passwords is a security risk! -->
                <th>Session Token</th>
            </tr>
        </thead>
        <tbody>
            <% 
            List<Document> users = (List<Document>) request.getAttribute("users");
            if(users != null) {
                for (Document user : users) { 
            %>
            <tr>
                <td><%= user.getString("username") %></td>
                <td>********</td> <!-- Masked password -->
                <td><%= user.getString("sessionToken") %></td>
            </tr>
            <% 
                } 
            } else { 
            %>
            <tr>
                <td colspan="3" class="text-center">No user data available.</td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>

<!-- Bootstrap JS and dependencies -->
<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.1.6/dist/umd/popper.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>

</body>
</html>
