package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.KitchenTask;
import org.example.restaurant_management_system.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KitchenService {


    //для отримання дефолтного часу приготування для страви з menu_cooking_times
    public int getEstimatedCookingTimeForMenuItem(int menuItemId) {
        String sql = "SELECT default_cooking_time FROM menu_cooking_times WHERE menu_item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, menuItemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("default_cooking_time");
            }
        } catch (SQLException e) {
            System.err.println("Помилка отримання орієнтовного часу приготування для страви меню: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    //створення нового завдання
    public boolean createKitchenTask(int orderItemId, int priority, int estimatedCookingTime) {
        String sql = "INSERT INTO kitchen_tasks (order_item_id, cooking_status, priority, estimated_cooking_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderItemId);
            pstmt.setString(2, "В очікуванні");
            pstmt.setInt(3, priority);
            pstmt.setInt(4, estimatedCookingTime);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Помилка створення кухонного завдання: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //отримання всіх завдань з деталями замовлення та страви
    public List<KitchenTask> getAllKitchenTasks() {
        List<KitchenTask> kitchenTasks = new ArrayList<>();
        String sql = "SELECT kt.id AS kitchen_task_id, kt.order_item_id, kt.cooking_status, kt.priority, " +
                "kt.start_cooking_time, kt.end_cooking_time, kt.estimated_cooking_time, " +
                "o.id AS order_id, mi.name AS menu_item_name, oi.quantity " +
                "FROM kitchen_tasks kt " +
                "JOIN order_items oi ON kt.order_item_id = oi.id " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                "JOIN orders o ON oi.order_id = o.id " +
               "ORDER BY kt.priority ASC, o.id ASC, kt.start_cooking_time ASC;";

        System.out.println("KitchenService: Виконую SQL-запит для отримання всіх завдань: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (conn == null) {
                System.err.println("KitchenService: З'єднання з базою даних NULL! Перевірте DatabaseConnection.");
                return kitchenTasks;
            }

            System.out.println("KitchenService: Запит виконано успішно. Отримую ResultSet.");

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                KitchenTask task = new KitchenTask();
                task.setId(rs.getInt("kitchen_task_id"));
                task.setOrderItemId(rs.getInt("order_item_id"));
                task.setCookingStatus(rs.getString("cooking_status"));
                task.setPriority(rs.getInt("priority"));

                if (rowCount <= 5) {
                    System.out.println("KitchenService: Отримано завдання #" + rowCount + ": ID=" + task.getId() +
                            ", Статус=" + task.getCookingStatus() +
                            ", Замовлення=" + rs.getInt("order_id") +
                            ", Страва=" + rs.getString("menu_item_name") +
                            ", Кількість=" + rs.getInt("quantity"));
                }


                Timestamp startTime = rs.getTimestamp("start_cooking_time");
                if (startTime != null) {
                    task.setStartCookingTime(startTime.toLocalDateTime());
                } else {
                    task.setStartCookingTime(null);
                }
                Timestamp endTime = rs.getTimestamp("end_cooking_time");
                if (endTime != null) {
                    task.setEndCookingTime(endTime.toLocalDateTime());
                } else {
                    task.setEndCookingTime(null);
                }

                int estimatedTime = rs.getInt("estimated_cooking_time");
                if (rs.wasNull()) {
                    task.setEstimatedCookingTime(0);
                } else {
                    task.setEstimatedCookingTime(estimatedTime);
                }

                task.setOrderId(rs.getInt("order_id"));
                task.setMenuItemName(rs.getString("menu_item_name"));
                task.setQuantity(rs.getInt("quantity"));

                kitchenTasks.add(task);
            }
            System.out.println("KitchenService: Зчитано всього " + rowCount + " завдань з бази даних."); // Додано лог

        } catch (SQLException e) {
            System.err.println("KitchenService: !!! КРИТИЧНА ПОМИЛКА SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("KitchenService: !!! НЕВІДОМА ПОМИЛКА під час отримання завдань: " + e.getMessage());
            e.printStackTrace();
        }
        return kitchenTasks;
    }

    // оновлення статусу кухонного завдання
    public boolean updateKitchenTaskStatus(int kitchenTaskId, String newStatus) {
        String sql = "UPDATE kitchen_tasks SET cooking_status = ?";
        LocalDateTime currentTime = LocalDateTime.now();
        if ("В роботі".equals(newStatus)) {
            sql += ", start_cooking_time = ?";
        } else if ("Готово".equals(newStatus) || "Скасовано".equals(newStatus)) {
            sql += ", end_cooking_time = ?";
        }
        sql += " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            int paramIndex = 2;
            if ("В роботі".equals(newStatus) || "Готово".equals(newStatus) || "Скасовано".equals(newStatus)) {
                pstmt.setTimestamp(paramIndex++, Timestamp.valueOf(currentTime));
            }
            pstmt.setInt(paramIndex, kitchenTaskId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Помилка оновлення статусу кухонного завдання: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // встановлення пріоритету  завдання
    public boolean setKitchenTaskPriority(int kitchenTaskId, int priority) {
        String sql = "UPDATE kitchen_tasks SET priority = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, priority);
            pstmt.setInt(2, kitchenTaskId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Помилка встановлення пріоритету кухонного завдання: " + e.getMessage());
            e.printStackTrace(); // Додано
            return false;
        }
    }
}