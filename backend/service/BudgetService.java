package service;

import database.DatabaseConnection;
import model.Budget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BudgetService {

    public boolean setBudget(Budget budget) {

        String sql = """
                INSERT INTO budgets
                (user_id, amount, month, year)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                amount = VALUES(amount)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, budget.getUserId());
            statement.setDouble(2, budget.getAmount());
            statement.setInt(3, budget.getMonth());
            statement.setInt(4, budget.getYear());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {

                System.out.println(
                        "Budget saved successfully!");

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Failed to save budget!");

            e.printStackTrace();
        }

        return false;
    }

    public Budget getBudget(int userId, int month, int year) {

        String sql = """
                SELECT id, user_id, amount, month, year
                FROM budgets
                WHERE user_id = ?
                AND month = ?
                AND year = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, userId);
            statement.setInt(2, month);
            statement.setInt(3, year);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                return new Budget(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getDouble("amount"),
                        resultSet.getInt("month"),
                        resultSet.getInt("year"));
            }

        } catch (SQLException e) {

            System.out.println(
                    "Failed to retrieve budget!");

            e.printStackTrace();
        }

        return null;
    }

    public String getBudgetStatus(double budgetAmount, double spentAmount) {

        if (budgetAmount <= 0) {
            return "NO BUDGET";
        }

        double percentage = (spentAmount / budgetAmount) * 100;

        if (percentage >= 100) {
            return "OVER BUDGET";
        }

        if (percentage >= 80) {
            return "WARNING";
        }

        return "NORMAL";
    }

    public double getBudgetPercentage(
            double budgetAmount,
            double spentAmount) {

        if (budgetAmount <= 0) {
            return 0;
        }

        return (spentAmount / budgetAmount) * 100;
    }
}