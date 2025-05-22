package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.Category;
import org.example.restaurant_management_system.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemService {

    private final Connection connection;

    public MenuItemService(Connection connection) {
        this.connection = connection;
    }
//збереження
    public void saveMenuItem(MenuItem item) throws SQLException {
        String insertQuery = "INSERT INTO menu_items (name, category_id, price, vegetarian, allergen, gluten_free) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getName());
            stmt.setInt(2, item.getCategoryId());
            stmt.setDouble(3, item.getPrice());
            stmt.setBoolean(4, item.isVegetarian());
            stmt.setBoolean(5, item.isAllergen());
            stmt.setBoolean(6, item.isGlutenFree());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Створення пункту меню не вдалося, жодного рядка не змінено.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Створення пункту меню не вдалося, не отримано ID.");
                }
            }
        }
    }
//оновлення
    public void updateMenuItem(MenuItem item) throws SQLException {
        String updateQuery = "UPDATE menu_items SET name = ?, category_id = ?, price = ?, vegetarian = ?, allergen = ?, gluten_free = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, item.getName());
            stmt.setInt(2, item.getCategoryId());
            stmt.setDouble(3, item.getPrice());
            stmt.setBoolean(4, item.isVegetarian());
            stmt.setBoolean(5, item.isAllergen());
            stmt.setBoolean(6, item.isGlutenFree());
            stmt.setInt(7, item.getId());

            stmt.executeUpdate();
        }
    }
//видалення
    public void deleteMenuItem(int itemId) throws SQLException {
        String deleteQuery = "DELETE FROM menu_items WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, itemId);
            stmt.executeUpdate();
        }
    }
//отримання всіх елементів
    public List<MenuItem> getAllMenuItems() throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();

        String query = "SELECT m.id, m.name, m.category_id, m.price, m.vegetarian, m.allergen, m.gluten_free, " +
                "c.name as category_name " +
                "FROM menu_items m " +
                "JOIN categories c ON m.category_id = c.id";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"));
                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getDouble("price"),
                        rs.getBoolean("vegetarian"),
                        rs.getBoolean("allergen"),
                        rs.getBoolean("gluten_free")
                );
                item.setCategory(category);
                menuItems.add(item);
            }
        }
        return menuItems;
    }
//отримання за id
    public MenuItem getMenuItemById(int id) throws SQLException {
        String query = "SELECT m.id, m.name, m.category_id, m.price, m.vegetarian, m.allergen, m.gluten_free, " +
                "c.name as category_name " +
                "FROM menu_items m " +
                "JOIN categories c ON m.category_id = c.id " +
                "WHERE m.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"));
                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getDouble("price"),
                        rs.getBoolean("vegetarian"),
                        rs.getBoolean("allergen"),
                        rs.getBoolean("gluten_free")
                );
                item.setCategory(category);
                return item;
            } else {
                return null;
            }
        }
    }

//фільтрація
    public List<MenuItem> filterMenuItems(
            Integer categoryId,
            boolean vegetarianOnly,
            boolean allergenFreeOnly,
            boolean glutenFreeOnly
    ) throws SQLException {
        List<MenuItem> filteredItems = new ArrayList<>();

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT m.id, m.name, m.category_id, m.price, m.vegetarian, m.allergen, m.gluten_free, " +
                        "c.name as category_name " +
                        "FROM menu_items m " +
                        "JOIN categories c ON m.category_id = c.id WHERE 1=1"
        );

        if (categoryId != null) {
            queryBuilder.append(" AND m.category_id = ?");
        }
        if (vegetarianOnly) {
            queryBuilder.append(" AND m.vegetarian = true");
        }
        if (allergenFreeOnly) {
            queryBuilder.append(" AND m.allergen = false");
        }
        if (glutenFreeOnly) {
            queryBuilder.append(" AND m.gluten_free = true");
        }

        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;
            if (categoryId != null) {
                stmt.setInt(paramIndex++, categoryId);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"));
                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getDouble("price"),
                        rs.getBoolean("vegetarian"),
                        rs.getBoolean("allergen"),
                        rs.getBoolean("gluten_free")
                );
                item.setCategory(category);
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }
}