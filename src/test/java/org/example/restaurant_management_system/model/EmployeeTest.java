package org.example.restaurant_management_system.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeTest {

    private Position mockPosition;

    @BeforeEach
    void setUp() {
        mockPosition = mock(Position.class);
        when(mockPosition.getName()).thenReturn("Тестова Посада");
    }

    @Test
    @DisplayName("Test constructor sets all fields correctly")
    void constructor_SetsAllFields() {
        int expectedId = 1;
        String expectedFirstName = "Іван";
        String expectedLastName = "Франко";
        String expectedPassword = "securePassword123";

        Employee employee = new Employee(expectedId,
                expectedFirstName, expectedLastName, mockPosition, expectedPassword);

        assertEquals(expectedId, employee.getId(),
                "ID має бути встановлено конструктором.");
        assertEquals(expectedFirstName, employee.getFirstName(),
                "FirstName має бути встановлено конструктором.");
        assertEquals(expectedLastName, employee.getLastName(),
                "LastName має бути встановлено конструктором.");
        assertSame(mockPosition, employee.getPosition(),
                "Position має бути встановлено конструктором.");
        assertEquals(expectedPassword, employee.getPassword(),
                "Password має бути встановлено конструктором.");
    }


    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        Employee employee = new Employee(0, "",
                "", null, ""); // Початкові значення
        int testId = 101;
        employee.setId(testId);
        assertEquals(testId, employee.getId());
    }

    @Test
    @DisplayName("Test setFirstName and getFirstName work correctly")
    void setFirstNameAndGetFirstName() {
        Employee employee = new Employee(0, "",
                "", null, "");
        String testName = "Тарас";
        employee.setFirstName(testName);
        assertEquals(testName, employee.getFirstName());
    }

    @Test
    @DisplayName("Test setLastName and getLastName work correctly")
    void setLastNameAndGetLastName() {
        Employee employee = new Employee(0, "",
                "", null, "");
        String testName = "Шевченко";
        employee.setLastName(testName);
        assertEquals(testName, employee.getLastName());
    }

    @Test
    @DisplayName("Test setPosition and getPosition work correctly")
    void setPositionAndGetPosition() {
        Employee employee = new Employee(0, "",
                "", null, "");
        Position newMockPosition = mock(Position.class);
        employee.setPosition(newMockPosition);
        assertSame(newMockPosition, employee.getPosition());
    }

    @Test
    @DisplayName("Test setPassword and getPassword work correctly")
    void setPasswordAndGetPassword() {
        Employee employee = new Employee(0, "",
                "", null, "");
        String testPassword = "newPassword@!#";
        employee.setPassword(testPassword);
        assertEquals(testPassword, employee.getPassword());
    }


    @Test
    @DisplayName("Test toString returns correct format with valid position")
    void toString_ReturnsCorrectFormat_WithValidPosition() {
        String firstName = "Леся";
        String lastName = "Українка";

        Employee employee = new Employee(2, firstName,
                lastName, mockPosition, "password");
        String expectedString = firstName + " " + lastName + " (" + mockPosition.getName() + ")";
        assertEquals(expectedString, employee.toString());
    }

    @Test
    @DisplayName("Test toString handles null position gracefully " +
            "(throws NullPointerException as per current implementation)")
    void toString_HandlesNullPosition_ThrowsNullPointerException() {
        Employee employee = new Employee(3, "Остап",
                "Вишня", null, "password");

        assertThrows(NullPointerException.class, () -> {
            employee.toString();
        }, "toString() має кинути NullPointerException, " +
                "якщо position є null, через виклик getName() на null.");
    }

    @Test
    @DisplayName("Test toString handles position with null name")
    void toString_HandlesPositionWithNullName() {
        when(mockPosition.getName()).thenReturn(null);

        Employee employee = new Employee(4, "Григорій",
                "Сковорода", mockPosition, "filosof");
        String expectedString = "Григорій Сковорода (null)";
        assertEquals(expectedString, employee.toString(),
                "toString() має коректно обробляти null ім'я позиції.");
    }


    @Test
    @DisplayName("Test getters and setters work together")
    void gettersAndSetters_WorkTogether() {
        Employee employee = new Employee(0, "",
                "", null, ""); // Використовуємо мок з setUp

        int testId = 200;
        String testFirstName = "Майкл";
        String testLastName = "Джордан";
        String testPassword = "airJordan23";
        Position anotherMockPosition = mock(Position.class);
        when(anotherMockPosition.getName()).thenReturn("Легенда Баскетболу");


        employee.setId(testId);
        employee.setFirstName(testFirstName);
        employee.setLastName(testLastName);
        employee.setPosition(anotherMockPosition);
        employee.setPassword(testPassword);

        assertEquals(testId, employee.getId());
        assertEquals(testFirstName, employee.getFirstName());
        assertEquals(testLastName, employee.getLastName());
        assertSame(anotherMockPosition, employee.getPosition());
        assertEquals(testPassword, employee.getPassword());
        assertEquals(testFirstName + " " + testLastName +
                " (" + anotherMockPosition.getName() + ")", employee.toString());
    }
}