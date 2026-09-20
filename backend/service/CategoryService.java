package service;

import database.DatabaseConnection;
import model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {

    public List<Category> getCategories(int userId) {

        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT id, name, user_id
                FROM categories
                WHERE user_id IS NULL
                   OR user_id = ?
                ORDER BY name
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Category category = new Category();

                category.setId(
                        resultSet.getInt("id"));

                category.setName(
                        resultSet.getString("name"));

                int userIdValue = resultSet.getInt("user_id");

                if (resultSet.wasNull()) {
                    category.setUserId(null);
                } else {
                    category.setUserId(userIdValue);
                }

                categories.add(category);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Failed to retrieve categories!");

            e.printStackTrace();
        }

        return categories;
    }

    public boolean addCategory(Category category) {

        String checkSql = """
                SELECT id
                FROM categories
                WHERE name = ?
                AND (user_id IS NULL OR user_id = ?)
                """;

        String insertSql = """
                INSERT INTO categories (name, user_id)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            // Check whether category already exists
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);

            checkStatement.setString(1, category.getName());
            checkStatement.setInt(2, category.getUserId());

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Category already exists!");
                return false;
            }

            // Create category
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);

            insertStatement.setString(1, category.getName());
            insertStatement.setInt(2, category.getUserId());

            int rowsInserted = insertStatement.executeUpdate();

            if (rowsInserted > 0) {

                System.out.println(
                        "Category added successfully!");

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Failed to add category!");

            e.printStackTrace();
        }

        return false;
    }
}