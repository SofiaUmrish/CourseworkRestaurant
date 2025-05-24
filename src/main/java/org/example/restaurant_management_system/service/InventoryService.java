package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.Ingredient;
import org.example.restaurant_management_system.model.MenuItem;
import org.example.restaurant_management_system.model.MenuItemIngredient;
import org.example.restaurant_management_system.model.Stock;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {

    private final Connection connection;

    public InventoryService(Connection connection) {
        this.connection = connection;
    }

    // отримує інгредієнт за його id
    public Ingredient getIngredientById(int id) throws SQLException {
        String query = "SELECT id, name, unit, expiration_date FROM ingredients WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LocalDate expirationDate = null;
                if (rs.getDate("expiration_date") != null) {
                    expirationDate = rs.getDate("expiration_date").toLocalDate();
                }
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        expirationDate
                );
                ingredient.setCurrentStock(calculateCurrentStock(ingredient.getId()));
                return ingredient;
            }
        }
        return null;
    }

    // оновлює термін придатності інгредієнта за його id
    public void updateIngredientExpiration(int ingredientId, LocalDate newExpirationDate) throws SQLException {
        String sql = "UPDATE ingredients SET expiration_date = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (newExpirationDate == null) {
                pstmt.setNull(1, java.sql.Types.DATE);
            } else {
                pstmt.setDate(1, java.sql.Date.valueOf(newExpirationDate));
            }
            pstmt.setInt(2, ingredientId);
            pstmt.executeUpdate();
        }
    }

    // створює новий інгредієнт та додає початковий рух запасів
    public void createNewIngredient(String name, String unit, double initialStock, LocalDate expirationDate) throws SQLException {
        String insertIngredientSql = "INSERT INTO ingredients (name, unit, expiration_date) VALUES (?, ?, ?)";
        int newIngredientId;
        try (PreparedStatement pstmt = connection.prepareStatement(insertIngredientSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, unit);
            if (expirationDate == null) {
                pstmt.setNull(3, java.sql.Types.DATE);
            } else {
                pstmt.setDate(3, java.sql.Date.valueOf(expirationDate));
            }
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newIngredientId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("creating ingredient failed, no id obtained.");
                }
            }
        }
        Ingredient newIngredient = new Ingredient(newIngredientId, name, unit, expirationDate);
        newIngredient.setCurrentStock(initialStock);

        Stock initialStockMovement = new Stock(newIngredient, initialStock, "income");
        addStockMovement(initialStockMovement);
    }

    // обчислює поточний запас інгредієнта за його id
    public double calculateCurrentStock(int ingredientId) throws SQLException {
        String query = "SELECT COALESCE(SUM(change_amount), 0) AS total FROM stock WHERE ingredient_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    // отримує список всіх інгредієнтів з їх поточним запасом
    public List<Ingredient> getAllIngredientsWithStock() throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT id, name, unit, expiration_date FROM ingredients";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                LocalDate expirationDate = null;
                if (rs.getDate("expiration_date") != null) {
                    expirationDate = rs.getDate("expiration_date").toLocalDate();
                }
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        expirationDate
                );
                ingredient.setCurrentStock(calculateCurrentStock(ingredient.getId()));
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    // додає новий рух запасів до бази даних
    public void addStockMovement(Stock movement) throws SQLException {
        String insertQuery = "INSERT INTO stock (ingredient_id, change_amount, movement_type, movement_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, movement.getIngredientId());
            stmt.setDouble(2, movement.getChangeAmount());
            stmt.setString(3, movement.getMovementType());
            stmt.setTimestamp(4, Timestamp.valueOf(movement.getMovementTime()));
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                movement.setId(generatedKeys.getInt(1));
            }
        }
    }

    // записує використання інгредієнтів для приготування страви
    public void recordIngredientsUsageForMenuItem(int menuItemId, int quantity) throws SQLException {
        List<MenuItemIngredient> ingredientsForMenuItem = getMenuItemIngredients(menuItemId);
        for (MenuItemIngredient item : ingredientsForMenuItem) {
            if (item.getIngredient() == null) {
                Ingredient loadedIngredient = getIngredientById(item.getIngredientId());
                if (loadedIngredient != null) {
                    item.setIngredient(loadedIngredient);
                } else {
                    throw new SQLException("інгредієнт для пункту меню " + menuItemId + " (ingredient id: " + item.getIngredientId() + ") не знайдено або не завантажено.");
                }
            }

            double totalUsed = item.getQuantityPerUnit() * quantity;
            double currentStock = calculateCurrentStock(item.getIngredient().getId());
            if (currentStock < totalUsed) {
                throw new SQLException("недостатньо " + item.getIngredient().getName() + " на складі для приготування страви id " + menuItemId + ". доступно: " + currentStock + ", потрібно: " + totalUsed);
            }

            Stock expenseMovement = new Stock(
                    item.getIngredient(),
                    -totalUsed,
                    "expense"
            );
            addStockMovement(expenseMovement);
        }
    }

    // отримує список всіх прострочених інгредієнтів з наявним запасом
    public List<Ingredient> getExpiredIngredients() throws SQLException {
        List<Ingredient> expired = new ArrayList<>();
        String query = "SELECT id, name, unit, expiration_date FROM ingredients WHERE expiration_date IS NOT NULL AND expiration_date < ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate expirationDate = null;
                if (rs.getDate("expiration_date") != null) {
                    expirationDate = rs.getDate("expiration_date").toLocalDate();
                }
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        expirationDate
                );
                double currentStock = calculateCurrentStock(ingredient.getId());
                if (currentStock > 0) {
                    ingredient.setCurrentStock(currentStock);
                    expired.add(ingredient);
                }
            }
        }
        return expired;
    }

    // отримує список інгредієнтів, термін придатності яких скоро закінчується
    public List<Ingredient> getIngredientsExpiringSoon(int days) throws SQLException {
        List<Ingredient> expiringSoon = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(days);
        String query = "SELECT id, name, unit, expiration_date FROM ingredients WHERE expiration_date IS NOT NULL AND expiration_date BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(today));
            stmt.setDate(2, Date.valueOf(threshold));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate expirationDate = null;
                if (rs.getDate("expiration_date") != null) {
                    expirationDate = rs.getDate("expiration_date").toLocalDate();
                }
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        expirationDate
                );
                double currentStock = calculateCurrentStock(ingredient.getId());
                if (currentStock > 0) {
                    ingredient.setCurrentStock(currentStock);
                    expiringSoon.add(ingredient);
                }
            }
        }
        return expiringSoon;
    }

    // отримує список всіх пунктів меню
    public List<MenuItem> getAllMenuItems() throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT id, name, price, category_id, vegetarian, allergen, gluten_free FROM menu_items";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                MenuItem menuItem = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getDouble("price"),
                        rs.getBoolean("vegetarian"),
                        rs.getBoolean("allergen"),
                        rs.getBoolean("gluten_free")
                );
                menuItems.add(menuItem);
            }
        }
        return menuItems;
    }

    // отримує список інгредієнтів для конкретного пункту меню
    public List<MenuItemIngredient> getMenuItemIngredients(int menuItemId) throws SQLException {
        List<MenuItemIngredient> ingredients = new ArrayList<>();
        String query = "SELECT mii.id AS mii_id, mii.menu_item_id, mii.ingredient_id, mii.quantity_per_unit, " +
                "i.name AS ingredient_name, i.unit AS ingredient_unit, i.expiration_date AS ingredient_exp_date, " +
                "mi.id AS mi_id, mi.name AS mi_name, mi.price AS mi_price, mi.category_id AS mi_category_id, " +
                "mi.vegetarian AS mi_vegetarian, mi.allergen AS mi_allergen, mi.gluten_free AS mi_gluten_free " +
                "FROM menu_item_ingredients mii " +
                "JOIN ingredients i ON mii.ingredient_id = i.id " +
                "JOIN menu_items mi ON mii.menu_item_id = mi.id " +
                "WHERE mii.menu_item_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, menuItemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDate expirationDate = null;
                if (rs.getDate("ingredient_exp_date") != null) {
                    expirationDate = rs.getDate("ingredient_exp_date").toLocalDate();
                }
                Ingredient ingredient = new Ingredient(
                        rs.getInt("ingredient_id"),
                        rs.getString("ingredient_name"),
                        rs.getString("ingredient_unit"),
                        expirationDate
                );
                ingredient.setCurrentStock(calculateCurrentStock(ingredient.getId()));

                MenuItem menuItem = new MenuItem(
                        rs.getInt("mi_id"),
                        rs.getString("mi_name"),
                        rs.getInt("mi_category_id"),
                        rs.getDouble("mi_price"),
                        rs.getBoolean("mi_vegetarian"),
                        rs.getBoolean("mi_allergen"),
                        rs.getBoolean("mi_gluten_free")
                );

                MenuItemIngredient item = new MenuItemIngredient(
                        rs.getInt("mii_id"),
                        menuItem,
                        ingredient,
                        rs.getDouble("quantity_per_unit")
                );

                ingredients.add(item);
            }
        }
        return ingredients;
    }

    // додає інгредієнт до рецепту страви
    public void addMenuItemIngredient(MenuItemIngredient itemIngredient) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM menu_item_ingredients WHERE menu_item_id = ? AND ingredient_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, itemIngredient.getMenuItemId());
            if (itemIngredient.getIngredient() != null) {
                checkStmt.setInt(2, itemIngredient.getIngredient().getId());
            } else {
                throw new SQLException("ingredient object is null in menuitemingredient for addmenuitemingredient.");
            }
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("інгредієнт вже є в рецепті цієї страви. використайте функцію 'оновити'.");
            }
        }

        String insertSql = "INSERT INTO menu_item_ingredients (menu_item_id, ingredient_id, quantity_per_unit) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, itemIngredient.getMenuItemId());
            if (itemIngredient.getIngredient() != null) {
                pstmt.setInt(2, itemIngredient.getIngredient().getId());
            } else {
                throw new SQLException("ingredient object is null in menuitemingredient for addmenuitemingredient.");
            }
            pstmt.setDouble(3, itemIngredient.getQuantityPerUnit());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                itemIngredient.setId(generatedKeys.getInt(1));
            }
        }
    }

    // оновлює інгредієнт в рецепті страви
    public void updateMenuItemIngredient(MenuItemIngredient itemIngredient) throws SQLException {
        String sql = "UPDATE menu_item_ingredients SET quantity_per_unit = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, itemIngredient.getQuantityPerUnit());
            pstmt.setInt(2, itemIngredient.getId());
            pstmt.executeUpdate();
        }
    }

    // видаляє інгредієнт з рецепту страви за його id
    public void removeMenuItemIngredient(int menuItemIngredientId) throws SQLException {
        String sql = "DELETE FROM menu_item_ingredients WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemIngredientId);
            pstmt.executeUpdate();
        }
    }
}