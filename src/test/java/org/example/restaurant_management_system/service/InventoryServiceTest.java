package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.Ingredient;
import org.example.restaurant_management_system.model.MenuItem;
import org.example.restaurant_management_system.model.MenuItemIngredient;
import org.example.restaurant_management_system.model.Stock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    private Connection h2Connection;
    private InventoryService inventoryService;
    private MockedStatic<org.example.restaurant_management_system.
            util.DatabaseConnection> mockedDbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:testinventorydb;DB_CLOSE_DELAY=-1");

        mockedDbConnection = Mockito.mockStatic(org.example.
                restaurant_management_system.util.DatabaseConnection.class);

        mockedDbConnection.when(org.example.restaurant_management_system.
                        util.DatabaseConnection::getConnection)
                .thenReturn(h2Connection);

        inventoryService = new InventoryService(h2Connection);

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DROP ALL OBJECTS;");


            stmt.execute("CREATE TABLE ingredients (id INT AUTO_INCREMENT PRIMARY KEY," +
                    " name VARCHAR(255) NOT NULL, unit VARCHAR(50), expiration_date DATE)");

            stmt.execute("CREATE TABLE stock (id INT AUTO_INCREMENT PRIMARY KEY," +
                    " ingredient_id INT, change_amount DOUBLE, movement_type VARCHAR(50)," +
                    " movement_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (ingredient_id) REFERENCES ingredients(id))");

            stmt.execute("CREATE TABLE categories (id INT AUTO_INCREMENT PRIMARY KEY," +
                    " name VARCHAR(255) NOT NULL)");
            stmt.execute("CREATE TABLE menu_items (id INT AUTO_INCREMENT PRIMARY KEY," +
                    " name VARCHAR(255) NOT NULL, price DOUBLE, category_id INT," +
                    " vegetarian BOOLEAN, allergen BOOLEAN, gluten_free BOOLEAN, " +
                    "FOREIGN KEY (category_id) REFERENCES categories(id))");

            stmt.execute("CREATE TABLE menu_item_ingredients (id INT AUTO_INCREMENT PRIMARY KEY," +
                    " menu_item_id INT, ingredient_id INT, quantity_per_unit DOUBLE," +
                    " FOREIGN KEY (menu_item_id) REFERENCES menu_items(id), " +
                    "FOREIGN KEY (ingredient_id) REFERENCES ingredients(id))");

            stmt.execute("INSERT INTO ingredients (name, unit, expiration_date) " +
                    "VALUES ('Flour', 'kg', '2025-12-31')");

            stmt.execute("INSERT INTO ingredients (name, unit, expiration_date) " +
                    "VALUES ('Sugar', 'kg', '2025-06-15')");

            stmt.execute("INSERT INTO ingredients (name, unit, expiration_date)" +
                    " VALUES ('Milk', 'liter', '" + LocalDate.now().minusDays(5) + "')");

            stmt.execute("INSERT INTO ingredients (name, unit, expiration_date) " +
                    "VALUES ('Eggs', 'pcs', '" + LocalDate.now().plusDays(3) + "')");

            stmt.execute("INSERT INTO ingredients (name, unit, expiration_date) " +
                    "VALUES ('Salt', 'kg', NULL)");


            stmt.execute("INSERT INTO stock (ingredient_id, change_amount," +
                    " movement_type,movement_time) VALUES " +
                    "(1, 10.0, 'income', '" + LocalDateTime.now().minusDays(2) + "')");

            stmt.execute("INSERT INTO stock (ingredient_id, change_amount," +
                    " movement_type,movement_time) VALUES " +
                    "(1, -2.0, 'outcome', '" + LocalDateTime.now().minusDays(1) + "')");

            stmt.execute("INSERT INTO stock (ingredient_id, change_amount," +
                    " movement_type, movement_time) VALUES " +
                    "(2, 5.0, 'income', '" + LocalDateTime.now().minusDays(3) + "')");

            stmt.execute("INSERT INTO stock (ingredient_id, change_amount, " +
                    "movement_type, movement_time) " +
                    "VALUES (3, 3.0, 'income', '" + LocalDateTime.now().minusDays(6) + "')");

            stmt.execute("INSERT INTO stock (ingredient_id, change_amount," +
                    " movement_type,movement_time) VALUES " +
                    "(4, 12.0, 'income', '" + LocalDateTime.now().minusDays(4) + "')");

            stmt.execute("INSERT INTO stock (ingredient_id, change_amount," +
                    " movement_type,movement_time) VALUES " +
                    "(5, 10.0, 'income', '" + LocalDateTime.now().minusDays(5) + "')");

            stmt.execute("INSERT INTO categories (id, name) VALUES (1, 'Desserts')");

            stmt.execute("INSERT INTO categories (id, name) VALUES (2, 'Main Course')");

            stmt.execute("INSERT INTO menu_items (id, name, price, category_id," +
                    " vegetarian, allergen, gluten_free) VALUES (101, 'Chocolate Cake'," +
                    " 8.50, 1, TRUE, TRUE, FALSE)");

            stmt.execute("INSERT INTO menu_items (id, name, price, category_id," +
                    " vegetarian, allergen, gluten_free) VALUES (102, 'Pasta Carbonara'," +
                    " 12.00, 2, FALSE, FALSE, FALSE)");

            stmt.execute("INSERT INTO menu_item_ingredients (menu_item_id, " +
                    "ingredient_id, quantity_per_unit) VALUES (101, 1, 0.5)");

            stmt.execute("INSERT INTO menu_item_ingredients (menu_item_id," +
                    " ingredient_id, quantity_per_unit) VALUES (101, 2, 0.3)");
        }
    }
    @AfterEach
    void tearDown() throws SQLException {
        if (mockedDbConnection != null) {
            mockedDbConnection.close();
            mockedDbConnection = null;
        }

        if (h2Connection != null && !h2Connection.isClosed()) {
            try (Statement stmt = h2Connection.createStatement()) {
                stmt.execute("DROP ALL OBJECTS;");
            } finally {
                h2Connection.close();
            }
        }
        h2Connection = null;
    }

    @Test
    void getIngredientById_existingIngredient() throws SQLException {
        Ingredient ingredient = inventoryService.getIngredientById(1);
        assertNotNull(ingredient);
        assertEquals(1, ingredient.getId());
        assertEquals("Flour", ingredient.getName());
        assertEquals("kg", ingredient.getUnit());
        assertEquals(LocalDate.of(2025, 12, 31), ingredient.getExpirationDate());
        assertEquals(8.0, ingredient.getCurrentStock());
    }

    @Test
    void getIngredientById_nonExistingIngredient() throws SQLException {
        Ingredient ingredient = inventoryService.getIngredientById(999);
        assertNull(ingredient);
    }

    @Test
    void getIngredientById_ingredientWithNoExpirationDate() throws SQLException {
        Ingredient ingredient = inventoryService.getIngredientById(5);
        assertNotNull(ingredient);
        assertEquals(5, ingredient.getId());
        assertEquals("Salt", ingredient.getName());
        assertNull(ingredient.getExpirationDate());
        assertEquals(10.0, ingredient.getCurrentStock());
    }

    @Test
    void updateIngredientExpiration_validUpdate() throws SQLException {
        LocalDate newDate = LocalDate.of(2026, 1, 1);
        inventoryService.updateIngredientExpiration(1, newDate);

        Ingredient updatedIngredient = inventoryService.getIngredientById(1);
        assertNotNull(updatedIngredient);
        assertEquals(newDate, updatedIngredient.getExpirationDate());
    }

    @Test
    void updateIngredientExpiration_setToNull() throws SQLException {
        inventoryService.updateIngredientExpiration(1, null);

        Ingredient updatedIngredient = inventoryService.getIngredientById(1);
        assertNotNull(updatedIngredient);
        assertNull(updatedIngredient.getExpirationDate());
    }

    @Test
    void createNewIngredient_successfulCreation() throws SQLException {
        String name = "New Oil";
        String unit = "liter";
        double initialStock = 2.5;
        LocalDate expirationDate = LocalDate.of(2025, 10, 10);

        inventoryService.createNewIngredient(name, unit, initialStock, expirationDate);

        List<Ingredient> ingredients = inventoryService.getAllIngredientsWithStock();
        assertEquals(6, ingredients.size());

        Ingredient newIngredient = ingredients.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElse(null);

        assertNotNull(newIngredient);
        assertTrue(newIngredient.getId() > 0);
        assertEquals(name, newIngredient.getName());
        assertEquals(unit, newIngredient.getUnit());
        assertEquals(expirationDate, newIngredient.getExpirationDate());
        assertEquals(initialStock, newIngredient.getCurrentStock());
    }

    @Test
    void createNewIngredient_noExpirationDate() throws SQLException {
        String name = "Pepper";
        String unit = "g";
        double initialStock = 0.5;

        inventoryService.createNewIngredient(name, unit, initialStock, null);

        List<Ingredient> ingredients = inventoryService.getAllIngredientsWithStock();
        assertEquals(6, ingredients.size());

        Ingredient newIngredient = ingredients.stream()
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElse(null);

        assertNotNull(newIngredient);
        assertTrue(newIngredient.getId() > 0);
        assertEquals(name, newIngredient.getName());
        assertEquals(unit, newIngredient.getUnit());
        assertNull(newIngredient.getExpirationDate());
        assertEquals(initialStock, newIngredient.getCurrentStock());
    }

    @Test
    void calculateCurrentStock_existingIngredient() throws SQLException {
        double stock = inventoryService.calculateCurrentStock(1);
        assertEquals(8.0, stock);
    }

    @Test
    void calculateCurrentStock_nonExistingIngredient() throws SQLException {
        double stock = inventoryService.calculateCurrentStock(999);
        assertEquals(0.0, stock);
    }

    @Test
    void getAllIngredientsWithStock_returnsAll() throws SQLException {
        List<Ingredient> ingredients = inventoryService.getAllIngredientsWithStock();
        assertNotNull(ingredients);
        assertEquals(5, ingredients.size());

        Ingredient flour = ingredients.stream().filter(i ->
                i.getName().equals("Flour")).findFirst().orElse(null);

        assertNotNull(flour);
        assertEquals(8.0, flour.getCurrentStock());
    }

    @Test
    void addStockMovement_successfulAddition() throws SQLException {
        Ingredient sugar = inventoryService.getIngredientById(2);
        assertNotNull(sugar);
        assertEquals(5.0, sugar.getCurrentStock());

        Stock newMovement = new Stock(sugar, 2.0,
                "income");
        inventoryService.addStockMovement(newMovement);

        assertTrue(newMovement.getId() > 0);
        assertEquals(7.0, inventoryService.calculateCurrentStock(2));
    }

    @Test
    void getExpiredIngredients_returnsCorrectly() throws SQLException {
        List<Ingredient> expired = inventoryService.getExpiredIngredients();
        assertNotNull(expired);

        assertEquals(1, expired.size());
        assertEquals("Milk", expired.get(0).getName());
        assertEquals(3.0, expired.get(0).getCurrentStock());
    }

    @Test
    void getExpiredIngredients_noExpired() throws SQLException {
        inventoryService.updateIngredientExpiration(3, LocalDate.now().
                plusDays(10));

        List<Ingredient> expired = inventoryService.getExpiredIngredients();
        assertTrue(expired.isEmpty());
    }

    @Test
    void getIngredientsExpiringSoon_returnsCorrectly() throws SQLException {
        List<Ingredient> expiringSoon = inventoryService.getIngredientsExpiringSoon(5);
        assertNotNull(expiringSoon);
        assertEquals(1, expiringSoon.size());
        assertEquals("Eggs", expiringSoon.get(0).getName());
        assertEquals(12.0, expiringSoon.get(0).getCurrentStock());
    }

    @Test
    void getIngredientsExpiringSoon_noExpiringSoon() throws SQLException {
        List<Ingredient> expiringSoon = inventoryService.getIngredientsExpiringSoon(0);
        assertTrue(expiringSoon.isEmpty());
    }

    @Test
    void getAllMenuItems_returnsAll() throws SQLException {
        List<MenuItem> menuItems = inventoryService.getAllMenuItems();
        assertNotNull(menuItems);
        assertEquals(2, menuItems.size());
        assertEquals("Chocolate Cake", menuItems.get(0).getName());
        assertEquals("Pasta Carbonara", menuItems.get(1).getName());
    }

    @Test
    void getMenuItemIngredients_existingMenuItem() throws SQLException {
        List<MenuItemIngredient> ingredients = inventoryService.
                getMenuItemIngredients(101);
        assertNotNull(ingredients);
        assertEquals(2, ingredients.size());

        MenuItemIngredient flourItem = ingredients.stream().filter(mii ->
                "Flour".equals(mii.getIngredient().getName())).findFirst().orElse(null);

        assertNotNull(flourItem);
        assertEquals(101, flourItem.getMenuItemId());
        assertEquals(1, flourItem.getIngredient().getId());
        assertEquals(0.5, flourItem.getQuantityPerUnit());

        MenuItemIngredient sugarItem = ingredients.stream().filter(mii ->
                "Sugar".equals(mii.getIngredient().getName())).findFirst().orElse(null);

        assertNotNull(sugarItem);
        assertEquals(101, sugarItem.getMenuItemId());
        assertEquals(2, sugarItem.getIngredient().getId());
        assertEquals(0.3, sugarItem.getQuantityPerUnit());
    }

    @Test
    void getMenuItemIngredients_nonExistingMenuItem() throws SQLException {
        List<MenuItemIngredient> ingredients = inventoryService.getMenuItemIngredients(999);
        assertNotNull(ingredients);
        assertTrue(ingredients.isEmpty());
    }

    @Test
    void addMenuItemIngredient_successfulAddition() throws SQLException {
        MenuItem cake = inventoryService.getAllMenuItems().stream()
                .filter(m -> m.getName().equals("Chocolate Cake"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Chocolate Cake not found"));
        Ingredient milk = inventoryService.getIngredientById(3);
        assertNotNull(milk);

        MenuItemIngredient newItemIngredient = new MenuItemIngredient(cake,
                milk, 0.1);
        inventoryService.addMenuItemIngredient(newItemIngredient);

        assertTrue(newItemIngredient.getId() > 0);
        List<MenuItemIngredient> ingredientsForCake = inventoryService.getMenuItemIngredients(cake.getId());
        assertEquals(3, ingredientsForCake.size());
    }

    @Test
    void addMenuItemIngredient_ingredientAlreadyExists() throws SQLException {
        MenuItem cake = inventoryService.getAllMenuItems().stream()
                .filter(m -> m.getName().equals("Chocolate Cake"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Chocolate Cake not found"));
        Ingredient flour = inventoryService.getIngredientById(1);
        assertNotNull(flour);

        MenuItemIngredient existingItemIngredient = new MenuItemIngredient(cake, flour,
                0.6);

        SQLException thrown = assertThrows(SQLException.class, () -> {
            inventoryService.addMenuItemIngredient(existingItemIngredient);
        });
        assertEquals("інгредієнт вже є в рецепті цієї страви. " +
                "використайте функцію 'оновити'.", thrown.getMessage());
    }

    @Test
    void addMenuItemIngredient_nullIngredientObject() {
        MenuItem cake = new MenuItem(101, "Test Cake", 1,
                10.0, true, false, false);

       MenuItemIngredient newItemIngredient = new MenuItemIngredient(cake,
               null, 0.1);

        SQLException thrown = assertThrows(SQLException.class, () -> {
            inventoryService.addMenuItemIngredient(newItemIngredient);
        });

        assertEquals("ingredient object is null in menuitemingredient " +
                "for addmenuitemingredient.", thrown.getMessage());
    }


    @Test
    void updateMenuItemIngredient_successfulUpdate() throws SQLException {

        List<MenuItemIngredient> ingredientsForCake = inventoryService.getMenuItemIngredients(101);
        MenuItemIngredient flourInCake = ingredientsForCake.stream()
                .filter(mii -> "Flour".equals(mii.getIngredient().getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Flour in cake not found"));

        double newQuantity = 0.7;
        flourInCake.setQuantityPerUnit(newQuantity);
        inventoryService.updateMenuItemIngredient(flourInCake);

        List<MenuItemIngredient> updatedIngredientsForCake = inventoryService.
                getMenuItemIngredients(101);

        MenuItemIngredient updatedFlourInCake = updatedIngredientsForCake.stream()
                .filter(mii -> "Flour".equals(mii.getIngredient().getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Updated Flour in cake not found"));
        assertEquals(newQuantity, updatedFlourInCake.getQuantityPerUnit());
    }

    @Test
    void removeMenuItemIngredient_successfulRemoval() throws SQLException {
        List<MenuItemIngredient> initialIngredientsForCake = inventoryService.
                getMenuItemIngredients(101);

        assertEquals(2, initialIngredientsForCake.size());

        int itemIngredientIdToRemove = initialIngredientsForCake.get(0).getId();

        inventoryService.removeMenuItemIngredient(itemIngredientIdToRemove);

        List<MenuItemIngredient> remainingIngredientsForCake = inventoryService.
                getMenuItemIngredients(101);

        assertEquals(1, remainingIngredientsForCake.size());
        assertFalse(remainingIngredientsForCake.stream().anyMatch(mii
                -> mii.getId() == itemIngredientIdToRemove));
    }

    @Test
    void removeMenuItemIngredient_nonExistingId() throws SQLException {
        int initialCount = inventoryService.getMenuItemIngredients(101).size();
        inventoryService.removeMenuItemIngredient(9999);
        assertEquals(initialCount, inventoryService.
                getMenuItemIngredients(101).size());
    }
}