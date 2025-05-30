package org.example.restaurant_management_system.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    @DisplayName("Test no-argument constructor creates an empty category")
    void noArgConstructor_CreatesEmptyCategory() {
        Category category = new Category();
        assertEquals(0, category.getId(), "ID має бути 0 для" +
                " конструктора без аргументів за замовчуванням.");
        assertNull(category.getName(), "Name має бути null для " +
                "конструктора без аргументів за замовчуванням.");
    }

    @Test
    @DisplayName("Test full constructor sets id and name correctly")
    void fullConstructor_SetsIdAndName() {
        int expectedId = 1;
        String expectedName = "Напої";
        Category category = new Category(expectedId, expectedName);

        assertEquals(expectedId, category.getId(), "ID має бути встановлено конструктором.");
        assertEquals(expectedName, category.getName(), "Name має бути встановлено конструктором.");
    }

    @Test
    @DisplayName("Test constructor with name only sets name and default id")
    void nameOnlyConstructor_SetsNameAndDefaultId() {
        String expectedName = "Десерти";
        Category category = new Category(expectedName);

        assertEquals(0, category.getId(), "ID має бути 0 за замовчуванням," +
                " якщо встановлюється тільки name.");
        assertEquals(expectedName, category.getName(), "Name має бути " +
                "встановлено конструктором.");
    }

    @Test
    @DisplayName("Test setId updates the id")
    void setId_UpdatesId() {
        Category category = new Category();
        int newId = 5;
        category.setId(newId);
        assertEquals(newId, category.getId(), "setId має оновити ID.");
    }

    @Test
    @DisplayName("Test setName updates the name")
    void setName_UpdatesName() {
        Category category = new Category();
        String newName = "Закуски";
        category.setName(newName);
        assertEquals(newName, category.getName(), "setName має оновити name.");
    }

    @Test
    @DisplayName("Test setName allows null name")
    void setName_AllowsNullName() {
        Category category = new Category("Початкова назва");
        category.setName(null);
        assertNull(category.getName(), "setName має дозволяти встановлення null для name.");
    }

    @Test
    @DisplayName("Test getName returns the correct name")
    void getName_ReturnsCorrectName() {
        String expectedName = "Основні страви";
        Category category = new Category(expectedName);
        assertEquals(expectedName, category.getName(), "getName має повертати правильне ім'я.");
    }

    @Test
    @DisplayName("Test getId returns the correct id")
    void getId_ReturnsCorrectId() {
        int expectedId = 10;
        Category category = new Category(expectedId, "Тестова категорія");
        assertEquals(expectedId, category.getId(), "getId має повертати правильний ID.");
    }

    @Test
    @DisplayName("Test toString returns the category name")
    void toString_ReturnsCategoryName() {
        String expectedName = "Спеціальні пропозиції";
        Category category = new Category(expectedName);
        assertEquals(expectedName, category.toString(), "toString() має повертати назву категорії.");
    }

    @Test
    @DisplayName("Test toString returns null if name is null")
    void toString_ReturnsNullIfNameIsNull() {
        Category category = new Category(); // name буде null
        assertNull(category.toString(), "toString() має повертати null, якщо ім'я категорії null.");
    }

    @Test
    @DisplayName("Test getters and setters work together")
    void gettersAndSetters_WorkTogether() {
        Category category = new Category();
        int testId = 100;
        String testName = "Комбінований тест";

        category.setId(testId);
        category.setName(testName);

        assertEquals(testId, category.getId(), "ID після setId/getId не співпадає.");
        assertEquals(testName, category.getName(), "Name після setName/getName не співпадає.");
        assertEquals(testName, category.toString(), "toString після setName не співпадає.");
    }
}