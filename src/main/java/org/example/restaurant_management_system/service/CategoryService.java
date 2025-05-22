package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {

    private final Connection connection;

    public CategoryService(Connection connection) {
        this.connection = connection;
    }

    public void addCategory(Category category) throws SQLException {
        String insertQuery = "INSERT INTO categories (name) VALUES (?)";
        // Використовуємо connection з поля класу
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Створення категорії не вдалося, жодного рядка не змінено.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));//автозаповнення id
                } else {
                    throw new SQLException("Створення категорії не вдалося, не отримано ID.");
                }
            }
        }
    }

// оновлення
    public void updateCategory(Category category) throws SQLException {
        String updateQuery = "UPDATE categories SET name = ? WHERE id = ?";
        // Використовуємо connection з поля класу
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getId());
            stmt.executeUpdate();
        }
    }

//видалення
    public void deleteCategory(int categoryId) throws SQLException {
        String deleteQuery = "DELETE FROM categories WHERE id = ?";
        // Використовуємо connection з поля класу
        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, categoryId);
            stmt.executeUpdate();
        }
    }

//отримання всх елементів
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT id, name FROM categories";
        // Використовуємо connection з поля класу
        try (PreparedStatement stmt = connection.prepareStatement(query); // Змінено на PreparedStatement
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        }
        return categories;
    }

//отримання по id
    public Category getCategoryById(int id) throws SQLException {
        String query = "SELECT id, name FROM categories WHERE id = ?";
        // Використовуємо connection з поля класу
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getInt("id"), rs.getString("name"));
                }
            }
        }
        return null;
    }
}