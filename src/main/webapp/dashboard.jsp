<%@ page import="java.util.List" %>
<%@ page import="org.bson.Document" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Service Logs Dashboard</title>
    <style>
        table {
            width: 100%;
            border-collapse: collapse;
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
<h2>Service Logs Dashboard</h2>
<table>
    <tr>
        <th>Timestamp</th>
        <th>Device Info</th>
        <th>API Endpoint</th>
        <th>Request Params</th>
        <th>Response Time (ms)</th>
        <th>Status</th>
    </tr>
    <% 
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    List<Document> logs = (List<Document>) request.getAttribute("logs");
    for (Document log : logs) { 
        Date timestamp = log.getDate("timestamp"); // Change this line to use getDate()
        String formattedTimestamp = timestamp != null ? dateFormat.format(timestamp) : "N/A";
    %>
    <tr>
        <td><%= formattedTimestamp %></td> <!-- Updated to use formattedTimestamp -->
        <td><%= log.getString("deviceInfo") %></td>
        <td><%= log.getString("apiEndpoint") %></td>
        <td><%= log.getString("requestParams") %></td>
        <td><%= log.getLong("responseTime") %></td>
        <td><%= log.getString("status") %></td>
    </tr>
    <% } %>
</table>
</body>
</html>
