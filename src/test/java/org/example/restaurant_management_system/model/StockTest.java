package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockTest {

    private Ingredient mockIngredient;

    @BeforeEach
    void setUp() {
        mockIngredient = mock(Ingredient.class);
        when(mockIngredient.getId()).thenReturn(300);
        when(mockIngredient.getName()).thenReturn("Тестовий Інгредієнт для Складу");
    }

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        Stock stock = new Stock();

        assertEquals(0, stock.getId());
        assertEquals(0, stock.getIngredientId());
        assertNull(stock.getIngredient());
        assertEquals(0.0, stock.getChangeAmount(), 0.001);
        assertNull(stock.getMovementType());
        assertNull(stock.getMovementTime());

        assertNotNull(stock.idProperty());
    }

    @Test
    @DisplayName("Test constructor for DB load (with id and ingredientId)")
    void constructorForDbLoad_WithIds_SetsProperties() {
        int id = 1;
        int ingredientId = 301;
        double changeAmount = 10.5;
        String movementType = "income";
        LocalDateTime movementTime = LocalDateTime.now().minusDays(1);

        Stock stock = new Stock(id, ingredientId, changeAmount, movementType, movementTime);

        assertEquals(id, stock.getId());
        assertEquals(ingredientId, stock.getIngredientId());
        assertNull(stock.getIngredient(), "Ingredient object має бути null для цього конструктора.");
        assertEquals(changeAmount, stock.getChangeAmount(), 0.001);
        assertEquals(movementType, stock.getMovementType());
        assertEquals(movementTime, stock.getMovementTime());
    }

    @Test
    @DisplayName("Test constructor for new record (without ID, with Ingredient object)")
    void constructorForNewRecord_WithIngredientObject_SetsProperties() {

        double changeAmount = -5.0;
        String movementType = "expense";

        Stock stock = new Stock(mockIngredient, changeAmount, movementType);
        LocalDateTime timeBefore = LocalDateTime.now().minusSeconds(1);
        LocalDateTime timeAfter = LocalDateTime.now().plusSeconds(1);


        assertEquals(0, stock.getId(), "ID має бути 0 для нового запису.");
        assertSame(mockIngredient, stock.getIngredient());
        assertEquals(mockIngredient.getId(), stock.getIngredientId());
        assertEquals(changeAmount, stock.getChangeAmount(), 0.001);
        assertEquals(movementType, stock.getMovementType());
        assertNotNull(stock.getMovementTime());
        assertTrue(stock.getMovementTime().isAfter(timeBefore) &&
                        stock.getMovementTime().isBefore(timeAfter),
                "MovementTime має бути близьким до поточного часу.");
    }

    @Test
    @DisplayName("Test constructor for new record handles null Ingredient")
    void constructorForNewRecord_HandlesNullIngredient() {
        double changeAmount = 20.0;
        String movementType = "income";
        Stock stock = new Stock(null, changeAmount, movementType);

        assertNull(stock.getIngredient());
        assertEquals(0, stock.getIngredientId(),
                "IngredientId має бути 0, якщо Ingredient null.");
    }

    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        Stock stock = new Stock();
        int testId = 101;
        stock.setId(testId);
        assertEquals(testId, stock.getId());
    }

    @Test
    @DisplayName("Test setIngredientId and getIngredientId (direct set)")
    void setIngredientIdAndGetIngredientId() {
        Stock stock = new Stock();
        int testIngredientId = 333;
        stock.setIngredientId(testIngredientId);
        assertEquals(testIngredientId, stock.getIngredientId());
        assertNull(stock.getIngredient(), "Ingredient object має бути null " +
                "при прямому встановленні ingredientId.");
    }

    @Test
    @DisplayName("Test setIngredient updates ingredient and ingredientId")
    void setIngredient_UpdatesIngredientAndId() {
        Stock stock = new Stock();
        stock.setIngredient(mockIngredient);

        assertSame(mockIngredient, stock.getIngredient());
        assertEquals(mockIngredient.getId(), stock.getIngredientId());
    }

    @Test
    @DisplayName("Test setIngredient with null updates ingredient to null " +
            "(ingredientId remains unchanged - CURRENT LOGIC)")
    void setIngredient_WithNull_UpdatesIngredientToNull_IngredientIdUnchanged() {
        Stock stock = new Stock(mockIngredient, 10.0, "income");
        assertEquals(mockIngredient.getId(), stock.getIngredientId());

        stock.setIngredient(null);

        assertNull(stock.getIngredient());
        assertEquals(mockIngredient.getId(), stock.getIngredientId(),
                "IngredientId НЕ має змінюватися, якщо новий Ingredient null.");
    }

    @Test
    @DisplayName("Test setChangeAmount and getChangeAmount work correctly")
    void setChangeAmountAndGetChangeAmount() {
        Stock stock = new Stock();
        double testAmount = -2.5;
        stock.setChangeAmount(testAmount);
        assertEquals(testAmount, stock.getChangeAmount(), 0.001);
    }

    @Test
    @DisplayName("Test setMovementType and getMovementType work correctly")
    void setMovementTypeAndGetMovementType() {
        Stock stock = new Stock();
        String type = "spoilage";
        stock.setMovementType(type);
        assertEquals(type, stock.getMovementType());
    }

    @Test
    @DisplayName("Test setMovementTime and getMovementTime work correctly")
    void setMovementTimeAndGetMovementTime() {
        Stock stock = new Stock();
        LocalDateTime time = LocalDateTime.of(2024, 1,
                1, 10, 0, 0);
        stock.setMovementTime(time);
        assertEquals(time, stock.getMovementTime());
    }

    @Test
    @DisplayName("Test setMovementTime allows null")
    void setMovementTime_AllowsNull() {
        Stock stock = new Stock(mockIngredient, 1.0,
                "test");
        assertNotNull(stock.getMovementTime());
        stock.setMovementTime(null);
        assertNull(stock.getMovementTime());
    }

    @Test
    @DisplayName("Test idProperty works")
    void idProperty_Works() {
        Stock stock = new Stock();
        IntegerProperty prop = stock.idProperty();
        assertNotNull(prop);
        assertEquals(stock.getId(), prop.get());
        prop.set(888);
        assertEquals(888, stock.getId());
    }

    @Test
    @DisplayName("Test ingredientIdProperty works")
    void ingredientIdProperty_Works() {
        Stock stock = new Stock();
        IntegerProperty prop = stock.ingredientIdProperty();
        assertNotNull(prop);
        assertEquals(stock.getIngredientId(), prop.get());
        prop.set(777);
        assertEquals(777, stock.getIngredientId());
    }

    @Test
    @DisplayName("Test ingredientProperty updates ingredient BUT NOT ingredientId (CURRENT BEHAVIOR)")
    void ingredientProperty_UpdatesIngredient_ButNotIngredientId_CurrentBehavior() {
        Stock stock = new Stock();
        stock.ingredientProperty().set(mockIngredient);

        assertSame(mockIngredient, stock.getIngredient());
        assertEquals(0, stock.getIngredientId(),
                "IngredientId НЕ має оновитися автоматично через ingredientProperty.");
    }

    @Test
    @DisplayName("Test ingredientProperty set to null does not" +
            " change ingredientId (CURRENT BEHAVIOR)")

    void ingredientProperty_SetToNull_DoesNotChangeIngredientId_CurrentBehavior() {
        Stock stock = new Stock(mockIngredient, 10.0, "type");
        assertEquals(mockIngredient.getId(), stock.getIngredientId());

        stock.ingredientProperty().set(null);

        assertNull(stock.getIngredient());
        assertEquals(mockIngredient.getId(), stock.getIngredientId(),
                "IngredientId НЕ має змінитися, якщо ingredientProperty встановлено в null.");
    }

    @Test
    @DisplayName("Test changeAmountProperty works")
    void changeAmountProperty_Works() {
        Stock stock = new Stock();
        DoubleProperty prop = stock.changeAmountProperty();
        assertNotNull(prop);
        assertEquals(stock.getChangeAmount(), prop.get(), 0.001);
        prop.set(123.45);
        assertEquals(123.45, stock.getChangeAmount(), 0.001);
    }

    @Test
    @DisplayName("Test movementTypeProperty works")
    void movementTypeProperty_Works() {
        Stock stock = new Stock();
        StringProperty prop = stock.movementTypeProperty();
        assertNotNull(prop);
        assertEquals(stock.getMovementType(), prop.get());
        prop.set("adjustment");
        assertEquals("adjustment", stock.getMovementType());
    }

    @Test
    @DisplayName("Test movementTimeProperty works")
    void movementTimeProperty_Works() {
        Stock stock = new Stock();
        ObjectProperty<LocalDateTime> prop = stock.movementTimeProperty();
        assertNotNull(prop);
        assertEquals(stock.getMovementTime(), prop.get());
        LocalDateTime newTime = LocalDateTime.now().minusHours(5);
        prop.set(newTime);
        assertEquals(newTime, stock.getMovementTime());
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        Stock stock1 = new Stock(1, 301, 10.0,
                "income", LocalDateTime.now());

        assertTrue(stock1.equals(stock1));
    }

    @Test
    @DisplayName("Test equals is symmetric for same ID")
    void equals_IsSymmetric_SameId() {
        LocalDateTime time = LocalDateTime.now();
        Stock stock1 = new Stock(1, 301, 10.0, "income", time);

        Stock stock2 = new Stock(1,
                302,
                -5.0,
                "expense",
                time.minusHours(1));

        assertTrue(stock1.equals(stock2),
                "Об'єкти Stock з однаковим ID мають бути рівними.");

        assertTrue(stock2.equals(stock1),
                "Рівність має бути симетричною.");
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        LocalDateTime time = LocalDateTime.now();
        Stock stock1 = new Stock(1, 301, 10.0,
                "income", time);

        Stock stock2 = new Stock(2, 301, 10.0,
                "income", time);

        assertFalse(stock1.equals(stock2));
    }

    @Test
    @DisplayName("Test equals handles objects with ID 0 (CURRENT Stock.equals BEHAVIOR)")
    void equals_ObjectsWithIdZero_CurrentBehavior() {
        Stock stock1 = new Stock();
        Stock stock2 = new Stock();
        Stock stock3 = stock1;

        assertTrue(stock1.equals(stock2), "Два різних екземпляри з ID=0 " +
                "будуть рівними через поточну логіку equals.");

        assertTrue(stock1.equals(stock3));
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        Stock stock1 = new Stock(1, 301, 10.0,
                "income", LocalDateTime.now());

        int initialHashCode = stock1.hashCode();
        stock1.setMovementType("adjustment_plus");
        assertEquals(initialHashCode, stock1.hashCode());
    }
}