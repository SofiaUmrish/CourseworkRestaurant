package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuItemIngredientTest {

    private MenuItem mockMenuItem;
    private Ingredient mockIngredient;

    @BeforeEach
    void setUp() {
        mockMenuItem = mock(MenuItem.class);
        when(mockMenuItem.getId()).thenReturn(10);
        when(mockMenuItem.getName()).thenReturn("Тестова Страва");

        mockIngredient = mock(Ingredient.class);
        when(mockIngredient.getId()).thenReturn(20);
        when(mockIngredient.getName()).thenReturn("Тестовий Інгредієнт");
    }

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        MenuItemIngredient mii = new MenuItemIngredient();

        assertEquals(0, mii.getId());
        assertEquals(0, mii.getMenuItemId());
        assertNull(mii.getMenuItem());
        assertEquals(0, mii.getIngredientId());
        assertNull(mii.getIngredient());
        assertEquals(0.0, mii.getQuantityPerUnit(), 0.001);

        assertNotNull(mii.idProperty());
        assertNotNull(mii.menuItemIdProperty());
        assertNotNull(mii.menuItemProperty());
        assertNotNull(mii.ingredientIdProperty());
        assertNotNull(mii.ingredientProperty());
        assertNotNull(mii.quantityPerUnitProperty());
    }

    @Test
    @DisplayName("Test constructor for DB load (with IDs)")
    void constructorForDbLoad_WithIds_SetsProperties() {
        int id = 1;
        int menuItemId = 11;
        int ingredientId = 22;
        double quantity = 50.5;

        MenuItemIngredient mii = new MenuItemIngredient(id, menuItemId, ingredientId, quantity);

        assertEquals(id, mii.getId());
        assertEquals(menuItemId, mii.getMenuItemId());
        assertNull(mii.getMenuItem(), "MenuItem object має бути null для цього конструктора.");
        assertEquals(ingredientId, mii.getIngredientId());
        assertNull(mii.getIngredient(), "Ingredient object має бути null для цього конструктора.");
        assertEquals(quantity, mii.getQuantityPerUnit(), 0.001);
    }

    @Test
    @DisplayName("Test constructor with full objects and ID")
    void constructorWithFullObjectsAndId_SetsProperties() {
        int id = 2;
        double quantity = 100.0;

        MenuItemIngredient mii = new MenuItemIngredient(id, mockMenuItem, mockIngredient, quantity);

        assertEquals(id, mii.getId());
        assertSame(mockMenuItem, mii.getMenuItem());
        assertEquals(mockMenuItem.getId(), mii.getMenuItemId());
        assertSame(mockIngredient, mii.getIngredient());
        assertEquals(mockIngredient.getId(), mii.getIngredientId());
        assertEquals(quantity, mii.getQuantityPerUnit(), 0.001);
    }

    @Test
    @DisplayName("Test constructor with full objects and ID handles null menuItem")
    void constructorWithFullObjectsAndId_HandlesNullMenuItem() {
        int id = 3;
        double quantity = 10.0;
        MenuItemIngredient mii = new MenuItemIngredient(id, null, mockIngredient, quantity);

        assertNull(mii.getMenuItem());
        assertEquals(0, mii.getMenuItemId(), "MenuItemId має бути 0, якщо MenuItem null.");
        assertSame(mockIngredient, mii.getIngredient());
        assertEquals(mockIngredient.getId(), mii.getIngredientId());
    }

    @Test
    @DisplayName("Test constructor with full objects and ID handles null ingredient")
    void constructorWithFullObjectsAndId_HandlesNullIngredient() {
        int id = 4;
        double quantity = 15.0;
        MenuItemIngredient mii = new MenuItemIngredient(id, mockMenuItem, null, quantity);

        assertSame(mockMenuItem, mii.getMenuItem());
        assertEquals(mockMenuItem.getId(), mii.getMenuItemId());
        assertNull(mii.getIngredient());
        assertEquals(0, mii.getIngredientId(),
                "IngredientId має бути 0, якщо Ingredient null.");
    }

    @Test
    @DisplayName("Test constructor for new record (without ID, with objects)")
    void constructorForNewRecord_WithObjects_SetsProperties() {
        double quantity = 2.5;

        MenuItemIngredient mii = new MenuItemIngredient(mockMenuItem, mockIngredient, quantity);

        assertEquals(0, mii.getId(), "ID має бути 0 для нового запису.");
        assertSame(mockMenuItem, mii.getMenuItem());
        assertEquals(mockMenuItem.getId(), mii.getMenuItemId());
        assertSame(mockIngredient, mii.getIngredient());
        assertEquals(mockIngredient.getId(), mii.getIngredientId());
        assertEquals(quantity, mii.getQuantityPerUnit(), 0.001);
    }

    @Test
    @DisplayName("Test constructor for new record handles null menuItem")
    void constructorForNewRecord_HandlesNullMenuItem() {
        double quantity = 5.0;
        MenuItemIngredient mii = new MenuItemIngredient(null, mockIngredient, quantity);
        assertNull(mii.getMenuItem());
        assertEquals(0, mii.getMenuItemId());
    }

    @Test
    @DisplayName("Test constructor for new record handles null ingredient")
    void constructorForNewRecord_HandlesNullIngredient() {
        double quantity = 7.5;
        MenuItemIngredient mii = new MenuItemIngredient(mockMenuItem, null, quantity);
        assertNull(mii.getIngredient());
        assertEquals(0, mii.getIngredientId());
    }

    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        MenuItemIngredient mii = new MenuItemIngredient();
        int testId = 101;
        mii.setId(testId);
        assertEquals(testId, mii.getId());
    }

    @Test
    @DisplayName("Test setMenuItemId and getMenuItemId (direct set)")
    void setMenuItemIdAndGetMenuItemId() {
        MenuItemIngredient mii = new MenuItemIngredient();
        int testMenuItemId = 55;
        mii.setMenuItemId(testMenuItemId);
        assertEquals(testMenuItemId, mii.getMenuItemId());
        assertNull(mii.getMenuItem(),
                "MenuItem object має бути null при прямому встановленні menuItemId.");
    }

    @Test
    @DisplayName("Test setMenuItem updates menuItem and menuItemId")
    void setMenuItem_UpdatesMenuItemAndId() {
        MenuItemIngredient mii = new MenuItemIngredient();
        mii.setMenuItem(mockMenuItem);

        assertSame(mockMenuItem, mii.getMenuItem());
        assertEquals(mockMenuItem.getId(), mii.getMenuItemId());
    }

    @Test
    @DisplayName("Test setMenuItem with null updates menuItem to null (menuItemId remains unchanged)")
    void setMenuItem_WithNull_UpdatesMenuItemToNull_MenuItemIdUnchanged() {
        MenuItemIngredient mii = new MenuItemIngredient(1, mockMenuItem, mockIngredient, 1.0);
        assertEquals(10, mii.getMenuItemId());

        mii.setMenuItem(null);

        assertNull(mii.getMenuItem());
        assertEquals(10, mii.getMenuItemId(),
                "MenuItemId НЕ має змінюватися, якщо новий MenuItem null.");
    }


    @Test
    @DisplayName("Test setIngredientId and getIngredientId (direct set)")
    void setIngredientIdAndGetIngredientId() {
        MenuItemIngredient mii = new MenuItemIngredient();
        int testIngredientId = 66;
        mii.setIngredientId(testIngredientId);
        assertEquals(testIngredientId, mii.getIngredientId());
        assertNull(mii.getIngredient());
    }

    @Test
    @DisplayName("Test setIngredient updates ingredient and ingredientId")
    void setIngredient_UpdatesIngredientAndId() {
        MenuItemIngredient mii = new MenuItemIngredient();
        mii.setIngredient(mockIngredient);

        assertSame(mockIngredient, mii.getIngredient());
        assertEquals(mockIngredient.getId(), mii.getIngredientId());
    }

    @Test
    @DisplayName("Test setIngredient with null updates ingredient to null (ingredientId remains unchanged)")
    void setIngredient_WithNull_UpdatesIngredientToNull_IngredientIdUnchanged() {
        MenuItemIngredient mii = new MenuItemIngredient(1,
                mockMenuItem, mockIngredient, 1.0);
        assertEquals(20, mii.getIngredientId());

        mii.setIngredient(null);

        assertNull(mii.getIngredient());
        assertEquals(20, mii.getIngredientId(),
                "IngredientId НЕ має змінюватися, якщо новий Ingredient null.");
    }


    @Test
    @DisplayName("Test setQuantityPerUnit and getQuantityPerUnit work correctly")
    void setQuantityPerUnitAndGetQuantityPerUnit() {
        MenuItemIngredient mii = new MenuItemIngredient();
        double testQuantity = 150.25;
        mii.setQuantityPerUnit(testQuantity);
        assertEquals(testQuantity, mii.getQuantityPerUnit(), 0.001);
    }

    @Test
    @DisplayName("Test idProperty works")
    void idProperty_Works() {
        MenuItemIngredient mii = new MenuItemIngredient();
        IntegerProperty prop = mii.idProperty();
        assertNotNull(prop);
        assertEquals(mii.getId(), prop.get());
        prop.set(77);
        assertEquals(77, mii.getId());
    }

    @Test
    @DisplayName("Test menuItemIdProperty works")
    void menuItemIdProperty_Works() {
        MenuItemIngredient mii = new MenuItemIngredient();
        IntegerProperty prop = mii.menuItemIdProperty();
        assertNotNull(prop);
        assertEquals(mii.getMenuItemId(), prop.get());
        prop.set(88);
        assertEquals(88, mii.getMenuItemId());
    }

    @Test
    @DisplayName("Test menuItemProperty updates menuItem and menuItemId (CURRENT BEHAVIOR)")
    void menuItemProperty_UpdatesMenuItem_ButNotMenuItemId_CurrentBehavior() {
        MenuItemIngredient mii = new MenuItemIngredient();
        assertEquals(0, mii.getMenuItemId());

        mii.menuItemProperty().set(mockMenuItem);

        assertSame(mockMenuItem, mii.getMenuItem());
        assertEquals(0, mii.getMenuItemId(), "MenuItemId НЕ має оновитися автоматично через menuItemProperty для поточного коду.");

    }

    @Test
    @DisplayName("Test menuItemProperty set to null does not change menuItemId (CURRENT BEHAVIOR)")
    void menuItemProperty_SetToNull_DoesNotChangeMenuItemId_CurrentBehavior() {
        MenuItemIngredient mii = new MenuItemIngredient(1, mockMenuItem, mockIngredient, 1.0);
        assertEquals(10, mii.getMenuItemId());

        mii.menuItemProperty().set(null);

        assertNull(mii.getMenuItem());
        assertEquals(10, mii.getMenuItemId(),
                "MenuItemId НЕ має змінитися, якщо menuItemProperty встановлено в null.");
    }

    @Test
    @DisplayName("Test ingredientIdProperty works")
    void ingredientIdProperty_Works() {
        MenuItemIngredient mii = new MenuItemIngredient();
        IntegerProperty prop = mii.ingredientIdProperty();
        assertNotNull(prop);
        assertEquals(mii.getIngredientId(), prop.get());
        prop.set(99);
        assertEquals(99, mii.getIngredientId());
    }

    @Test
    @DisplayName("Test ingredientProperty updates ingredient BUT NOT ingredientId (CURRENT BEHAVIOR)")
    void ingredientProperty_UpdatesIngredient_ButNotIngredientId_CurrentBehavior() {
        MenuItemIngredient mii = new MenuItemIngredient();
        assertEquals(0, mii.getIngredientId());

        mii.ingredientProperty().set(mockIngredient);

        assertSame(mockIngredient, mii.getIngredient());
        assertEquals(0, mii.getIngredientId(),
                "IngredientId НЕ має оновитися автоматично через ingredientProperty для поточного коду.");
    }

    @Test
    @DisplayName("Test ingredientProperty set to null does not change ingredientId (CURRENT BEHAVIOR)")
    void ingredientProperty_SetToNull_DoesNotChangeIngredientId_CurrentBehavior() {
        MenuItemIngredient mii = new MenuItemIngredient(1, mockMenuItem, mockIngredient, 1.0);
        assertEquals(20, mii.getIngredientId());

        mii.ingredientProperty().set(null);

        assertNull(mii.getIngredient());
        assertEquals(20, mii.getIngredientId(),
                "IngredientId НЕ має змінитися, якщо ingredientProperty встановлено в null.");
    }


    @Test
    @DisplayName("Test quantityPerUnitProperty works")
    void quantityPerUnitProperty_Works() {
        MenuItemIngredient mii = new MenuItemIngredient();
        DoubleProperty prop = mii.quantityPerUnitProperty();
        assertNotNull(prop);
        assertEquals(mii.getQuantityPerUnit(), prop.get(), 0.001);
        prop.set(12.34);
        assertEquals(12.34, mii.getQuantityPerUnit(), 0.001);
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        MenuItemIngredient mii1 = new MenuItemIngredient(1,
                10, 20, 1.0);
        assertTrue(mii1.equals(mii1));
    }

    @Test
    @DisplayName("Test equals is symmetric")
    void equals_IsSymmetric() {
        MenuItemIngredient mii1 = new MenuItemIngredient(1,
                10, 20, 1.0);

        MenuItemIngredient mii2 = new MenuItemIngredient(1,
                mockMenuItem, mockIngredient, 2.0);
        assertTrue(mii1.equals(mii2));
        assertTrue(mii2.equals(mii1));
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        MenuItemIngredient mii1 = new MenuItemIngredient(1,
                10, 20, 1.0);

        MenuItemIngredient mii2 = new MenuItemIngredient(2,
                10, 20, 1.0);
        assertFalse(mii1.equals(mii2));
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        MenuItemIngredient mii1 = new MenuItemIngredient(1,
                10, 20, 1.0);

        int initialHashCode = mii1.hashCode();
        mii1.setQuantityPerUnit(5.0);
        assertEquals(initialHashCode, mii1.hashCode());
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void hashCode_ForEqualObjects() {
        MenuItemIngredient mii1 = new MenuItemIngredient(1,
                10, 20, 1.0);

        MenuItemIngredient mii2 = new MenuItemIngredient(1,
                11, 22, 2.0);

        assertTrue(mii1.equals(mii2));
        assertEquals(mii1.hashCode(), mii2.hashCode());
    }
}