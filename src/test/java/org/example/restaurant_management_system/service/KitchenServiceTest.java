package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.KitchenTask;
import org.example.restaurant_management_system.util.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)

public class KitchenServiceTest {

    private KitchenService kitchenService;

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() {
        kitchenService = new KitchenService();
    }

    @Test
    void getEstimatedCookingTimeForMenuItem_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("default_cooking_time")).thenReturn(30);

            int estimatedTime = kitchenService.getEstimatedCookingTimeForMenuItem(1);
            assertEquals(30, estimatedTime);

            verify(mockConnection).prepareStatement("SELECT default_cooking_time " +
                    "FROM menu_cooking_times WHERE menu_item_id = ?");

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeQuery();
            verify(mockResultSet).next();
            verify(mockResultSet).getInt("default_cooking_time");
        }
    }

    @Test
    void getEstimatedCookingTimeForMenuItem_NoResult() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            int estimatedTime = kitchenService.getEstimatedCookingTimeForMenuItem(99);
            assertEquals(0, estimatedTime);

            verify(mockPreparedStatement).setInt(1, 99);
        }
    }

    @Test
    void getEstimatedCookingTimeForMenuItem_SQLException() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

            int estimatedTime = kitchenService.getEstimatedCookingTimeForMenuItem(1);
            assertEquals(0, estimatedTime);
        }
    }

    @Test
    void createKitchenTask_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            boolean result = kitchenService.createKitchenTask(101, 1,
                    25);
            assertTrue(result);

            verify(mockConnection).prepareStatement("INSERT INTO kitchen_tasks " +
                    "(order_item_id, cooking_status, priority, estimated_cooking_time) VALUES (?, ?, ?, ?)");

            verify(mockPreparedStatement).setInt(1, 101);
            verify(mockPreparedStatement).setString(2, "В очікуванні");
            verify(mockPreparedStatement).setInt(3, 1);
            verify(mockPreparedStatement).setInt(4, 25);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void createKitchenTask_Failure_NoRowsAffected() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);

            boolean result = kitchenService.createKitchenTask(102,
                    2, 40);
            assertFalse(result);
        }
    }

    @Test
    void createKitchenTask_SQLException() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test SQL Exception"));

            boolean result = kitchenService.createKitchenTask(103,
                    3, 15);
            assertFalse(result);
        }
    }


    @Test
    void getAllKitchenTasks_EmptyList() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            List<KitchenTask> tasks = kitchenService.getAllKitchenTasks();
            assertNotNull(tasks);
            assertTrue(tasks.isEmpty());

            verify(mockResultSet).next();
        }
    }

    @Test
    void getAllKitchenTasks_SQLException() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

            List<KitchenTask> tasks = kitchenService.getAllKitchenTasks();
            assertNotNull(tasks);
            assertTrue(tasks.isEmpty());
        }
    }

    @Test
    void getAllKitchenTasks_EstimatedCookingTimeNull() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getInt("kitchen_task_id")).thenReturn(1);
            when(mockResultSet.getInt("order_item_id")).thenReturn(10);
            when(mockResultSet.getString("cooking_status")).thenReturn("В очікуванні");
            when(mockResultSet.getInt("priority")).thenReturn(1);
            when(mockResultSet.getTimestamp("start_cooking_time")).thenReturn(null);
            when(mockResultSet.getTimestamp("end_cooking_time")).thenReturn(null);
            when(mockResultSet.getInt("estimated_cooking_time")).thenReturn(0);
            when(mockResultSet.wasNull()).thenReturn(true);

            when(mockResultSet.getInt("order_id")).thenReturn(100);
            when(mockResultSet.getString("menu_item_name")).thenReturn("Десерт");
            when(mockResultSet.getInt("quantity")).thenReturn(1);

            List<KitchenTask> tasks = kitchenService.getAllKitchenTasks();
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            assertEquals(0, tasks.get(0).getEstimatedCookingTime());
        }
    }

    @Test
    void updateKitchenTaskStatus_WaitingToInProgress() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            boolean result = kitchenService.updateKitchenTaskStatus(1, "В роботі");
            assertTrue(result);

            verify(mockConnection).prepareStatement(startsWith("UPDATE kitchen_tasks SET cooking_status = ?"));
            verify(mockPreparedStatement).setString(1, "В роботі");
            verify(mockPreparedStatement).setTimestamp(eq(2), any(Timestamp.class));
            verify(mockPreparedStatement).setInt(3, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void updateKitchenTaskStatus_InProgressToDone() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            boolean result = kitchenService.updateKitchenTaskStatus(2, "Готово");
            assertTrue(result);

            verify(mockConnection).prepareStatement(startsWith("UPDATE kitchen_tasks SET cooking_status = ?"));
            verify(mockPreparedStatement).setString(1, "Готово");
            verify(mockPreparedStatement).setTimestamp(eq(2), any(Timestamp.class));
            verify(mockPreparedStatement).setInt(3, 2);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void updateKitchenTaskStatus_ToCanceled() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            boolean result = kitchenService.updateKitchenTaskStatus(3, "Скасовано");
            assertTrue(result);

            verify(mockConnection).prepareStatement(startsWith("UPDATE kitchen_tasks SET cooking_status = ?"));
            verify(mockPreparedStatement).setString(1, "Скасовано");
            verify(mockPreparedStatement).setTimestamp(eq(2), any(Timestamp.class));
            verify(mockPreparedStatement).setInt(3, 3);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void updateKitchenTaskStatus_NoTimestampUpdate() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            boolean result = kitchenService.updateKitchenTaskStatus(4,
                    "В очікуванні");
            assertTrue(result);

            verify(mockConnection).prepareStatement("UPDATE kitchen_tasks " +
                    "SET cooking_status = ? WHERE id = ?");

            verify(mockPreparedStatement).setString(1, "В очікуванні");
            verify(mockPreparedStatement, never()).setTimestamp(anyInt(), any(Timestamp.class));
            verify(mockPreparedStatement).setInt(2, 4);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void updateKitchenTaskStatus_Failure_NoRowsAffected() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);

            boolean result = kitchenService.updateKitchenTaskStatus(99, "Готово");
            assertFalse(result);
        }
    }

    @Test
    void updateKitchenTaskStatus_SQLException() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test SQL Exception"));

            boolean result = kitchenService.updateKitchenTaskStatus(1, "В роботі");
            assertFalse(result);
        }
    }

    @Test
    void setKitchenTaskPriority_Success() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            boolean result = kitchenService.setKitchenTaskPriority(1, 5);
            assertTrue(result);

            verify(mockConnection).prepareStatement("UPDATE kitchen_tasks SET priority = ? WHERE id = ?");
            verify(mockPreparedStatement).setInt(1, 5);
            verify(mockPreparedStatement).setInt(2, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void setKitchenTaskPriority_Failure_NoRowsAffected() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);

            boolean result = kitchenService.setKitchenTaskPriority(99, 10);
            assertFalse(result);
        }
    }

    @Test
    void setKitchenTaskPriority_SQLException() throws SQLException {
        try (MockedStatic<DatabaseConnection> mockedStatic = mockStatic(DatabaseConnection.class)) {
            mockedStatic.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test SQL Exception"));

            boolean result = kitchenService.setKitchenTaskPriority(1, 1);
            assertFalse(result);
        }
    }
}