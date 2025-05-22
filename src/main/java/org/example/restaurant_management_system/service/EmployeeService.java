package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.exception.AuthenticationException;
import org.example.restaurant_management_system.util.DatabaseConnection;
import org.example.restaurant_management_system.model.Employee;
import org.example.restaurant_management_system.model.Position;

import java.sql.*;

public class EmployeeService {

    public static Employee authenticateById(int id, String password) {
        String userQuery = "SELECT e.id, " +
                "e.first_name, " +
                "e.last_name, " +
                "e.password, " +
                "e.position_id, " +
                "p.position_name as position_name " +
                "FROM employees e JOIN positions p ON e.position_id = p.id " +
                "WHERE e.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(userQuery)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (dbPassword.equals(password.trim())) {
                    Position position = new Position(rs.getInt("position_id"), rs.getString("position_name"));
                    return new Employee(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            position,
                            dbPassword
                    );
                } else {
                    throw new AuthenticationException("Неправильний пароль.");
                }
            } else {

                throw new AuthenticationException("Користувача з таким ID не знайдено.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthenticationException("Помилка бази даних під час автентифікації.");
        }

    }
}