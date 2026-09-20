package service;

import database.DatabaseConnection;
import model.Expense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExpenseService {

    public boolean addExpense(Expense expense) {

        String sql = """
                INSERT INTO expenses
                (user_id, category_id, amount, description, expense_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, expense.getUserId());
            statement.setInt(2, expense.getCategoryId());
            statement.setDouble(3, expense.getAmount());
            statement.setString(4, expense.getDescription());
            statement.setDate(
                    5,
                    java.sql.Date.valueOf(
                            expense.getExpenseDate()));

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {

                System.out.println(
                        "Expense added successfully!");

                return true;
            }

        } catch (SQLException e) {

            System.out.println("Failed to add expense!");
            e.printStackTrace();
        }

        return false;
    }

    public List<Expense> getExpensesByUserId(int userId) {

        List<Expense> expenses = new ArrayList<>();

        String sql = """
                SELECT
                    e.id,
                    e.user_id,
                    e.category_id,
                    c.name AS category_name,
                    e.amount,
                    e.description,
                    e.expense_date
                FROM expenses e
                JOIN categories c
                    ON e.category_id = c.id
                WHERE e.user_id = ?
                ORDER BY e.expense_date DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Expense expense = new Expense();

                expense.setId(
                        resultSet.getInt("id"));

                expense.setUserId(
                        resultSet.getInt("user_id"));

                expense.setCategoryId(
                        resultSet.getInt("category_id"));

                expense.setCategoryName(
                        resultSet.getString("category_name"));

                expense.setAmount(
                        resultSet.getDouble("amount"));

                expense.setDescription(
                        resultSet.getString("description"));

                expense.setExpenseDate(
                        resultSet
                                .getDate("expense_date")
                                .toLocalDate());

                expenses.add(expense);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Failed to retrieve expenses!");

            e.printStackTrace();
        }

        return expenses;
    }

    public boolean updateExpense(Expense expense) {

        String sql = """
                UPDATE expenses
                SET category_id = ?,
                    amount = ?,
                    description = ?,
                    expense_date = ?
                WHERE id = ?
                AND user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, expense.getCategoryId());
            statement.setDouble(2, expense.getAmount());
            statement.setString(3, expense.getDescription());

            statement.setDate(
                    4,
                    java.sql.Date.valueOf(
                            expense.getExpenseDate()));

            statement.setInt(5, expense.getId());
            statement.setInt(6, expense.getUserId());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {

                System.out.println(
                        "Expense updated successfully!");

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Failed to update expense!");

            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteExpense(int expenseId, int userId) {

        String sql = """
                DELETE FROM expenses
                WHERE id = ?
                AND user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, expenseId);
            statement.setInt(2, userId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {

                System.out.println(
                        "Expense deleted successfully!");

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Failed to delete expense!");

            e.printStackTrace();
        }

        return false;
    }

    public double getMonthlySpent(int userId, int month, int year) {

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM expenses
                WHERE user_id = ?
                AND MONTH(expense_date) = ?
                AND YEAR(expense_date) = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, userId);
            statement.setInt(2, month);
            statement.setInt(3, year);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Failed to calculate monthly spending!");

            e.printStackTrace();
        }

        return 0;
    }
}