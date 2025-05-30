package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.exception.AuthenticationException;
import org.example.restaurant_management_system.model.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {

    private Connection h2Connection;
    private MockedStatic<org.example.restaurant_management_system.util.DatabaseConnection>
            mockedDbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        h2Connection = DriverManager.getConnection(
                "jdbc:h2:mem:testemployeedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

        mockedDbConnection = Mockito.mockStatic(org.example.restaurant_management_system.
                util.DatabaseConnection.class);

        mockedDbConnection.when(org.example.restaurant_management_system.util.
                        DatabaseConnection::getConnection)
                .thenReturn(h2Connection);

        try (Statement stmt = h2Connection.createStatement()) {
            try {
                stmt.execute("DROP TABLE employees IF EXISTS");

            } catch (SQLException e) {
            }
            try {
                stmt.execute("DROP TABLE positions IF EXISTS");

            } catch (SQLException e) {
            }

            stmt.execute("CREATE TABLE positions (id INT PRIMARY KEY," +
                    " position_name VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE employees (id INT PRIMARY KEY," +
                    " first_name VARCHAR(255), last_name VARCHAR(255), " +
                    "password VARCHAR(255) NOT NULL, position_id INT, FOREIGN KEY (position_id) " +
                    "REFERENCES positions(id))");

            stmt.execute("INSERT INTO positions (id, position_name) VALUES (1, 'Manager')");
            stmt.execute("INSERT INTO positions (id, position_name) VALUES (2, 'Waiter')");

            stmt.execute("INSERT INTO employees (id, first_name, last_name," +
                    " password, position_id) VALUES (101, 'John', 'Doe', 'securepass', 1)");

            stmt.execute("INSERT INTO employees (id, first_name, last_name," +
                    " password, position_id) VALUES (102, 'Jane', 'Smith', 'pass123', 2)");
            stmt.execute("INSERT INTO employees (id, first_name, last_name, password, " +
                    "position_id) VALUES (103, 'Bob', 'Johnson', 'strongpass', 1)");

        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        // 1. Закриваємо MockedStatic
        if (mockedDbConnection != null) {
            mockedDbConnection.close();
            mockedDbConnection = null;
        }

        if (h2Connection != null && !h2Connection.isClosed()) {

            try (Statement stmt = h2Connection.createStatement()) {
                stmt.execute("DROP TABLE employees IF EXISTS");
                stmt.execute("DROP TABLE positions IF EXISTS");

            } finally {
                h2Connection.close();
            }
        }
        h2Connection = null;
    }

    @Test
    void authenticateById_successfulAuthentication() {

        Employee employee = EmployeeService.authenticateById(101, "securepass");

        assertNotNull(employee);
        assertEquals(101, employee.getId());
        assertEquals("John", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
        assertEquals("securepass", employee.getPassword());
        assertNotNull(employee.getPosition());
        assertEquals(1, employee.getPosition().getId());
        assertEquals("Manager", employee.getPosition().getName());
    }

    @Test
    void authenticateById_successfulAuthenticationWithTrim() {
        Employee employee = EmployeeService.authenticateById(102, "pass123   ");

        assertNotNull(employee);
        assertEquals(102, employee.getId());
        assertEquals("Jane", employee.getFirstName());
        assertEquals("Smith", employee.getLastName());
        assertEquals("pass123", employee.getPassword());
        assertNotNull(employee.getPosition());
        assertEquals(2, employee.getPosition().getId());
        assertEquals("Waiter", employee.getPosition().getName());
    }

    @Test
    void authenticateById_invalidPassword() {
        AuthenticationException thrown = assertThrows(AuthenticationException.class, () -> {
            EmployeeService.authenticateById(101, "wrongpass");
        });
        assertEquals("Неправильний пароль.", thrown.getMessage());
    }

    @Test
    void authenticateById_userNotFound() {

        AuthenticationException thrown = assertThrows(AuthenticationException.class, () -> {
            EmployeeService.authenticateById(999, "anypass");
        });
        assertEquals("Користувача з таким ID не знайдено.", thrown.getMessage());
    }

    @Test
    void authenticateById_databaseError() throws SQLException {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }

        mockedDbConnection.when(org.example.restaurant_management_system.util.
                        DatabaseConnection::getConnection)

                .thenThrow(new SQLException("Simulated DB connection error"));

        AuthenticationException thrown = assertThrows(AuthenticationException.class, () -> {
            EmployeeService.authenticateById(101, "securepass");
        });
        assertEquals("Помилка бази даних під час автентифікації.", thrown.getMessage());
    }

    @Test
    void authenticateById_nullPasswordInput() {

        assertThrows(NullPointerException.class, () -> {
            EmployeeService.authenticateById(101, null);
        });
    }

    @Test
    void authenticateById_emptyPasswordInput() {
        // Тестуємо порожній пароль
        AuthenticationException thrown = assertThrows(AuthenticationException.class, () -> {
            EmployeeService.authenticateById(101, "");
        });
        assertEquals("Неправильний пароль.", thrown.getMessage());
    }
}