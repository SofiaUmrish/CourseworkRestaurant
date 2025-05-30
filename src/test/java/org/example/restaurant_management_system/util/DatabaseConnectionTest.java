package org.example.restaurant_management_system.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    @DisplayName("Test if a connection to the database can be successfully established")
    void testGetConnection_Success() {

        Connection connection = null;

        try {

            connection = DatabaseConnection.getConnection();
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");
            System.out.println("Successfully connected to the database!");

        } catch (SQLException e) {
            fail("Failed to connect to the database: " + e.getMessage());

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    assertTrue(connection.isClosed(),
                            "Connection should be closed after test");

                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}