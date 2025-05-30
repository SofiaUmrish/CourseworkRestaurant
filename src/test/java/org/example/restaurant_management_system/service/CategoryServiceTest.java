package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest {

    private Connection connection;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        categoryService = new CategoryService(connection);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE categories (id INT AUTO_INCREMENT PRIMARY KEY," +
                    " name VARCHAR(255) NOT NULL)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE categories");
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void addCategory() throws SQLException {
        Category category = new Category("Drinks");
        categoryService.addCategory(category);

        assertNotNull(category.getId());
        assertTrue(category.getId() > 0);

        Category retrievedCategory = categoryService.getCategoryById(category.getId());
        assertNotNull(retrievedCategory);
        assertEquals("Drinks", retrievedCategory.getName());
    }

    @Test
    void addCategory_nullNameThrowsException() {
        Category category = new Category(null);
        assertThrows(SQLException.class, () -> categoryService.addCategory(category));
    }

    @Test
    void updateCategory() throws SQLException {

        Category category = new Category("Starters");
        categoryService.addCategory(category);
        assertNotNull(category.getId());

        category.setName("Appetizers");
        categoryService.updateCategory(category);

        Category updatedCategory = categoryService.getCategoryById(category.getId());
        assertNotNull(updatedCategory);
        assertEquals("Appetizers", updatedCategory.getName());
        assertEquals(category.getId(), updatedCategory.getId());
    }

    @Test
    void updateCategory_nonExistentId() throws SQLException {
        Category category = new Category(999, "NonExistent");
        categoryService.updateCategory(category);
        assertNull(categoryService.getCategoryById(999));
    }

    @Test
    void deleteCategory() throws SQLException {

        Category category = new Category("Desserts");
        categoryService.addCategory(category);
        assertNotNull(category.getId());

        assertNotNull(categoryService.getCategoryById(category.getId()));

        categoryService.deleteCategory(category.getId());

        assertNull(categoryService.getCategoryById(category.getId()));
    }

    @Test
    void deleteCategory_nonExistentId()  {
        assertDoesNotThrow(() -> categoryService.deleteCategory(999));
    }

    @Test
    void getAllCategories() throws SQLException {

        List<Category> categories = categoryService.getAllCategories();
        assertTrue(categories.isEmpty());

        categoryService.addCategory(new Category("Main Courses"));
        categoryService.addCategory(new Category("Sides"));
        categoryService.addCategory(new Category("Soups"));

        categories = categoryService.getAllCategories();
        assertNotNull(categories);
        assertEquals(3, categories.size());

        assertTrue(categories.stream().anyMatch(c -> "Main Courses".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Sides".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Soups".equals(c.getName())));
    }

    @Test
    void getCategoryById() throws SQLException {
        Category category1 = new Category("Breakfast");
        categoryService.addCategory(category1);

        Category category2 = new Category("Lunch");
        categoryService.addCategory(category2);

        Category retrievedCategory1 = categoryService.getCategoryById(category1.getId());
        assertNotNull(retrievedCategory1);
        assertEquals(category1.getId(), retrievedCategory1.getId());
        assertEquals("Breakfast", retrievedCategory1.getName());

        Category retrievedCategory2 = categoryService.getCategoryById(category2.getId());
        assertNotNull(retrievedCategory2);
        assertEquals(category2.getId(), retrievedCategory2.getId());
        assertEquals("Lunch", retrievedCategory2.getName());
    }

    @Test
    void getCategoryById_nonExistentId() throws SQLException {
        Category retrievedCategory = categoryService.getCategoryById(999);
        assertNull(retrievedCategory);
    }
}