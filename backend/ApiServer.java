import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import database.DatabaseConnection;
import model.Expense;
import service.ExpenseService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ApiServer {

    private static final ExpenseService expenseService = new ExpenseService();

    public static void main(String[] args)
            throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080),
                0);

        // =========================
        // TEST API
        // =========================

        server.createContext(
                "/api/test",
                ApiServer::test);

        server.createContext(
                "/api/dashboard",
                ApiServer::handleDashboard);

        server.createContext(
                "/api/budget",
                ApiServer::handleBudget);

        server.createContext(
                "/api/expenses",
                ApiServer::handleExpenses);

        server.createContext(
                "/api/categories",
                ApiServer::handleCategories);

        server.createContext(
                "/api/login",
                ApiServer::handleLogin);

        server.createContext(
                "/api/register",
                ApiServer::handleRegister);

        server.setExecutor(null);

        System.out.println(
                "API Server started on http://localhost:8080");

        server.start();
    }

    private static void test(
            HttpExchange exchange)
            throws IOException {

        String response = "{\"message\":\"Java backend is working!\"}";

        sendResponse(
                exchange,
                200,
                response);
    }

    private static void handleExpenses(
            HttpExchange exchange)
            throws IOException {

        // =========================
        // CORS
        // =========================

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type");

        String method = exchange.getRequestMethod();

        // =========================
        // OPTIONS
        // =========================

        if (method.equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(
                    204,
                    -1);

            exchange.close();

            return;
        }

        // =========================
        // GET
        // =========================

        if (method.equalsIgnoreCase("GET")) {

            getExpenses(exchange);

            return;
        }

        // =========================
        // POST
        // =========================

        if (method.equalsIgnoreCase("POST")) {

            addExpense(exchange);

            return;
        }

        // =========================
        // PUT
        // =========================

        if (method.equalsIgnoreCase("PUT")) {

            updateExpense(exchange);

            return;
        }

        // =========================
        // DELETE
        // =========================

        if (method.equalsIgnoreCase("DELETE")) {

            deleteExpense(exchange);

            return;
        }

        // =========================
        // OTHER METHODS
        // =========================

        sendResponse(
                exchange,
                405,
                "{\"error\":\"Method not allowed\"}");
    }

    private static void getExpenses(
            HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}");

            return;
        }

        // Get query string

        String query = exchange.getRequestURI()
                .getQuery();

        if (query == null ||
                !query.startsWith("userId=")) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"userId is required\"}");

            return;
        }

        // Extract user ID

        String userIdString = query.substring(
                "userId=".length());

        int userId;

        try {

            userId = Integer.parseInt(
                    userIdString);

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid userId\"}");

            return;
        }

        // Get expenses from MySQL

        List<Expense> expenses = expenseService
                .getExpensesByUserId(userId);

        // Build JSON

        StringBuilder json = new StringBuilder();

        json.append("[");

        for (int i = 0; i < expenses.size(); i++) {

            Expense expense = expenses.get(i);

            json.append("{");

            json.append("\"id\":")
                    .append(expense.getId())
                    .append(",");

            json.append("\"userId\":")
                    .append(expense.getUserId())
                    .append(",");

            json.append("\"categoryId\":")
                    .append(expense.getCategoryId())
                    .append(",");

            json.append("\"category\":\"")
                    .append(
                            escapeJson(
                                    expense.getCategoryName()))
                    .append("\",");

            json.append("\"amount\":")
                    .append(expense.getAmount())
                    .append(",");

            json.append("\"description\":\"")
                    .append(
                            escapeJson(
                                    expense.getDescription()))
                    .append("\",");

            json.append("\"date\":\"")
                    .append(
                            expense.getExpenseDate())
                    .append("\"");

            json.append("}");

            if (i < expenses.size() - 1) {

                json.append(",");
            }
        }

        json.append("]");

        sendResponse(
                exchange,
                200,
                json.toString());
    }

    private static void addExpense(
            HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        String requestBody = new String(
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        System.out.println(
                "Received expense: "
                        + requestBody);

        try {

            int userId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "userId"));

            int categoryId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "categoryId"));

            double amount = Double.parseDouble(
                    getJsonValue(
                            requestBody,
                            "amount"));

            String description = getJsonValue(
                    requestBody,
                    "description");

            String date = getJsonValue(
                    requestBody,
                    "date");

            Expense expense = new Expense(
                    userId,
                    categoryId,
                    amount,
                    description,
                    java.time.LocalDate.parse(
                            date));

            boolean success = expenseService
                    .addExpense(expense);

            if (success) {

                sendResponse(
                        exchange,
                        201,
                        "{\"message\":\"Expense added successfully\"}");

            } else {

                sendResponse(
                        exchange,
                        500,
                        "{\"error\":\"Failed to add expense\"}");
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid expense data\"}");
        }
    }

    private static void updateExpense(
            HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        String requestBody = new String(
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        System.out.println(
                "Received expense update: "
                        + requestBody);

        try {

            // =========================
            // GET DATA FROM JSON
            // =========================

            int expenseId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "id"));

            int userId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "userId"));

            int categoryId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "categoryId"));

            double amount = Double.parseDouble(
                    getJsonValue(
                            requestBody,
                            "amount"));

            String description = getJsonValue(
                    requestBody,
                    "description");

            String date = getJsonValue(
                    requestBody,
                    "date");

            // =========================
            // CREATE EXPENSE OBJECT
            // =========================

            Expense expense = new Expense(
                    expenseId,
                    userId,
                    categoryId,
                    amount,
                    description,
                    java.time.LocalDate.parse(
                            date));

            // =========================
            // UPDATE DATABASE
            // =========================

            boolean success = expenseService.updateExpense(
                    expense);

            if (success) {

                sendResponse(
                        exchange,
                        200,
                        "{\"message\":\"Expense updated successfully\"}");

            } else {

                sendResponse(
                        exchange,
                        404,
                        "{\"error\":\"Expense not found or update failed\"}");
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid expense data\"}");
        }
    }

    private static void deleteExpense(
            HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        try {

            // Get query parameters
            String query = exchange.getRequestURI()
                    .getQuery();

            int expenseId = 0;
            int userId = 0;

            if (query != null) {

                String[] params = query.split("&");

                for (String param : params) {

                    String[] pair = param.split("=");

                    if (pair.length == 2) {

                        if (pair[0].equals("id")) {
                            expenseId = Integer.parseInt(pair[1]);
                        }

                        if (pair[0].equals("userId")) {
                            userId = Integer.parseInt(pair[1]);
                        }
                    }
                }
            }

            System.out.println(
                    "Delete expense: id="
                            + expenseId
                            + ", userId="
                            + userId);

            // Validate IDs

            if (expenseId <= 0 || userId <= 0) {

                sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"Invalid expense ID or user ID\"}");

                return;
            }

            // Delete expense

            boolean success = expenseService.deleteExpense(
                    expenseId,
                    userId);

            if (success) {

                sendResponse(
                        exchange,
                        200,
                        "{\"message\":\"Expense deleted successfully\"}");

            } else {

                sendResponse(
                        exchange,
                        404,
                        "{\"error\":\"Expense not found\"}");
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid request\"}");
        }
    }

    // ******************************************************//

    private static void handleCategories(
            HttpExchange exchange)
            throws IOException {

        // CORS

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type");

        String method = exchange.getRequestMethod();

        // =========================
        // OPTIONS
        // =========================

        if (method.equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(
                    204,
                    -1);

            exchange.close();

            return;
        }

        // =========================
        // GET
        // =========================

        if (method.equalsIgnoreCase("GET")) {

            getCategories(exchange);

            return;
        }

        // =========================
        // POST
        // =========================

        if (method.equalsIgnoreCase("POST")) {

            addCategory(exchange);

            return;
        }

        if (method.equalsIgnoreCase("PUT")) {

            updateCategory(exchange);

            return;
        }

        if (method.equalsIgnoreCase("DELETE")) {

            deleteCategory(exchange);

            return;
        }

        // =========================
        // OTHER METHODS
        // =========================

        sendResponse(
                exchange,
                405,
                "{\"error\":\"Method not allowed\"}");

        // =========================
        // ONLY GET
        // =========================

        if (!method.equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}");

            return;
        }

        // =========================
        // GET USER ID
        // =========================

        String query = exchange.getRequestURI()
                .getQuery();

        if (query == null ||
                !query.startsWith("userId=")) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"userId is required\"}");

            return;
        }

        String userIdString = query.substring(
                "userId=".length());

        int userId;

        try {

            userId = Integer.parseInt(
                    userIdString);

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid userId\"}");

            return;
        }

        // =========================
        // GET CATEGORIES FROM MYSQL
        // =========================

        String sql = """
                SELECT id, user_id, name
                FROM categories
                WHERE user_id = ?
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            // =========================
            // BUILD JSON
            // =========================

            StringBuilder json = new StringBuilder();

            json.append("[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {

                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"id\":")
                        .append(
                                resultSet.getInt("id"))
                        .append(",");

                json.append("\"userId\":")
                        .append(
                                resultSet.getInt("user_id"))
                        .append(",");

                json.append("\"name\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "name")))
                        .append("\"");

                json.append("}");
            }

            json.append("]");

            sendResponse(
                    exchange,
                    200,
                    json.toString());

        } catch (SQLException e) {

            System.out.println(
                    "Failed to retrieve categories!");

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Failed to retrieve categories\"}");
        }
    }

    private static String getJsonValue(
            String json,
            String key) {

        String searchKey = "\"" + key + "\":";

        int start = json.indexOf(searchKey);

        if (start == -1) {

            throw new IllegalArgumentException(
                    "Missing key: " + key);
        }

        start += searchKey.length();

        while (start < json.length()
                &&
                Character.isWhitespace(
                        json.charAt(start))) {

            start++;
        }

        if (json.charAt(start) == '"') {

            start++;

            int end = json.indexOf(
                    '"',
                    start);

            if (end == -1) {

                throw new IllegalArgumentException(
                        "Invalid JSON");
            }

            return json.substring(
                    start,
                    end);
        }

        int end = start;

        while (end < json.length()
                &&
                json.charAt(end) != ','
                &&
                json.charAt(end) != '}') {

            end++;
        }

        return json.substring(
                start,
                end).trim();
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        byte[] responseBytes = response.getBytes(
                StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {

            output.write(
                    responseBytes);
        }
    }

    private static String escapeJson(
            String value) {

        if (value == null) {

            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static void getCategories(
            HttpExchange exchange)
            throws IOException {

        String query = exchange.getRequestURI()
                .getQuery();

        if (query == null ||
                !query.startsWith("userId=")) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"userId is required\"}");

            return;
        }

        String userIdString = query.substring(
                "userId=".length());

        int userId;

        try {

            userId = Integer.parseInt(
                    userIdString);

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid userId\"}");

            return;
        }

        String sql = """
                SELECT id, user_id, name
                FROM categories
                WHERE user_id = ?
                ORDER BY id
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            StringBuilder json = new StringBuilder("[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"id\":")
                        .append(
                                resultSet.getInt("id"))
                        .append(",");

                json.append("\"userId\":")
                        .append(
                                resultSet.getInt("user_id"))
                        .append(",");

                json.append("\"name\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "name")))
                        .append("\"");

                json.append("}");
            }

            json.append("]");

            sendResponse(
                    exchange,
                    200,
                    json.toString());

        } catch (SQLException e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Failed to retrieve categories\"}");
        }
    }

    private static void addCategory(
            HttpExchange exchange)
            throws IOException {

        String requestBody = new String(
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        System.out.println(
                "Received category: "
                        + requestBody);

        try {

            int userId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "userId"));

            String name = getJsonValue(
                    requestBody,
                    "name");

            name = name.trim();

            // =========================
            // VALIDATION
            // =========================

            if (name.isEmpty()) {

                sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"Category name cannot be empty\"}");

                return;
            }

            // =========================
            // CHECK USER
            // =========================

            String userSql = """
                    SELECT id
                    FROM users
                    WHERE id = ?
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement userStatement = connection.prepareStatement(
                            userSql)) {

                userStatement.setInt(
                        1,
                        userId);

                ResultSet userResult = userStatement.executeQuery();

                if (!userResult.next()) {

                    sendResponse(
                            exchange,
                            400,
                            "{\"error\":\"User does not exist\"}");

                    return;
                }
            }

            // =========================
            // CHECK DUPLICATE
            // =========================

            String duplicateSql = """
                    SELECT id
                    FROM categories
                    WHERE user_id = ?
                    AND LOWER(name) = LOWER(?)
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement duplicateStatement = connection.prepareStatement(
                            duplicateSql)) {

                duplicateStatement.setInt(
                        1,
                        userId);

                duplicateStatement.setString(
                        2,
                        name);

                ResultSet duplicateResult = duplicateStatement.executeQuery();

                if (duplicateResult.next()) {

                    sendResponse(
                            exchange,
                            409,
                            "{\"error\":\"Category already exists\"}");

                    return;
                }
            }

            // =========================
            // INSERT CATEGORY
            // =========================

            String sql = """
                    INSERT INTO categories
                    (user_id, name)
                    VALUES (?, ?)
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement statement = connection.prepareStatement(
                            sql)) {

                statement.setInt(
                        1,
                        userId);

                statement.setString(
                        2,
                        name);

                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {

                    sendResponse(
                            exchange,
                            201,
                            "{\"message\":\"Category added successfully\"}");

                } else {

                    sendResponse(
                            exchange,
                            500,
                            "{\"error\":\"Failed to add category\"}");
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid category data\"}");
        }
    }

    private static void updateCategory(
            HttpExchange exchange)
            throws IOException {

        String requestBody = new String(
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        System.out.println(
                "Received category update: "
                        + requestBody);

        try {

            int categoryId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "id"));

            int userId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "userId"));

            String newName = getJsonValue(
                    requestBody,
                    "name");

            newName = newName.trim();

            // =========================
            // VALIDATION
            // =========================

            if (newName.isEmpty()) {

                sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"Category name cannot be empty\"}");

                return;
            }

            // =========================
            // CHECK CATEGORY BELONGS
            // TO THIS USER
            // =========================

            String checkSql = """
                    SELECT id
                    FROM categories
                    WHERE id = ?
                    AND user_id = ?
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement statement = connection.prepareStatement(
                            checkSql)) {

                statement.setInt(
                        1,
                        categoryId);

                statement.setInt(
                        2,
                        userId);

                ResultSet resultSet = statement.executeQuery();

                if (!resultSet.next()) {

                    sendResponse(
                            exchange,
                            404,
                            "{\"error\":\"Category not found\"}");

                    return;
                }
            }

            // =========================
            // CHECK DUPLICATE NAME
            // =========================

            String duplicateSql = """
                    SELECT id
                    FROM categories
                    WHERE user_id = ?
                    AND LOWER(name) = LOWER(?)
                    AND id <> ?
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement statement = connection.prepareStatement(
                            duplicateSql)) {

                statement.setInt(
                        1,
                        userId);

                statement.setString(
                        2,
                        newName);

                statement.setInt(
                        3,
                        categoryId);

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {

                    sendResponse(
                            exchange,
                            409,
                            "{\"error\":\"Category already exists\"}");

                    return;
                }
            }

            // =========================
            // UPDATE CATEGORY
            // =========================

            String updateSql = """
                    UPDATE categories
                    SET name = ?
                    WHERE id = ?
                    AND user_id = ?
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement statement = connection.prepareStatement(
                            updateSql)) {

                statement.setString(
                        1,
                        newName);

                statement.setInt(
                        2,
                        categoryId);

                statement.setInt(
                        3,
                        userId);

                int rowsUpdated = statement.executeUpdate();

                if (rowsUpdated > 0) {

                    sendResponse(
                            exchange,
                            200,
                            "{\"message\":\"Category updated successfully\"}");

                } else {

                    sendResponse(
                            exchange,
                            500,
                            "{\"error\":\"Failed to update category\"}");
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid category data\"}");
        }
    }

    private static void deleteCategory(
            HttpExchange exchange)
            throws IOException {

        String query = exchange.getRequestURI()
                .getQuery();

        if (query == null) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"id and userId are required\"}");

            return;
        }

        int categoryId = 0;
        int userId = 0;

        try {

            String[] parameters = query.split("&");

            for (String parameter : parameters) {

                String[] pair = parameter.split("=");

                if (pair.length != 2) {
                    continue;
                }

                if (pair[0].equals("id")) {

                    categoryId = Integer.parseInt(pair[1]);
                }

                if (pair[0].equals("userId")) {

                    userId = Integer.parseInt(pair[1]);
                }
            }

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid id or userId\"}");

            return;
        }

        if (categoryId <= 0 || userId <= 0) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Valid id and userId are required\"}");

            return;
        }

        String sql = """
                DELETE FROM categories
                WHERE id = ?
                AND user_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    categoryId);

            statement.setInt(
                    2,
                    userId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {

                sendResponse(
                        exchange,
                        200,
                        "{\"message\":\"Category deleted successfully\"}");

            } else {

                sendResponse(
                        exchange,
                        404,
                        "{\"error\":\"Category not found\"}");
            }

        } catch (SQLException e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Failed to delete category\"}");
        }
    }

    // **********************************************//

    private static void handleDashboard(
            HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, OPTIONS");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type");

        String method = exchange.getRequestMethod();

        if (method.equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(
                    204,
                    -1);

            exchange.close();

            return;
        }

        if (!method.equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}");

            return;
        }

        String query = exchange.getRequestURI()
                .getQuery();

        if (query == null ||
                !query.startsWith("userId=")) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"userId is required\"}");

            return;
        }

        int userId;

        try {

            userId = Integer.parseInt(
                    query.substring(
                            "userId=".length()));

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid userId\"}");

            return;
        }

        try {

            double totalSpending = getTotalSpending(userId);

            double monthlySpending = getCurrentMonthSpending(userId);

            String categoryJson = getCategorySpending(userId);

            String recentExpenseJson = getRecentExpenses(userId);

            String monthlySpendingJson = getMonthlySpending(userId);

            double budgetAmount = getCurrentBudget(userId);

            double remainingBudget = budgetAmount - monthlySpending;

            double budgetPercentage = 0;

            if (budgetAmount > 0) {

                budgetPercentage = (monthlySpending / budgetAmount) * 100;
            }

            String budgetStatus;

            if (budgetAmount <= 0) {

                budgetStatus = "NO_BUDGET";

            } else if (monthlySpending > budgetAmount) {

                budgetStatus = "EXCEEDED";

            } else if (budgetPercentage >= 80) {

                budgetStatus = "WARNING";

            } else {

                budgetStatus = "SAFE";
            }

            String json = "{"
                    + "\"totalSpending\":"
                    + totalSpending
                    + ","
                    + "\"monthlySpending\":"
                    + monthlySpending
                    + ","
                    + "\"budget\":"
                    + budgetAmount
                    + ","
                    + "\"remaining\":"
                    + remainingBudget
                    + ","
                    + "\"percentage\":"
                    + budgetPercentage
                    + ","
                    + "\"status\":\""
                    + budgetStatus
                    + "\","
                    + "\"categories\":"
                    + categoryJson
                    + ","
                    + "\"recentExpenses\":"
                    + recentExpenseJson
                    + ","
                    + "\"monthlyChart\":"
                    + monthlySpendingJson
                    + "}";

            sendResponse(
                    exchange,
                    200,
                    json);

        } catch (SQLException e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Failed to load dashboard data\"}");
        }
    }

    private static double getTotalSpending(
            int userId)
            throws SQLException {

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM expenses
                WHERE user_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                return resultSet.getDouble(1);
            }
        }

        return 0;
    }

    private static double getCurrentMonthSpending(
            int userId)
            throws SQLException {

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM expenses
                WHERE user_id = ?
                AND MONTH(expense_date) = MONTH(CURDATE())
                AND YEAR(expense_date) = YEAR(CURDATE())
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                return resultSet.getDouble(1);
            }
        }

        return 0;
    }

    private static String getCategorySpending(
            int userId)
            throws SQLException {

        String sql = """
                SELECT
                    c.name,
                    COALESCE(SUM(e.amount), 0) AS total
                FROM categories c
                LEFT JOIN expenses e
                    ON c.id = e.category_id
                    AND e.user_id = ?
                WHERE c.user_id = ?
                GROUP BY c.id, c.name
                ORDER BY total DESC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            statement.setInt(
                    2,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            StringBuilder json = new StringBuilder("[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {

                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"name\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "name")))
                        .append("\",");

                json.append("\"amount\":")
                        .append(
                                resultSet.getDouble(
                                        "total"));

                json.append("}");
            }

            json.append("]");

            return json.toString();
        }
    }

    private static String getRecentExpenses(
            int userId)
            throws SQLException {

        String sql = """
                SELECT
                    c.name AS category_name,
                    e.description,
                    e.amount,
                    e.expense_date
                FROM expenses e
                JOIN categories c
                    ON e.category_id = c.id
                WHERE e.user_id = ?
                ORDER BY e.expense_date DESC, e.id DESC
                LIMIT 5
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            StringBuilder json = new StringBuilder("[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {

                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"category\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "category_name")))
                        .append("\",");

                json.append("\"description\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "description")))
                        .append("\",");

                json.append("\"amount\":")
                        .append(
                                resultSet.getDouble(
                                        "amount"))
                        .append(",");

                json.append("\"date\":\"")
                        .append(
                                resultSet.getDate(
                                        "expense_date"))
                        .append("\"");

                json.append("}");
            }

            json.append("]");

            return json.toString();
        }
    }

    private static String getMonthlySpending(
            int userId)
            throws SQLException {

        String sql = """
                SELECT
                    MONTH(expense_date) AS month_number,
                    COALESCE(SUM(amount), 0) AS total
                FROM expenses
                WHERE user_id = ?
                GROUP BY MONTH(expense_date)
                ORDER BY month_number
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            StringBuilder json = new StringBuilder("[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"month\":")
                        .append(
                                resultSet.getInt(
                                        "month_number"));

                json.append(",");

                json.append("\"amount\":")
                        .append(
                                resultSet.getDouble(
                                        "total"));

                json.append("}");
            }

            json.append("]");

            return json.toString();
        }
    }

    private static double getCurrentBudget(
            int userId)
            throws SQLException {

        String sql = """
                SELECT COALESCE(MAX(amount), 0)
                FROM budgets
                WHERE user_id = ?
                AND month = MONTH(CURDATE())
                AND year = YEAR(CURDATE())
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                return resultSet.getDouble(1);
            }
        }

        return 0;
    }

    // ********************************************//

    private static void handleBudget(
            HttpExchange exchange)
            throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, OPTIONS");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type");

        String method = exchange.getRequestMethod();

        // =========================
        // OPTIONS
        // =========================

        if (method.equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(
                    204,
                    -1);

            exchange.close();

            return;
        }

        // =========================
        // GET
        // =========================

        if (method.equalsIgnoreCase("GET")) {

            getBudget(exchange);

            return;
        }

        // =========================
        // POST
        // =========================

        if (method.equalsIgnoreCase("POST")) {

            saveBudget(exchange);

            return;
        }

        sendResponse(
                exchange,
                405,
                "{\"error\":\"Method not allowed\"}");
    }

    private static void getBudget(
            HttpExchange exchange)
            throws IOException {

        String query = exchange.getRequestURI()
                .getQuery();

        if (query == null ||
                !query.startsWith("userId=")) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"userId is required\"}");

            return;
        }

        int userId;

        try {

            userId = Integer.parseInt(
                    query.substring(
                            "userId=".length()));

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid userId\"}");

            return;
        }

        String sql = """
                SELECT amount, month, year
                FROM budgets
                WHERE user_id = ?
                AND month = MONTH(CURDATE())
                AND year = YEAR(CURDATE())
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                double amount = resultSet.getDouble(
                        "amount");

                int month = resultSet.getInt(
                        "month");

                int year = resultSet.getInt(
                        "year");

                String json = "{"
                        + "\"amount\":"
                        + amount
                        + ","
                        + "\"month\":"
                        + month
                        + ","
                        + "\"year\":"
                        + year
                        + "}";

                sendResponse(
                        exchange,
                        200,
                        json);

            } else {

                sendResponse(
                        exchange,
                        200,
                        "{\"amount\":0,\"month\":"
                                + java.time.LocalDate.now().getMonthValue()
                                + ",\"year\":"
                                + java.time.LocalDate.now().getYear()
                                + "}");
            }

        } catch (SQLException e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Failed to retrieve budget\"}");
        }
    }

    private static void saveBudget(
            HttpExchange exchange)
            throws IOException {

        String requestBody = new String(
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        System.out.println(
                "Received budget: "
                        + requestBody);

        try {

            int userId = Integer.parseInt(
                    getJsonValue(
                            requestBody,
                            "userId"));

            double amount = Double.parseDouble(
                    getJsonValue(
                            requestBody,
                            "amount"));

            if (amount <= 0) {

                sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"Budget must be greater than 0\"}");

                return;
            }

            String sql = """
                    INSERT INTO budgets
                    (user_id, amount, month, year)
                    VALUES (?, ?, MONTH(CURDATE()), YEAR(CURDATE()))
                    ON DUPLICATE KEY UPDATE
                    amount = VALUES(amount)
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(
                        1,
                        userId);

                statement.setDouble(
                        2,
                        amount);

                int rows = statement.executeUpdate();

                if (rows > 0) {

                    sendResponse(
                            exchange,
                            200,
                            "{\"message\":\"Budget saved successfully\"}");

                } else {

                    sendResponse(
                            exchange,
                            500,
                            "{\"error\":\"Failed to save budget\"}");
                }
            }

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid budget amount\"}");

        } catch (SQLException e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Database error\"}");

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid budget data\"}");
        }
    }

    // *******************************************/

    private static void handleLogin(
            HttpExchange exchange)
            throws IOException {

        // =========================
        // CORS
        // =========================

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "POST, OPTIONS");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type");

        String method = exchange.getRequestMethod();

        // =========================
        // OPTIONS
        // =========================

        if (method.equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(
                    204,
                    -1);

            exchange.close();

            return;
        }

        // =========================
        // ONLY POST
        // =========================

        if (!method.equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}");

            return;
        }

        // =========================
        // READ REQUEST
        // =========================

        String requestBody = new String(
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        System.out.println(
                "Login request: "
                        + requestBody);

        try {

            String email = getJsonValue(
                    requestBody,
                    "email");

            String password = getJsonValue(
                    requestBody,
                    "password");

            email = email.trim();

            // =========================
            // VALIDATION
            // =========================

            if (email.isEmpty() ||
                    password.isEmpty()) {

                sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"Email and password are required\"}");

                return;
            }

            // =========================
            // FIND USER
            // =========================

            String sql = """
                    SELECT id, email, password
                    FROM users
                    WHERE email = ?
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(
                        1,
                        email);

                ResultSet resultSet = statement.executeQuery();

                // =========================
                // USER NOT FOUND
                // =========================

                if (!resultSet.next()) {

                    sendResponse(
                            exchange,
                            401,
                            "{\"error\":\"Invalid email or password\"}");

                    return;
                }

                int userId = resultSet.getInt("id");

                String storedPassword = resultSet.getString(
                        "password");

                // =========================
                // CHECK PASSWORD
                // =========================

                if (!password.equals(
                        storedPassword)) {

                    sendResponse(
                            exchange,
                            401,
                            "{\"error\":\"Invalid email or password\"}");

                    return;
                }

                // =========================
                // SUCCESS
                // =========================

                String response = "{"
                        + "\"message\":\"Login successful\","
                        + "\"userId\":"
                        + userId
                        + ","
                        + "\"email\":\""
                        + escapeJson(email)
                        + "\""
                        + "}";

                sendResponse(
                        exchange,
                        200,
                        response);
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "{\"error\":\"Login failed\"}");
        }
    }

    private static void handleRegister(
            HttpExchange exchange)
            throws IOException {

        // =========================
        // CORS
        // =========================

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "POST, OPTIONS");

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type");

        String method = exchange.getRequestMethod();

        // =========================
        // OPTIONS
        // =========================

        if (method.equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(
                    204,
                    -1);

            exchange.close();

            return;
        }

        // =========================
        // ONLY POST
        // =========================

        if (!method.equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}");

            return;
        }

        // =========================
        // READ REQUEST
        // =========================

        String requestBody = new String(
                exchange.getRequestBody()
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        System.out.println(
                "Register request: "
                        + requestBody);

        try {

            String name = getJsonValue(
                    requestBody,
                    "name");

            String email = getJsonValue(
                    requestBody,
                    "email");

            String password = getJsonValue(
                    requestBody,
                    "password");

            name = name.trim();
            email = email.trim();

            // =========================
            // VALIDATION
            // =========================

            if (name.isEmpty()
                    || email.isEmpty()
                    || password.isEmpty()) {

                sendResponse(
                        exchange,
                        400,
                        "{\"error\":\"All fields are required\"}");

                return;
            }

            // =========================
            // INSERT USER
            // =========================

            String sql = """
                    INSERT INTO users
                    (name, email, password)
                    VALUES (?, ?, ?)
                    """;

            try (
                    Connection connection = DatabaseConnection.getConnection();

                    PreparedStatement statement = connection.prepareStatement(
                            sql,
                            java.sql.Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(
                        1,
                        name);

                statement.setString(
                        2,
                        email);

                statement.setString(
                        3,
                        password);

                int rowsInserted = statement.executeUpdate();

                if (rowsInserted == 0) {

                    sendResponse(
                            exchange,
                            500,
                            "{\"error\":\"Registration failed\"}");

                    return;
                }

                // =========================
                // GET NEW USER ID
                // =========================

                int userId;

                try (
                        ResultSet generatedKeys = statement.getGeneratedKeys()) {

                    if (!generatedKeys.next()) {

                        sendResponse(
                                exchange,
                                500,
                                "{\"error\":\"Could not get user ID\"}");

                        return;
                    }

                    userId = generatedKeys.getInt(1);
                }

                System.out.println(
                        "New user created. User ID: "
                                + userId);

                // =========================
                // CREATE DEFAULT CATEGORIES
                // =========================

                String categorySql = """
                        INSERT INTO categories
                        (name, user_id)
                        VALUES (?, ?)
                        """;

                String[] defaultCategories = {
                        "Food",
                        "Travel",
                        "Shopping",
                        "Entertainment",
                        "Education",
                        "Bills",
                        "Healthcare",
                        "Rent",
                        "Other",
                        "Gym"
                };

                try (
                        PreparedStatement categoryStatement = connection.prepareStatement(
                                categorySql)) {

                    for (String category : defaultCategories) {

                        categoryStatement.setString(
                                1,
                                category);

                        categoryStatement.setInt(
                                2,
                                userId);

                        categoryStatement.addBatch();
                    }

                    categoryStatement.executeBatch();
                }

                System.out.println(
                        "Default categories created for user "
                                + userId);

                // =========================
                // SUCCESS
                // =========================

                sendResponse(
                        exchange,
                        201,
                        "{\"message\":\"Registration successful\"}");

            } catch (SQLException e) {

                // =========================
                // DUPLICATE EMAIL
                // =========================

                if (e.getErrorCode() == 1062) {

                    sendResponse(
                            exchange,
                            409,
                            "{\"error\":\"Email already registered\"}");

                } else {

                    e.printStackTrace();

                    sendResponse(
                            exchange,
                            500,
                            "{\"error\":\"Database error\"}");
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid registration data\"}");
        }
    }
}
