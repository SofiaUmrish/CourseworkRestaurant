package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalaryTest {

    private Position mockPosition;

    @BeforeEach
    void setUp() {
        mockPosition = mock(Position.class);
        when(mockPosition.getId()).thenReturn(50);
        when(mockPosition.getName()).thenReturn("Тестова Посада для ЗП");
    }

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        Salary salary = new Salary();

        assertEquals(0, salary.getId());
        assertEquals(0, salary.getPositionId());
        assertNull(salary.getPosition());
        assertEquals(0.0, salary.getMonthlySalary(), 0.001);

        assertNotNull(salary.idProperty());
        assertNotNull(salary.positionIdProperty());
        assertNotNull(salary.positionProperty());
        assertNotNull(salary.monthlySalaryProperty());
    }

    @Test
    @DisplayName("Test constructor for DB load (with id and positionId)")
    void constructorForDbLoad_WithIds_SetsProperties() {
        int id = 1;
        int positionId = 55;
        double monthlySalary = 25000.0;

        Salary salary = new Salary(id, positionId, monthlySalary);

        assertEquals(id, salary.getId());
        assertEquals(positionId, salary.getPositionId());
        assertNull(salary.getPosition(), "Position object має бути null" +
                " для цього конструктора.");

        assertEquals(monthlySalary, salary.getMonthlySalary(), 0.001);
    }

    @Test
    @DisplayName("Test constructor with Position object and ID")
    void constructorWithPositionObjectAndId_SetsProperties() {
        int id = 2;
        double monthlySalary = 30000.0;

        Salary salary = new Salary(id, mockPosition, monthlySalary);

        assertEquals(id, salary.getId());
        assertSame(mockPosition, salary.getPosition());
        assertEquals(mockPosition.getId(), salary.getPositionId());
        assertEquals(monthlySalary, salary.getMonthlySalary(), 0.001);
    }

    @Test
    @DisplayName("Test constructor with Position object and ID handles null Position")
    void constructorWithPositionObjectAndId_HandlesNullPosition() {
        int id = 3;
        double monthlySalary = 15000.0;
        Salary salary = new Salary(id, null, monthlySalary);

        assertNull(salary.getPosition());
        assertEquals(0, salary.getPositionId(), "PositionId має бути 0," +
                " якщо Position null.");
        assertEquals(monthlySalary, salary.getMonthlySalary(), 0.001);
    }

    @Test
    @DisplayName("Test constructor for new record (without ID, with Position object)")
    void constructorForNewRecord_WithPositionObject_SetsProperties() {
        double monthlySalary = 40000.0;

        Salary salary = new Salary(mockPosition, monthlySalary);

        assertEquals(0, salary.getId(), "ID має бути 0 для нового запису.");
        assertSame(mockPosition, salary.getPosition());
        assertEquals(mockPosition.getId(), salary.getPositionId());
        assertEquals(monthlySalary, salary.getMonthlySalary(), 0.001);
    }

    @Test
    @DisplayName("Test constructor for new record handles null Position")
    void constructorForNewRecord_HandlesNullPosition() {
        double monthlySalary = 10000.0;
        Salary salary = new Salary(null, monthlySalary);
        assertNull(salary.getPosition());
        assertEquals(0, salary.getPositionId());
    }

    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        Salary salary = new Salary();
        int testId = 101;
        salary.setId(testId);
        assertEquals(testId, salary.getId());
    }

    @Test
    @DisplayName("Test setPositionId and getPositionId (direct set)")
    void setPositionIdAndGetPositionId() {
        Salary salary = new Salary();
        int testPositionId = 77;
        salary.setPositionId(testPositionId);
        assertEquals(testPositionId, salary.getPositionId());
        assertNull(salary.getPosition(), "Position object має бути" +
                " null при прямому встановленні positionId.");
    }

    @Test
    @DisplayName("Test setPosition updates position and positionId")
    void setPosition_UpdatesPositionAndId() {
        Salary salary = new Salary();
        salary.setPosition(mockPosition);

        assertSame(mockPosition, salary.getPosition());
        assertEquals(mockPosition.getId(), salary.getPositionId());
    }

    @Test
    @DisplayName("Test setPosition with null updates position to null " +
            "(positionId remains unchanged - CURRENT LOGIC)")
    void setPosition_WithNull_UpdatesPositionToNull_PositionIdUnchanged() {
        Salary salary = new Salary(1, mockPosition, 20000.0);
        assertEquals(mockPosition.getId(), salary.getPositionId());


        salary.setPosition(null);

        assertNull(salary.getPosition());
        assertEquals(mockPosition.getId(), salary.getPositionId(),
                "PositionId НЕ має змінюватися, якщо новий Position null.");
    }

    @Test
    @DisplayName("Test setMonthlySalary and getMonthlySalary work correctly")
    void setMonthlySalaryAndGetMonthlySalary() {
        Salary salary = new Salary();
        double testSalary = 55555.55;
        salary.setMonthlySalary(testSalary);
        assertEquals(testSalary, salary.getMonthlySalary(), 0.001);
    }

    @Test
    @DisplayName("Test idProperty works")
    void idProperty_Works() {
        Salary salary = new Salary();
        IntegerProperty prop = salary.idProperty();
        assertNotNull(prop);
        assertEquals(salary.getId(), prop.get());
        prop.set(88);
        assertEquals(88, salary.getId());
    }

    @Test
    @DisplayName("Test positionIdProperty works")
    void positionIdProperty_Works() {
        Salary salary = new Salary();
        IntegerProperty prop = salary.positionIdProperty();
        assertNotNull(prop);
        assertEquals(salary.getPositionId(), prop.get());
        prop.set(99);
        assertEquals(99, salary.getPositionId());
    }

    @Test
    @DisplayName("Test positionProperty updates position BUT NOT positionId (CURRENT BEHAVIOR)")
    void positionProperty_UpdatesPosition_ButNotPositionId_CurrentBehavior() {
        Salary salary = new Salary();
        salary.positionProperty().set(mockPosition);

        assertSame(mockPosition, salary.getPosition());

        assertEquals(0, salary.getPositionId(), "PositionId НЕ має" +
                " оновитися автоматично через positionProperty.");
    }

    @Test
    @DisplayName("Test positionProperty set to null does not change positionId (CURRENT BEHAVIOR)")
    void positionProperty_SetToNull_DoesNotChangePositionId_CurrentBehavior() {
        Salary salary = new Salary(1, mockPosition, 20000.0);
        assertEquals(mockPosition.getId(), salary.getPositionId());

        salary.positionProperty().set(null);

        assertNull(salary.getPosition());

        assertEquals(mockPosition.getId(), salary.getPositionId(),
                "PositionId НЕ має змінитися, якщо positionProperty встановлено в null.");
    }


    @Test
    @DisplayName("Test monthlySalaryProperty works")
    void monthlySalaryProperty_Works() {
        Salary salary = new Salary();
        DoubleProperty prop = salary.monthlySalaryProperty();
        assertNotNull(prop);
        assertEquals(salary.getMonthlySalary(), prop.get(), 0.001);
        prop.set(1234.56);
        assertEquals(1234.56, salary.getMonthlySalary(), 0.001);
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        Salary salary1 = new Salary(1, 50, 20000.0);
        assertTrue(salary1.equals(salary1));
    }

    @Test
    @DisplayName("Test equals is symmetric for same ID")
    void equals_IsSymmetric_SameId() {
        Salary salary1 = new Salary(1, 50, 20000.0);
        Salary salary2 = new Salary(1, mockPosition, 30000.0);
        assertTrue(salary1.equals(salary2));
        assertTrue(salary2.equals(salary1));
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        Salary salary1 = new Salary(1, 50, 20000.0);
        Salary salary2 = new Salary(2, 50, 20000.0);
        assertFalse(salary1.equals(salary2));
    }

    @Test
    @DisplayName("Test equals handles objects with ID 0 (CURRENT Salary.equals BEHAVIOR)")
    void equals_ObjectsWithIdZero_CurrentBehavior() {
        Salary salary1 = new Salary();
        Salary salary2 = new Salary();
        Salary salary3 = salary1;

        assertTrue(salary1.equals(salary2), "Два різних екземпляри з ID=0 будуть" +
                " рівними через поточну логіку equals.");
        assertTrue(salary1.equals(salary3));
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        Salary salary1 = new Salary(1, 50, 20000.0);
        int initialHashCode = salary1.hashCode();
        salary1.setMonthlySalary(25000.0);
        assertEquals(initialHashCode, salary1.hashCode());
    }
}