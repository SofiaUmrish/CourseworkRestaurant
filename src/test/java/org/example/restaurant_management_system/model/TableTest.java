package org.example.restaurant_management_system.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        Table table = new Table();

        assertEquals(0, table.getId(), "ID має бути 0 за замовчуванням.");
        assertNull(table.getTableNumber(), "TableNumber має бути null за замовчуванням.");
        assertEquals(0, table.getCapacity(), "Capacity має бути 0 за замовчуванням.");

        assertNotNull(table.idProperty());
        assertNotNull(table.tableNumberProperty());
        assertNotNull(table.capacityProperty());
    }

    @Test
    @DisplayName("Test constructor for loading from DB (with id, tableNumber, capacity)")
    void constructorForDbLoad_SetsProperties() {
        int id = 1;
        String tableNumber = "A101";
        int capacity = 4;

        Table table = new Table(id, tableNumber, capacity);

        assertEquals(id, table.getId());
        assertEquals(tableNumber, table.getTableNumber());
        assertEquals(capacity, table.getCapacity());
    }

    @Test
    @DisplayName("Test constructor for new record (without id, with tableNumber, capacity)")
    void constructorForNewRecord_SetsPropertiesAndDefaultId() {
        String tableNumber = "B202";
        int capacity = 2;

        Table table = new Table(tableNumber, capacity);

        assertEquals(0, table.getId(), "ID має бути 0 за" +
                " замовчуванням для нового запису.");

        assertEquals(tableNumber, table.getTableNumber());
        assertEquals(capacity, table.getCapacity());
    }

    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        Table table = new Table();
        int testId = 55;
        table.setId(testId);
        assertEquals(testId, table.getId());
    }

    @Test
    @DisplayName("Test setTableNumber and getTableNumber work correctly")
    void setTableNumberAndGetTableNumber() {
        Table table = new Table();
        String testTableNumber = "C303";
        table.setTableNumber(testTableNumber);
        assertEquals(testTableNumber, table.getTableNumber());
    }

    @Test
    @DisplayName("Test setCapacity and getCapacity work correctly")
    void setCapacityAndGetCapacity() {
        Table table = new Table();
        int testCapacity = 6;
        table.setCapacity(testCapacity);
        assertEquals(testCapacity, table.getCapacity());
    }

    @Test
    @DisplayName("Test idProperty works")
    void idProperty_Works() {
        Table table = new Table();
        IntegerProperty prop = table.idProperty();
        assertNotNull(prop);
        assertEquals(table.getId(), prop.get());
        prop.set(111);
        assertEquals(111, table.getId());
    }

    @Test
    @DisplayName("Test tableNumberProperty works")
    void tableNumberProperty_Works() {
        Table table = new Table();
        StringProperty prop = table.tableNumberProperty();
        assertNotNull(prop);
        assertEquals(table.getTableNumber(), prop.get());
        String newTableNumber = "VIP1";
        prop.set(newTableNumber);
        assertEquals(newTableNumber, table.getTableNumber());
    }

    @Test
    @DisplayName("Test capacityProperty works")
    void capacityProperty_Works() {
        Table table = new Table();
        IntegerProperty prop = table.capacityProperty();
        assertNotNull(prop);
        assertEquals(table.getCapacity(), prop.get());
        prop.set(8);
        assertEquals(8, table.getCapacity());
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        Table table1 = new Table(1, "T1", 4);
        assertTrue(table1.equals(table1));
    }

    @Test
    @DisplayName("Test equals is symmetric for same ID")
    void equals_IsSymmetric_SameId() {
        Table table1 = new Table(1, "T1", 4);
        Table table2 = new Table(1, "ІншийНомер", 2);
        assertTrue(table1.equals(table2));
        assertTrue(table2.equals(table1));
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        Table table1 = new Table(1, "T1", 4);
        Table table2 = new Table(2, "T1", 4);
        assertFalse(table1.equals(table2));
    }

    @Test
    @DisplayName("Test equals handles objects with ID 0 (CURRENT Table.equals BEHAVIOR)")
    void equals_ObjectsWithIdZero_CurrentBehavior() {
        Table table1 = new Table("T0A", 2);
        Table table2 = new Table("T0B", 4);
        Table table3 = table1;

        assertTrue(table1.equals(table2), "Два різних екземпляри з ID=0 " +
                "будуть рівними через поточну логіку equals.");
        assertTrue(table1.equals(table3));
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        Table table1 = new Table(1, "T1", 4);
        int initialHashCode = table1.hashCode();
        table1.setTableNumber("T1-Змінений");
        assertEquals(initialHashCode, table1.hashCode());
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void hashCode_ForEqualObjects() {
        Table table1 = new Table(1, "T1", 4);
        Table table2 = new Table(1, "T2", 2);
        assertTrue(table1.equals(table2));
        assertEquals(table1.hashCode(), table2.hashCode());
    }

    @Test
    @DisplayName("Test toString returns correct format")
    void toString_ReturnsCorrectFormat() {
        String tableNumber = "A1";
        int capacity = 4;
        Table table = new Table(tableNumber, capacity);
        String expectedString = "Столик " + tableNumber + " (місць: " + capacity + ")";
        assertEquals(expectedString, table.toString());
    }

    @Test
    @DisplayName("Test toString handles null tableNumber")
    void toString_HandlesNullTableNumber() {
        int capacity = 2;
        Table table = new Table();
        table.setCapacity(capacity);
        String expectedString = "Столик null (місць: " + capacity + ")";
        assertEquals(expectedString, table.toString());
    }

    @Test
    @DisplayName("Test toString handles default capacity (0)")
    void toString_HandlesDefaultCapacity() {
        String tableNumber = "B2";
        Table tableWithZeroCapacity = new Table(tableNumber, 0);
        String expectedString = "Столик " + tableNumber + " (місць: 0)";
        assertEquals(expectedString, tableWithZeroCapacity.toString());

        // Для перевірки конструктора за замовчуванням для capacity,
        // ми можемо створити Table() і потім встановити tableNumber
        Table tableDefaultCapacity = new Table(); // Тут capacity буде 0
        tableDefaultCapacity.setTableNumber(tableNumber);
        assertEquals(expectedString, tableDefaultCapacity.toString(), "toString() " +
                "для столика з tableNumber та capacity=0 (за замовчуванням)");
    }


    @Test
    @DisplayName("Test getters and setters work together")
    void gettersAndSetters_WorkTogether() {
        Table table = new Table();
        int testId = 777;
        String testTableNumber = "Terrace-5";
        int testCapacity = 6;

        table.setId(testId);
        table.setTableNumber(testTableNumber);
        table.setCapacity(testCapacity);

        assertEquals(testId, table.getId());
        assertEquals(testTableNumber, table.getTableNumber());
        assertEquals(testCapacity, table.getCapacity());
        assertEquals("Столик " + testTableNumber + " (місць: " + testCapacity + ")",
                table.toString());
    }
}