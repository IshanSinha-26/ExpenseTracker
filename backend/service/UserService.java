package service;

import database.DatabaseConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    public boolean registerUser(User user) {

        String checkSql = "SELECT id FROM users WHERE email = ?";

        String insertSql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {

            // Check whether email already exists
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);

            checkStatement.setString(1, user.getEmail());

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Email already registered!");
                return false;
            }

            // Insert new user
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);

            insertStatement.setString(1, user.getName());
            insertStatement.setString(2, user.getEmail());
            insertStatement.setString(3, user.getPassword());

            int rowsInserted = insertStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("User registered successfully!");
                return true;
            }

        } catch (SQLException e) {

            System.out.println("Registration failed!");
            e.printStackTrace();
        }

        return false;
    }

    public User loginUser(String email, String password) {

        String sql = "SELECT id, name, email, password FROM users WHERE email = ? AND password = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                User user = new User();

                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));

                return user;
            }

        } catch (SQLException e) {

            System.out.println("Login failed!");
            e.printStackTrace();
        }

        return null;
    }
}