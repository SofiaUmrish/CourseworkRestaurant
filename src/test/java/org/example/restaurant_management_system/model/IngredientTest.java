package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        Ingredient ingredient = new Ingredient();

        assertEquals(0, ingredient.getId(), "ID має бути 0 за замовчуванням.");
        assertNull(ingredient.getName(), "Name має бути null за замовчуванням.");
        assertNull(ingredient.getUnit(), "Unit має бути null за замовчуванням.");
        assertNull(ingredient.getExpirationDate(), "ExpirationDate має бути null за замовчуванням.");
        assertEquals(0.0, ingredient.getCurrentStock(), 0.001,
                "CurrentStock має бути 0.0 за замовчуванням.");


        assertNotNull(ingredient.idProperty());
        assertNotNull(ingredient.nameProperty());
        assertNotNull(ingredient.unitProperty());
        assertNotNull(ingredient.expirationDateProperty());
        assertNotNull(ingredient.currentStockProperty());
    }

    @Test
    @DisplayName("Test constructor with id, name, unit, expirationDate sets properties (stock default 0)")
    void constructorWithIdNameUnitExpDate_SetsProperties_StockDefault() {
        int expectedId = 1;
        String expectedName = "Молоко";
        String expectedUnit = "мл";
        LocalDate expectedExpDate = LocalDate.now().plusDays(7);

        Ingredient ingredient = new Ingredient(expectedId, expectedName, expectedUnit, expectedExpDate);

        assertEquals(expectedId, ingredient.getId());
        assertEquals(expectedName, ingredient.getName());
        assertEquals(expectedUnit, ingredient.getUnit());
        assertEquals(expectedExpDate, ingredient.getExpirationDate());
        assertEquals(0.0, ingredient.getCurrentStock(), 0.001,
                "CurrentStock має бути 0.0 за замовчуванням для цього конструктора.");
    }

    @Test
    @DisplayName("Test constructor with id, name, unit, currentStock, expirationDate sets all properties")
    void constructorWithAllFields_SetsAllProperties() {
        int expectedId = 2;
        String expectedName = "Цукор";
        String expectedUnit = "г";
        double expectedStock = 500.0;
        LocalDate expectedExpDate = LocalDate.now().plusYears(1);

        Ingredient ingredient = new Ingredient(expectedId, expectedName,
                expectedUnit, expectedStock, expectedExpDate);

        assertEquals(expectedId, ingredient.getId());
        assertEquals(expectedName, ingredient.getName());
        assertEquals(expectedUnit, ingredient.getUnit());
        assertEquals(expectedStock, ingredient.getCurrentStock(), 0.001);
        assertEquals(expectedExpDate, ingredient.getExpirationDate());
    }

    @Test
    @DisplayName("Test constructor without ID (name, unit, expirationDate) sets properties and default ID/stock")
    void constructorWithoutId_SetsProperties_DefaultIdStock() {
        String expectedName = "Сіль";
        String expectedUnit = "г";
        LocalDate expectedExpDate = LocalDate.now().plusYears(2);

        Ingredient ingredient = new Ingredient(expectedName, expectedUnit, expectedExpDate);

        assertEquals(0, ingredient.getId(), "ID має бути 0 за замовчуванням.");
        assertEquals(expectedName, ingredient.getName());
        assertEquals(expectedUnit, ingredient.getUnit());
        assertEquals(expectedExpDate, ingredient.getExpirationDate());
        assertEquals(0.0, ingredient.getCurrentStock(), 0.001,
                "CurrentStock має бути 0.0 за замовчуванням.");
    }


    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        Ingredient ingredient = new Ingredient();
        int testId = 123;
        ingredient.setId(testId);
        assertEquals(testId, ingredient.getId());
    }

    @Test
    @DisplayName("Test setName and getName work correctly")
    void setNameAndGetName() {
        Ingredient ingredient = new Ingredient();
        String testName = "Борошно";
        ingredient.setName(testName);
        assertEquals(testName, ingredient.getName());
    }

    @Test
    @DisplayName("Test setUnit and getUnit work correctly")
    void setUnitAndGetUnit() {
        Ingredient ingredient = new Ingredient();
        String testUnit = "кг";
        ingredient.setUnit(testUnit);
        assertEquals(testUnit, ingredient.getUnit());
    }

    @Test
    @DisplayName("Test setExpirationDate and getExpirationDate work correctly")
    void setExpirationDateAndGetExpirationDate() {
        Ingredient ingredient = new Ingredient();
        LocalDate testDate = LocalDate.of(2025, 12, 31);
        ingredient.setExpirationDate(testDate);
        assertEquals(testDate, ingredient.getExpirationDate());
    }

    @Test
    @DisplayName("Test setExpirationDate allows null")
    void setExpirationDate_AllowsNull() {
        Ingredient ingredient = new Ingredient("Тест", "шт", LocalDate.now());
        ingredient.setExpirationDate(null);
        assertNull(ingredient.getExpirationDate(), "ExpirationDate має дозволяти встановлення null.");
    }

    @Test
    @DisplayName("Test setCurrentStock and getCurrentStock work correctly")
    void setCurrentStockAndGetCurrentStock() {
        Ingredient ingredient = new Ingredient();
        double testStock = 125.75;
        ingredient.setCurrentStock(testStock);
        assertEquals(testStock, ingredient.getCurrentStock(), 0.001);
    }

    @Test
    @DisplayName("Test idProperty returns correct IntegerProperty")
    void idProperty_ReturnsCorrectProperty() {
        Ingredient ingredient = new Ingredient();
        IntegerProperty prop = ingredient.idProperty();
        assertNotNull(prop);
        assertEquals(ingredient.getId(), prop.get());

        int newValue = 77;
        prop.set(newValue);
        assertEquals(newValue, ingredient.getId());
    }

    @Test
    @DisplayName("Test nameProperty returns correct StringProperty")
    void nameProperty_ReturnsCorrectProperty() {
        Ingredient ingredient = new Ingredient();
        StringProperty prop = ingredient.nameProperty();
        assertNotNull(prop);
        assertEquals(ingredient.getName(), prop.get());

        String newValue = "Новий Інгредієнт";
        prop.set(newValue);
        assertEquals(newValue, ingredient.getName());
    }

    @Test
    @DisplayName("Test unitProperty returns correct StringProperty")
    void unitProperty_ReturnsCorrectProperty() {
        Ingredient ingredient = new Ingredient();
        StringProperty prop = ingredient.unitProperty();
        assertNotNull(prop);
        assertEquals(ingredient.getUnit(), prop.get());

        String newValue = "л";
        prop.set(newValue);
        assertEquals(newValue, ingredient.getUnit());
    }

    @Test
    @DisplayName("Test expirationDateProperty returns correct ObjectProperty<LocalDate>")
    void expirationDateProperty_ReturnsCorrectProperty() {
        Ingredient ingredient = new Ingredient();
        ObjectProperty<LocalDate> prop = ingredient.expirationDateProperty();
        assertNotNull(prop);
        assertEquals(ingredient.getExpirationDate(), prop.get());

        LocalDate newValue = LocalDate.of(2024, 1, 1);
        prop.set(newValue);
        assertEquals(newValue, ingredient.getExpirationDate());
    }

    @Test
    @DisplayName("Test currentStockProperty returns correct DoubleProperty")
    void currentStockProperty_ReturnsCorrectProperty() {
        Ingredient ingredient = new Ingredient();
        DoubleProperty prop = ingredient.currentStockProperty();
        assertNotNull(prop);
        assertEquals(ingredient.getCurrentStock(), prop.get(), 0.001);

        double newValue = 99.9;
        prop.set(newValue);
        assertEquals(newValue, ingredient.getCurrentStock(), 0.001);
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        Ingredient ingredient1 = new Ingredient(1, "Сіль",
                "г", LocalDate.now().plusYears(1));
        assertTrue(ingredient1.equals(ingredient1));
    }

    @Test
    @DisplayName("Test equals is symmetric")
    void equals_IsSymmetric() {
        LocalDate date = LocalDate.now();
        Ingredient ingredient1 = new Ingredient(1, "Сіль", "г", date);
        Ingredient ingredient2 = new Ingredient(1, "ІншаСіль", "кг",
                100.0, date.plusDays(1));
        assertTrue(ingredient1.equals(ingredient2));
        assertTrue(ingredient2.equals(ingredient1));
    }

    @Test
    @DisplayName("Test equals is transitive")
    void equals_IsTransitive() {
        LocalDate date = LocalDate.now();
        Ingredient ingredient1 = new Ingredient(1, "A", "г", date);
        Ingredient ingredient2 = new Ingredient(1, "B", "кг", date.plusDays(1));
        Ingredient ingredient3 = new Ingredient(1, "C", "шт", date.plusDays(2));
        assertTrue(ingredient1.equals(ingredient2));
        assertTrue(ingredient2.equals(ingredient3));
        assertTrue(ingredient1.equals(ingredient3));
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        LocalDate date = LocalDate.now();
        Ingredient ingredient1 = new Ingredient(1, "Сіль", "г", date);
        Ingredient ingredient2 = new Ingredient(2, "Сіль", "г", date);
        assertFalse(ingredient1.equals(ingredient2));
    }

    @Test
    @DisplayName("Test equals returns false for null object")
    void equals_NullObject_ReturnsFalse() {
        Ingredient ingredient1 = new Ingredient(1, "Сіль", "г", LocalDate.now());
        assertFalse(ingredient1.equals(null));
    }

    @Test
    @DisplayName("Test equals returns false for object of different class")
    void equals_DifferentClass_ReturnsFalse() {
        Ingredient ingredient1 = new Ingredient(1, "Сіль", "г", LocalDate.now());
        Object otherObject = new Object();
        assertFalse(ingredient1.equals(otherObject));
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        Ingredient ingredient1 = new Ingredient(1, "Сіль", "г", LocalDate.now());
        int initialHashCode = ingredient1.hashCode();
        ingredient1.setName("НоваСіль");
        assertEquals(initialHashCode, ingredient1.hashCode());
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void hashCode_ForEqualObjects() {
        LocalDate date = LocalDate.now();
        Ingredient ingredient1 = new Ingredient(1, "Сіль1", "г", date);
        Ingredient ingredient2 = new Ingredient(1, "Сіль2", "кг",
                date.plusDays(1));
        assertTrue(ingredient1.equals(ingredient2));
        assertEquals(ingredient1.hashCode(), ingredient2.hashCode());
    }

    @Test
    @DisplayName("Test hashCode for different objects (different ID)")
    void hashCode_ForDifferentObjects_DifferentId() {
        LocalDate date = LocalDate.now();
        Ingredient ingredient1 = new Ingredient(1, "Сіль", "г", date);
        Ingredient ingredient2 = new Ingredient(2, "Сіль", "г", date);
        assertNotEquals(ingredient1.hashCode(), ingredient2.hashCode());
    }

    @Test
    @DisplayName("Test toString returns ingredient name")
    void toString_ReturnsIngredientName() {
        String expectedName = "Чорний перець";
        Ingredient ingredient = new Ingredient(expectedName, "г",
                LocalDate.now().plusMonths(6));
        assertEquals(expectedName, ingredient.toString());
    }

    @Test
    @DisplayName("Test toString returns null if name is null")
    void toString_ReturnsNullIfNameIsNull() {
        Ingredient ingredient = new Ingredient(); // name буде null
        assertNull(ingredient.toString(), "toString() має " +
                "повертати null, якщо ім'я інгредієнта null.");
    }

    @Test
    @DisplayName("Test getters and setters work together")
    void gettersAndSetters_WorkTogether() {
        Ingredient ingredient = new Ingredient();
        int testId = 300;
        String testName = "Оливкова олія";
        String testUnit = "мл";
        LocalDate testExpDate = LocalDate.of(2026, 1, 15);
        double testStock = 750.5;

        ingredient.setId(testId);
        ingredient.setName(testName);
        ingredient.setUnit(testUnit);
        ingredient.setExpirationDate(testExpDate);
        ingredient.setCurrentStock(testStock);

        assertEquals(testId, ingredient.getId());
        assertEquals(testName, ingredient.getName());
        assertEquals(testUnit, ingredient.getUnit());
        assertEquals(testExpDate, ingredient.getExpirationDate());
        assertEquals(testStock, ingredient.getCurrentStock(), 0.001);
        assertEquals(testName, ingredient.toString());
    }
}