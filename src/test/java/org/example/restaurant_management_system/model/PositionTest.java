package org.example.restaurant_management_system.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    @DisplayName("Test no-argument constructor creates an empty Position")
    void noArgConstructor_CreatesEmptyPosition() {
        Position position = new Position();
        assertEquals(0, position.getId(), "ID має бути 0" +
                " для конструктора без аргументів за замовчуванням.");

        assertNull(position.getName(), "Name має бути null " +
                "для конструктора без аргументів за замовчуванням.");
    }

    @Test
    @DisplayName("Test full constructor sets id and name correctly")
    void fullConstructor_SetsIdAndName() {
        int expectedId = 1;
        String expectedName = "Офіціант";
        Position position = new Position(expectedId, expectedName);

        assertEquals(expectedId, position.getId(), "ID має бути встановлено конструктором.");
        assertEquals(expectedName, position.getName(), "Name має бути встановлено конструктором.");
    }

    @Test
    @DisplayName("Test constructor with name only sets name and default id")
    void nameOnlyConstructor_SetsNameAndDefaultId() {
        String expectedName = "Кухар";
        Position position = new Position(expectedName);

        assertEquals(0, position.getId(), "ID має бути 0 за замовчуванням, " +
                "якщо встановлюється тільки name.");

        assertEquals(expectedName, position.getName(), "Name має бути встановлено конструктором.");
    }


    @Test
    @DisplayName("Test setId updates the id")
    void setId_UpdatesId() {
        Position position = new Position();
        int newId = 5;
        position.setId(newId);
        assertEquals(newId, position.getId(), "setId має оновити ID.");
    }

    @Test
    @DisplayName("Test setName updates the name")
    void setName_UpdatesName() {
        Position position = new Position();
        String newName = "Адміністратор";
        position.setName(newName);
        assertEquals(newName, position.getName(), "setName має оновити name.");
    }

    @Test
    @DisplayName("Test setName allows null name")
    void setName_AllowsNullName() {
        Position position = new Position("Початкова назва");
        position.setName(null);
        assertNull(position.getName(), "setName має дозволяти встановлення null для name.");
    }

    @Test
    @DisplayName("Test getName returns the correct name")
    void getName_ReturnsCorrectName() {
        String expectedName = "Менеджер";
        Position position = new Position(expectedName);
        assertEquals(expectedName, position.getName(), "getName має повертати правильне ім'я.");
    }

    @Test
    @DisplayName("Test getId returns the correct id")
    void getId_ReturnsCorrectId() {
        int expectedId = 10;
        Position position = new Position(expectedId, "Тестова посада");
        assertEquals(expectedId, position.getId(), "getId має повертати правильний ID.");
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        Position pos1 = new Position(1, "Офіціант");
        assertTrue(pos1.equals(pos1), "Позиція має дорівнювати сама собі.");
    }

    @Test
    @DisplayName("Test equals is symmetric")
    void equals_IsSymmetric() {
        Position pos1 = new Position(1, "Офіціант");
        Position pos2 = new Position(1, "Інша Назва Офіціанта");
        assertTrue(pos1.equals(pos2), "Позиції з однаковим ID мають бути рівними.");
        assertTrue(pos2.equals(pos1), "Рівність має бути симетричною.");
    }

    @Test
    @DisplayName("Test equals is transitive")
    void equals_IsTransitive() {
        Position pos1 = new Position(1, "A");
        Position pos2 = new Position(1, "B");
        Position pos3 = new Position(1, "C");

        assertTrue(pos1.equals(pos2));
        assertTrue(pos2.equals(pos3));
        assertTrue(pos1.equals(pos3), "Рівність має бути транзитивною.");
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        Position pos1 = new Position(1, "Офіціант");
        Position pos2 = new Position(2, "Офіціант");
        assertFalse(pos1.equals(pos2), "Позиції з різними ID не мають бути рівними.");
    }

    @Test
    @DisplayName("Test equals returns false for null object")
    void equals_NullObject_ReturnsFalse() {
        Position pos1 = new Position(1, "Офіціант");
        assertFalse(pos1.equals(null), "Позиція не має дорівнювати null.");
    }

    @Test
    @DisplayName("Test equals returns false for object of different class")
    void equals_DifferentClass_ReturnsFalse() {
        Position pos1 = new Position(1, "Офіціант");
        Object otherObject = new Object();
        assertFalse(pos1.equals(otherObject), "Позиція не має " +
                "дорівнювати об'єкту іншого класу.");
    }

    @Test
    @DisplayName("Test equals handles objects with ID 0 (CURRENT Position.equals BEHAVIOR)")
    void equals_ObjectsWithIdZero_CurrentBehavior() {
        Position pos1 = new Position("Тест1");
        Position pos2 = new Position("Тест2");
        Position pos3 = pos1;

        assertTrue(pos1.equals(pos2), "Два різних екземпляри з ID=0 будуть " +
                "рівними через поточну логіку equals.");
        assertTrue(pos1.equals(pos3));

        pos1.setId(5);
        pos2.setId(5);
        assertTrue(pos1.equals(pos2), "Об'єкти з однаковим ненульовим ID мають бути рівними.");
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        Position pos1 = new Position(1, "Офіціант");
        int initialHashCode = pos1.hashCode();

        pos1.setName("Старший офіціант");
        assertEquals(initialHashCode, pos1.hashCode(), "HashCode має бути послідовним," +
                " якщо ID не змінюється.");
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void hashCode_ForEqualObjects() {
        Position pos1 = new Position(1, "Офіціант");
        Position pos2 = new Position(1, "Інша Назва Офіціанта");
        assertTrue(pos1.equals(pos2));
        assertEquals(pos1.hashCode(), pos2.hashCode(), "Рівні об'єкти мають" +
                " мати однаковий hashCode.");
    }

    @Test
    @DisplayName("Test hashCode for different objects (different ID)")
    void hashCode_ForDifferentObjects_DifferentId() {
        Position pos1 = new Position(1, "Офіціант");
        Position pos2 = new Position(2, "Офіціант");
        if (pos1.hashCode() == pos2.hashCode() && pos1.getId() != pos2.getId()) {
            System.out.println("Warning: Different IDs produced same hashCode. " +
                    "This is possible but less ideal.");
        }
    }

    @Test
    @DisplayName("Test toString returns the position name")
    void toString_ReturnsPositionName() {
        String expectedName = "Бармен";
        Position position = new Position(expectedName);
        assertEquals(expectedName, position.toString(), "toString() " +
                "має повертати назву посади.");
    }

    @Test
    @DisplayName("Test toString returns null if name is null")
    void toString_ReturnsNullIfNameIsNull() {
        Position position = new Position(); // name буде null
        assertNull(position.toString(), "toString() має повертати null, " +
                "якщо ім'я посади null.");
    }

    @Test
    @DisplayName("Test getters and setters work together")
    void gettersAndSetters_WorkTogether() {
        Position position = new Position();
        int testId = 100;
        String testName = "Головний кухар";

        position.setId(testId);
        position.setName(testName);

        assertEquals(testId, position.getId(), "ID після setId/getId не співпадає.");
        assertEquals(testName, position.getName(), "Name після setName/getName не співпадає.");
        assertEquals(testName, position.toString(), "toString після setName не співпадає.");
    }
}