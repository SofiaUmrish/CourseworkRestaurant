package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuItemTest {

    private Category mockCategory;
    private Category mockCategory2;

    @BeforeEach
    void setUp() {
        mockCategory = mock(Category.class);
        when(mockCategory.getId()).thenReturn(100);
        when(mockCategory.getName()).thenReturn("Тестова Категорія 1");

        mockCategory2 = mock(Category.class);
        when(mockCategory2.getId()).thenReturn(200);
        when(mockCategory2.getName()).thenReturn("Тестова Категорія 2");
    }

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        MenuItem item = new MenuItem();

        assertEquals(0, item.getId());
        assertNull(item.getName());
        assertEquals(0.0, item.getPrice(), 0.001);
        assertNull(item.getCategory());
        assertEquals(0, item.getCategoryId());
        assertFalse(item.isVegetarian());
        assertFalse(item.isAllergen());
        assertFalse(item.isGlutenFree());

        assertNotNull(item.idProperty());
        assertNotNull(item.nameProperty());
        assertNotNull(item.priceProperty());
        assertNotNull(item.categoryProperty());
        assertNotNull(item.vegetarianProperty());
        assertNotNull(item.allergenProperty());
        assertNotNull(item.glutenFreeProperty());
    }

    @Test
    @DisplayName("Test constructor for loading from DB (with id and categoryId)")
    void constructorForDbLoad_SetsProperties() {
        int id = 1;
        String name = "Піца";
        int categoryId = 5;
        double price = 150.75;
        boolean veg = false;
        boolean allergen = true;
        boolean glutenFree = false;

        MenuItem item = new MenuItem(id, name, categoryId, price, veg, allergen, glutenFree);

        assertEquals(id, item.getId());
        assertEquals(name, item.getName());
        assertEquals(categoryId, item.getCategoryId());
        assertEquals(price, item.getPrice(), 0.001);
        assertEquals(veg, item.isVegetarian());
        assertEquals(allergen, item.isAllergen());
        assertEquals(glutenFree, item.isGlutenFree());
        assertNull(item.getCategory());
    }

    @Test
    @DisplayName("Test constructor for new item (without id, with Category object)")
    void constructorForNewItem_SetsProperties() {
        String name = "Салат Цезар";
        double price = 80.0;
        boolean veg = true;
        boolean allergen = false;
        boolean glutenFree = true;

        MenuItem item = new MenuItem(name, price, mockCategory, veg, allergen, glutenFree);

        assertEquals(0, item.getId());
        assertEquals(name, item.getName());
        assertEquals(price, item.getPrice(), 0.001);
        assertSame(mockCategory, item.getCategory());
        assertEquals(mockCategory.getId(), item.getCategoryId());
        assertEquals(veg, item.isVegetarian());
        assertEquals(allergen, item.isAllergen());
        assertEquals(glutenFree, item.isGlutenFree());
    }

    @Test
    @DisplayName("Test constructor for new item handles null category")
    void constructorForNewItem_HandlesNullCategory() {
        String name = "Страва без категорії";
        double price = 50.0;
        MenuItem item = new MenuItem(name, price, null,
                false, false, false);

        assertNull(item.getCategory());
        assertEquals(0, item.getCategoryId());
    }

    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        MenuItem item = new MenuItem();
        int testId = 123;
        item.setId(testId);
        assertEquals(testId, item.getId());
    }

    @Test
    @DisplayName("Test setName and getName work correctly")
    void setNameAndGetName() {
        MenuItem item = new MenuItem();
        String testName = "Бургер";
        item.setName(testName);
        assertEquals(testName, item.getName());
    }

    @Test
    @DisplayName("Test setPrice and getPrice work correctly")
    void setPriceAndGetPrice() {
        MenuItem item = new MenuItem();
        double testPrice = 99.99;
        item.setPrice(testPrice);
        assertEquals(testPrice, item.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Test setCategoryId and getCategoryId work correctly (direct set)")
    void setCategoryIdAndGetCategoryId_DirectSet() {
        MenuItem item = new MenuItem();
        int testCategoryId = 7;
        item.setCategoryId(testCategoryId);
        assertEquals(testCategoryId, item.getCategoryId());
        assertNull(item.getCategory());
    }

    @Test
    @DisplayName("Test setCategory updates category and categoryId")
    void setCategory_UpdatesCategoryAndCategoryId() {
        MenuItem item = new MenuItem();
        item.setCategory(mockCategory);

        assertSame(mockCategory, item.getCategory());
        assertEquals(mockCategory.getId(), item.getCategoryId(),
                "CategoryId має оновитися при встановленні Category.");
    }

    @Test
    @DisplayName("Test setCategory with null updates category to null " +
            "(categoryId remains unchanged - CURRENT LOGIC)")
    void setCategory_WithNull_UpdatesCategoryToNull_CategoryIdUnchanged() {
        MenuItem item = new MenuItem("Тест", 10.0, mockCategory,
                false, false, false);

        assertEquals(100, item.getCategoryId(),
                "Початковий categoryId має бути 100.");
        assertNotNull(item.getCategory());

        item.setCategory(null);


        assertNull(item.getCategory(), "Category має стати null.");
        assertEquals(100, item.getCategoryId(),
                "CategoryId НЕ має змінюватися, якщо нова Category null " +
                        "(згідно з поточним кодом setCategory).");
    }

    @Test
    @DisplayName("Test setVegetarian and isVegetarian work correctly")
    void setVegetarianAndIsVegetarian() {
        MenuItem item = new MenuItem();
        item.setVegetarian(true);
        assertTrue(item.isVegetarian());
        item.setVegetarian(false);
        assertFalse(item.isVegetarian());
    }

    @Test
    @DisplayName("Test setAllergen and isAllergen work correctly")
    void setAllergenAndIsAllergen() {
        MenuItem item = new MenuItem();
        item.setAllergen(true);
        assertTrue(item.isAllergen());
        item.setAllergen(false);
        assertFalse(item.isAllergen());
    }

    @Test
    @DisplayName("Test setGlutenFree and isGlutenFree work correctly")
    void setGlutenFreeAndIsGlutenFree() {
        MenuItem item = new MenuItem();
        item.setGlutenFree(true);
        assertTrue(item.isGlutenFree());
        item.setGlutenFree(false);
        assertFalse(item.isGlutenFree());
    }

    @Test
    @DisplayName("Test idProperty works")
    void idProperty_Works() {
        MenuItem item = new MenuItem();
        IntegerProperty prop = item.idProperty();
        assertNotNull(prop);
        assertEquals(item.getId(), prop.get());
        prop.set(55);
        assertEquals(55, item.getId());
    }

    @Test
    @DisplayName("Test nameProperty works")
    void nameProperty_Works() {
        MenuItem item = new MenuItem();
        StringProperty prop = item.nameProperty();
        assertNotNull(prop);
        assertEquals(item.getName(), prop.get());
        String newName = "Нова назва через property";
        prop.set(newName);
        assertEquals(newName, item.getName());
    }

    @Test
    @DisplayName("Test priceProperty works")
    void priceProperty_Works() {
        MenuItem item = new MenuItem();
        DoubleProperty prop = item.priceProperty();
        assertNotNull(prop);
        assertEquals(item.getPrice(), prop.get(), 0.001);
        double newPrice = 123.45;
        prop.set(newPrice);
        assertEquals(newPrice, item.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Test categoryProperty updates category BUT NOT categoryId (reflects current MenuItem.java)")
    void categoryProperty_UpdatesCategory_ButNotCategoryId_ReflectsCurrentBehavior() {
        MenuItem item = new MenuItem();

        ObjectProperty<Category> prop = item.categoryProperty();
        assertNotNull(prop);
        assertNull(item.getCategory(), "Initial category should be null.");
        assertEquals(0, item.getCategoryId(), "Initial categoryId should be 0 from " +
                "default constructor.");

        prop.set(mockCategory);

        assertSame(mockCategory, item.getCategory(), "Category object має бути mockCategory.");
        assertEquals(0, item.getCategoryId(), "CategoryId НЕ МАЄ оновитися " +
                "автоматично через categoryProperty, оскільки немає слухача.");
    }

    @Test
    @DisplayName("Test categoryProperty set to null does not change categoryId (reflects current MenuItem.java)")
    void categoryProperty_SetToNull_DoesNotChangeCategoryId_ReflectsCurrentBehavior() {
        MenuItem item = new MenuItem("Тест", 1.0, mockCategory,
                false, false, false);
        assertEquals(100, item.getCategoryId(), "Initial categoryId set by constructor" +
                " should be 100.");

        item.categoryProperty().set(null);

        assertNull(item.getCategory(), "Category object should now be null.");
        assertEquals(100, item.getCategoryId(), "CategoryId НЕ має змінитися, " +
                "якщо categoryProperty встановлено в null (немає слухача).");
    }


    @Test
    @DisplayName("Test vegetarianProperty works")
    void vegetarianProperty_Works() {
        MenuItem item = new MenuItem();
        BooleanProperty prop = item.vegetarianProperty();
        assertNotNull(prop);
        assertEquals(item.isVegetarian(), prop.get());
        prop.set(true);
        assertTrue(item.isVegetarian());
    }

    @Test
    @DisplayName("Test allergenProperty works")
    void allergenProperty_Works() {
        MenuItem item = new MenuItem();
        BooleanProperty prop = item.allergenProperty();
        assertNotNull(prop);
        assertEquals(item.isAllergen(), prop.get());
        prop.set(true);
        assertTrue(item.isAllergen());
    }

    @Test
    @DisplayName("Test glutenFreeProperty works")
    void glutenFreeProperty_Works() {
        MenuItem item = new MenuItem();
        BooleanProperty prop = item.glutenFreeProperty();
        assertNotNull(prop);
        assertEquals(item.isGlutenFree(), prop.get());
        prop.set(true);
        assertTrue(item.isGlutenFree());
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        MenuItem item1 = new MenuItem(1, "A", 1, 1.0,
                false, false, false);

        assertTrue(item1.equals(item1));
    }

    @Test
    @DisplayName("Test equals is symmetric")
    void equals_IsSymmetric() {
        MenuItem item1 = new MenuItem(1, "A", 1, 1.0,
                false, false, false);

        MenuItem item2 = new MenuItem(1, "B", 2, 2.0,
                true, true, true);

        assertTrue(item1.equals(item2));
        assertTrue(item2.equals(item1));
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        MenuItem item1 = new MenuItem(1, "A", 1, 1.0,
                false, false, false);

        MenuItem item2 = new MenuItem(2, "A", 1, 1.0,
                false, false, false);

        assertFalse(item1.equals(item2));
    }

    @Test
    @DisplayName("Test equals returns false for null")
    void equals_Null_ReturnsFalse() {
        MenuItem item1 = new MenuItem(1, "A", 1,
                1.0, false, false, false);

        assertFalse(item1.equals(null));
    }

    @Test
    @DisplayName("Test equals returns false for different class")
    void equals_DifferentClass_ReturnsFalse() {
        MenuItem item1 = new MenuItem(1, "A", 1,
                1.0, false, false, false);

        assertFalse(item1.equals(new Object()));
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        MenuItem item1 = new MenuItem(1, "A", 1,
                1.0, false, false, false);

        int initialHashCode = item1.hashCode();
        item1.setName("New Name");
        assertEquals(initialHashCode, item1.hashCode());
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void hashCode_ForEqualObjects() {
        MenuItem item1 = new MenuItem(1, "A", 1, 1.0,
                false, false, false);

        MenuItem item2 = new MenuItem(1, "B", 2, 2.0,
                true, true, true);

        assertTrue(item1.equals(item2));
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    @DisplayName("Test toString returns item name")
    void toString_ReturnsItemName() {
        String expectedName = "Кава";
        MenuItem item = new MenuItem(expectedName, 25.0, mockCategory,
                false, false, true);

        assertEquals(expectedName, item.toString());
    }

    @Test
    @DisplayName("Test toString returns null if name is null")
    void toString_ReturnsNullIfNameIsNull() {
        MenuItem item = new MenuItem();
        assertNull(item.toString());
    }

    @Test
    @DisplayName("Test getters and setters work together")
    void gettersAndSetters_WorkTogether() {
        MenuItem item = new MenuItem();
        int testId = 77;
        String testName = "Чай";
        double testPrice = 30.5;
        boolean veg = true;
        boolean allergen = false;
        boolean gluten = true;

        item.setId(testId);
        item.setName(testName);
        item.setPrice(testPrice);
        item.setCategory(mockCategory2);
        item.setVegetarian(veg);
        item.setAllergen(allergen);
        item.setGlutenFree(gluten);

        assertEquals(testId, item.getId());
        assertEquals(testName, item.getName());
        assertEquals(testPrice, item.getPrice(), 0.001);
        assertSame(mockCategory2, item.getCategory());
        assertEquals(mockCategory2.getId(), item.getCategoryId());
        assertEquals(veg, item.isVegetarian());
        assertEquals(allergen, item.isAllergen());
        assertEquals(gluten, item.isGlutenFree());
        assertEquals(testName, item.toString());
    }
}