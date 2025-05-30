package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.Category;
import org.example.restaurant_management_system.model.MenuItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuItemServiceTest {

    private Connection connection;
    private MenuItemService menuItemService;
    private CategoryService categoryService;

    private static final String H2_URL = "jdbc:h2:mem:test_menuitem_db;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";


    private Category createTestCategory(String name) throws SQLException {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Category(generatedKeys.getInt(1), name);
                } else {
                    throw new SQLException("Не вдалося створити категорію, ID не отримано.");
                }
            }
        }
    }


    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(H2_URL, USER, PASSWORD);
        menuItemService = new MenuItemService(connection);

        // Створення таблиць
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS menu_items");
            stmt.execute("DROP TABLE IF EXISTS categories");

            stmt.execute("CREATE TABLE categories (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(255) NOT NULL UNIQUE" +
                    ")");

            stmt.execute("CREATE TABLE menu_items (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "category_id INT NOT NULL, " +
                    "price DECIMAL(10, 2) NOT NULL, " +
                    "vegetarian BOOLEAN DEFAULT FALSE, " +
                    "allergen BOOLEAN DEFAULT FALSE, " +
                    "gluten_free BOOLEAN DEFAULT TRUE, " +
                    "FOREIGN KEY (category_id) REFERENCES categories(id)" +
                    ")");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Test saveMenuItem successfully saves an item")
    void saveMenuItem_Success() throws SQLException {
        Category drinks = createTestCategory("Напої");
        MenuItem item = new MenuItem(0, "Кола", drinks.getId(),
                25.00, true, false, true);

        item.setCategory(drinks);

        menuItemService.saveMenuItem(item);

        assertTrue(item.getId() > 0,
                "ID елемента меню має бути встановлено після збереження.");

        MenuItem savedItem = menuItemService.getMenuItemById(item.getId());
        assertNotNull(savedItem, "Збережений елемент меню не повинен бути null.");
        assertEquals("Кола", savedItem.getName());
        assertEquals(drinks.getId(), savedItem.getCategoryId());
        assertEquals(drinks.getName(), savedItem.getCategory().getName());
        assertEquals(25.00, savedItem.getPrice());
        assertTrue(savedItem.isVegetarian());
        assertFalse(savedItem.isAllergen());
        assertTrue(savedItem.isGlutenFree());
    }


    @Test
    @DisplayName("Test saveMenuItem throws SQLException for invalid category_id " +
            "(foreign key constraint)")

    void saveMenuItem_InvalidCategoryId_ThrowsSQLException() throws SQLException {
        MenuItem item = new MenuItem(0, "Страва без категорії",
                999, 50.00, false, false, true);

        assertThrows(SQLException.class, () -> {
            menuItemService.saveMenuItem(item);
        }, "Має кинути SQLException через порушення зовнішнього ключа category_id.");
    }


    @Test
    @DisplayName("Test updateMenuItem successfully updates an item")
    void updateMenuItem_Success() throws SQLException {
        Category mainCourses = createTestCategory("Основні страви");
        Category appetizers = createTestCategory("Закуски");

        MenuItem item = new MenuItem(0, "Стара назва", mainCourses.getId(),
                100.0, false, false, true);
        menuItemService.saveMenuItem(item);

        item.setName("Нова смачна страва");
        item.setCategoryId(appetizers.getId());
        item.setPrice(120.50);
        item.setVegetarian(true);
        item.setAllergen(true);
        item.setGlutenFree(false);
        item.setCategory(appetizers);


        menuItemService.updateMenuItem(item);

        MenuItem updatedItem = menuItemService.getMenuItemById(item.getId());
        assertNotNull(updatedItem);
        assertEquals("Нова смачна страва", updatedItem.getName());
        assertEquals(appetizers.getId(), updatedItem.getCategoryId());
        assertEquals(appetizers.getName(), updatedItem.getCategory().getName());
        assertEquals(120.50, updatedItem.getPrice());
        assertTrue(updatedItem.isVegetarian());
        assertTrue(updatedItem.isAllergen());
        assertFalse(updatedItem.isGlutenFree());
    }

    @Test
    @DisplayName("Test updateMenuItem on non-existent item does nothing (no exception)")
    void updateMenuItem_NonExistentItem() throws SQLException {
        Category cat = createTestCategory("Тест");
        MenuItem nonExistentItem = new MenuItem(999, "Неіснуюча",
                cat.getId(), 10.0, false, false, true);
        nonExistentItem.setCategory(cat);

        assertDoesNotThrow(() -> {
            menuItemService.updateMenuItem(nonExistentItem);
        });

        assertNull(menuItemService.getMenuItemById(999));
    }


    @Test
    @DisplayName("Test deleteMenuItem successfully deletes an item")
    void deleteMenuItem_Success() throws SQLException {
        Category desserts = createTestCategory("Десерти");
        MenuItem item = new MenuItem(0, "Торт", desserts.getId(),
                75.0, false, true, false);

        menuItemService.saveMenuItem(item);
        int itemId = item.getId();

        assertNotNull(menuItemService.getMenuItemById(itemId),
                "Елемент має існувати перед видаленням.");

        menuItemService.deleteMenuItem(itemId);

        assertNull(menuItemService.getMenuItemById(itemId),
                "Елемент має бути видалений.");
    }

    @Test
    @DisplayName("Test deleteMenuItem on non-existent item does nothing (no exception)")
    void deleteMenuItem_NonExistentItem() throws SQLException {
        assertDoesNotThrow(() -> {
            menuItemService.deleteMenuItem(999);
        });
    }

    @Test
    @DisplayName("Test getAllMenuItems returns all items")
    void getAllMenuItems_ReturnsAllItems() throws SQLException {
        Category cat1 = createTestCategory("Категорія 1");
        Category cat2 = createTestCategory("Категорія 2");

        MenuItem item1 = new MenuItem(0, "Елемент 1", cat1.getId(),
                10.0, true, false, true);

        item1.setCategory(cat1);
        menuItemService.saveMenuItem(item1);

        MenuItem item2 = new MenuItem(0, "Елемент 2", cat2.getId(),
                20.0, false, true, false);

        item2.setCategory(cat2);
        menuItemService.saveMenuItem(item2);

        List<MenuItem> allItems = menuItemService.getAllMenuItems();
        assertEquals(2, allItems.size(), "Має повернути два елементи меню.");

        assertTrue(allItems.stream().anyMatch(i -> i.getName().equals("Елемент 1")));
        assertTrue(allItems.stream().anyMatch(i -> i.getName().equals("Елемент 2")));

        allItems.forEach(item -> assertNotNull(item.getCategory().getName()));
    }

    @Test
    @DisplayName("Test getAllMenuItems returns empty list when no items")
    void getAllMenuItems_NoItems_ReturnsEmptyList() throws SQLException {
        List<MenuItem> allItems = menuItemService.getAllMenuItems();
        assertTrue(allItems.isEmpty(), "Список має бути порожнім, якщо немає елементів.");
    }

    @Test
    @DisplayName("Test getMenuItemById returns correct item")
    void getMenuItemById_ExistingItem_ReturnsCorrectItem() throws SQLException {
        Category cat = createTestCategory("Сніданки");
        MenuItem item = new MenuItem(0, "Омлет", cat.getId(), 60.0,
                true, false, true);
        item.setCategory(cat);
        menuItemService.saveMenuItem(item);

        MenuItem foundItem = menuItemService.getMenuItemById(item.getId());
        assertNotNull(foundItem);
        assertEquals(item.getId(), foundItem.getId());
        assertEquals("Омлет", foundItem.getName());
        assertEquals(cat.getName(), foundItem.getCategory().getName());
    }

    @Test
    @DisplayName("Test getMenuItemById returns null for non-existent item")
    void getMenuItemById_NonExistentItem_ReturnsNull() throws SQLException {
        MenuItem foundItem = menuItemService.getMenuItemById(999);
        assertNull(foundItem, "Має повернути null для неіснуючого ID.");
    }

    @Test
    @DisplayName("Test filterMenuItems by category")
    void filterMenuItems_ByCategory() throws SQLException {
        Category salads = createTestCategory("Салати");
        Category soups = createTestCategory("Супи");

        menuItemService.saveMenuItem(new MenuItem(0, "Цезар", salads.getId(),
                80.0, false, false, true));

        menuItemService.saveMenuItem(new MenuItem(0, "Грецький", salads.getId(),
                70.0, true, false, true));

        menuItemService.saveMenuItem(new MenuItem(0, "Борщ", soups.getId(),
                50.0, false, true, false));

        List<MenuItem> filtered = menuItemService.filterMenuItems(salads.getId(),
                false, false, false);

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(item -> item.getCategoryId() == salads.getId()));
    }

    @Test
    @DisplayName("Test filterMenuItems for vegetarian only")
    void filterMenuItems_VegetarianOnly() throws SQLException {
        Category main = createTestCategory("Головні страви");
        menuItemService.saveMenuItem(new MenuItem(0, "Стейк", main.getId(),
                200.0, false, false, true));

        menuItemService.saveMenuItem(new MenuItem(0, "Овочеве рагу", main.getId(),
                120.0, true, false, true));

        menuItemService.saveMenuItem(new MenuItem(0, "Паста з грибами", main.getId(),
                150.0, true, true, false));

        List<MenuItem> filtered = menuItemService.filterMenuItems(null,
                true, false, false);

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(MenuItem::isVegetarian));
    }

    @Test
    @DisplayName("Test filterMenuItems for allergen free only")
    void filterMenuItems_AllergenFreeOnly() throws SQLException {
        Category desserts = createTestCategory("Десерти");
        menuItemService.saveMenuItem(new MenuItem(0, "Торт Наполеон",
                desserts.getId(), 90.0, false, true, false));

        menuItemService.saveMenuItem(new MenuItem(0, "Фруктовий салат",
                desserts.getId(), 60.0, true, false, true));

        menuItemService.saveMenuItem(new MenuItem(0, "Морозиво", desserts.getId(),
                50.0, false, false, true));

        List<MenuItem> filtered = menuItemService.filterMenuItems(null,
                false, true, false);

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(item -> !item.isAllergen()));
    }

    @Test
    @DisplayName("Test filterMenuItems for gluten free only")
    void filterMenuItems_GlutenFreeOnly() throws SQLException {
        Category bakery = createTestCategory("Випічка");
        menuItemService.saveMenuItem(new MenuItem(0, "Хліб пшеничний",
                bakery.getId(), 20.0, true, true, false));

        menuItemService.saveMenuItem(new MenuItem(0, "Кукурудзяний хліб",
                bakery.getId(), 25.0, true, false, true));

        menuItemService.saveMenuItem(new MenuItem(0, "Безглютеновий кекс", bakery.getId(),
                40.0, true, false, true));

        List<MenuItem> filtered = menuItemService.filterMenuItems(null,
                false, false, true);

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(MenuItem::isGlutenFree));
    }

    @Test
    @DisplayName("Test filterMenuItems with multiple criteria")
    void filterMenuItems_MultipleCriteria() throws SQLException {
        Category cat1 = createTestCategory("Загальна");

        menuItemService.saveMenuItem(new MenuItem(0, "Супер Здорове",
                cat1.getId(), 100.0, true, false, true));

        menuItemService.saveMenuItem(new MenuItem(0, "Вег з Алергеном"
                , cat1.getId(), 110.0, true, true, true));

        menuItemService.saveMenuItem(new MenuItem(0, "М'ясне",
                cat1.getId(), 120.0, false, false, true));

        menuItemService.saveMenuItem(new MenuItem(0, "Вег з Глютеном",
                cat1.getId(), 130.0, true, false, false));

        List<MenuItem> filtered = menuItemService.filterMenuItems(cat1.getId(),
                true, true, true);

        assertEquals(1, filtered.size());
        assertEquals("Супер Здорове", filtered.get(0).getName());
    }

    @Test
    @DisplayName("Test filterMenuItems returns empty list for no matches")
    void filterMenuItems_NoMatches_ReturnsEmptyList() throws SQLException {
        Category cat = createTestCategory("Спеціальне");
        menuItemService.saveMenuItem(new MenuItem(0, "Звичайна страва",
                cat.getId(), 50.0, false, true, false));

        List<MenuItem> filtered = menuItemService.filterMenuItems(cat.getId(),
                true, false, false);
        assertTrue(filtered.isEmpty());
    }

    @Test
    @DisplayName("Test saveMenuItem throws SQLException when connection is closed")
    void saveMenuItem_ConnectionClosed_ThrowsSQLException() throws SQLException {
        Category cat = createTestCategory("Категорія");

        MenuItem item = new MenuItem(0, "Тест", cat.getId(), 1.0,
                false, false, true);

        connection.close();
        assertThrows(SQLException.class, () -> menuItemService.saveMenuItem(item));
    }

    @Test
    @DisplayName("Test updateMenuItem throws SQLException when connection is closed")
    void updateMenuItem_ConnectionClosed_ThrowsSQLException() throws SQLException {
        Category cat = createTestCategory("Кат");
        MenuItem item = new MenuItem(1, "Тест", cat.getId(), 1.0,
                false, false, true);
        connection.close();
        assertThrows(SQLException.class, () -> menuItemService.updateMenuItem(item));
    }

    @Test
    @DisplayName("Test deleteMenuItem throws SQLException when connection is closed")
    void deleteMenuItem_ConnectionClosed_ThrowsSQLException() throws SQLException {
        connection.close();
        assertThrows(SQLException.class, () -> menuItemService.deleteMenuItem(1));
    }

    @Test
    @DisplayName("Test getAllMenuItems throws SQLException when connection is closed")
    void getAllMenuItems_ConnectionClosed_ThrowsSQLException() throws SQLException {
        connection.close();
        assertThrows(SQLException.class, () -> menuItemService.getAllMenuItems());
    }

    @Test
    @DisplayName("Test getMenuItemById throws SQLException when connection is closed")
    void getMenuItemById_ConnectionClosed_ThrowsSQLException() throws SQLException {
        connection.close();
        assertThrows(SQLException.class, () -> menuItemService.getMenuItemById(1));
    }

    @Test
    @DisplayName("Test filterMenuItems throws SQLException when connection is closed")
    void filterMenuItems_ConnectionClosed_ThrowsSQLException() throws SQLException {
        connection.close();
        assertThrows(SQLException.class, () -> menuItemService.
                filterMenuItems(null, false,
                        false, false));
    }
}