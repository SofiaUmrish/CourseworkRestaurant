package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final Connection connection;
    private KitchenService kitchenService; // залежність від KitchenService


    public OrderService(Connection connection) {
        this.connection = connection;
        this.kitchenService = new KitchenService();
    }

    private Order createOrderFromResultSet(ResultSet rs) throws SQLException {
        Integer tableId = rs.getObject("table_id", Integer.class);
        Integer clientId = rs.getObject("client_id", Integer.class);
        Integer employeeId = rs.getObject("employee_id", Integer.class);
        Order order = new Order(
                rs.getInt("id"),
                rs.getTimestamp("order_time").toLocalDateTime(),
                tableId,
                clientId,
                employeeId,
                rs.getString("payment_method"),
                rs.getDouble("total_amount")
        );
        if (tableId != null && tableId != 0) order.setTable(getTableById(tableId));
        if (clientId != null && clientId != 0) order.setClient(getClientById(clientId));
        if (employeeId != null && employeeId != 0) order.setEmployee(getEmployeeById(employeeId));
        return order;
    }

    private OrderItem createOrderItemFromResultSet(ResultSet rs) throws SQLException {
        OrderItem orderItem = new OrderItem(
                rs.getInt("id"), rs.getInt("order_id"), rs.getInt("menu_item_id"),
                rs.getInt("quantity"), rs.getDouble("price_at_order")
        );
        orderItem.setMenuItem(getMenuItemById(orderItem.getMenuItemId()));
        return orderItem;
    }

    public MenuItem getMenuItemById(int id) throws SQLException {
        String query = "SELECT id, name, category_id, price, vegetarian, allergen, gluten_free FROM menu_items WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getInt("category_id"),
                            rs.getDouble("price"), rs.getBoolean("vegetarian"), rs.getBoolean("allergen"), rs.getBoolean("gluten_free"));
                }
            }
        }
        return null;
    }

    public Client getClientById(int id) throws SQLException {
        String query = "SELECT id, first_name, last_name, phone_number, email, loyalty_points FROM clients WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Client(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                            rs.getString("phone_number"), rs.getString("email"), rs.getDouble("loyalty_points"));
                }
            }
        }
        return null; // Повертаємо null, якщо клієнта не знайдено
    }

    private void updateClientLoyaltyPointsInDB(int clientId, double newLoyaltyPoints) throws SQLException {
        double pointsToSet = Math.max(0, newLoyaltyPoints);
        String sql = "UPDATE clients SET loyalty_points = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, pointsToSet);
            pstmt.setInt(2, clientId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("SERVICE DB: Бали для клієнта ID " + clientId + " оновлено на " + pointsToSet);
            } else {
                System.err.println("SERVICE DB: Не вдалося оновити бали для клієнта ID " + clientId + ". Клієнт не знайдений?");
            }
        }
    }

    private Table getTableById(int id) throws SQLException {
        String query = "SELECT id, table_number, capacity FROM tables WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return new Table(rs.getInt("id"), rs.getString("table_number"), rs.getInt("capacity"));
            }
        }
        return null;
    }

    private Position getPositionById(int id) throws SQLException {
        String query = "SELECT id, position_name FROM positions WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return new Position(rs.getInt("id"), rs.getString("position_name"));
            }
        }
        return new Position(id, "Невідома позиція");
    }

    private Employee getEmployeeById(int id) throws SQLException {
        String query = "SELECT id, first_name, last_name, position_id, password FROM employees WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                            getPositionById(rs.getInt("position_id")), rs.getString("password"));
                }
            }
        }
        return null;
    }

    public Order saveOrUpdateOrderCore(Order order, double bonusDiscountToApply, boolean isNewOrder) throws SQLException {
        double baseAmount = order.getTotalAmount(); // сума позицій з контролера
        double finalPayableAmount = baseAmount;
        Client clientInOrder = order.getClient(); // клієнт з контролера

        if (clientInOrder != null && clientInOrder.getId() != 0) {
            Client clientFromDB = getClientById(clientInOrder.getId());
            if (clientFromDB != null) {
                double currentDbPoints = clientFromDB.getLoyaltyPoints();
                double pointsAfterTransaction = currentDbPoints;

                if ("Бонуси".equals(order.getPaymentMethod())) {
                    double actualDiscount = Math.min(bonusDiscountToApply, currentDbPoints);
                    actualDiscount = Math.min(actualDiscount, baseAmount);
                    finalPayableAmount = baseAmount - actualDiscount;
                    pointsAfterTransaction = currentDbPoints - actualDiscount;
                    updateClientLoyaltyPointsInDB(clientFromDB.getId(), pointsAfterTransaction);
                } else if ("Картка".equals(order.getPaymentMethod()) && baseAmount > 0) {
                    double pointsToAccrue = finalPayableAmount * 0.05; // 5% від суми, що ПЛАТИТЬСЯ карткою
                    pointsAfterTransaction = currentDbPoints + pointsToAccrue;
                    updateClientLoyaltyPointsInDB(clientFromDB.getId(), pointsAfterTransaction);
                }
                clientFromDB.setLoyaltyPoints(pointsAfterTransaction); // оновлення об'єкта
                order.setClient(clientFromDB); // оновлений клієнт
            } else {
                order.setClient(null);
            }
        } else {
            order.setClient(null); // клієнт не вказаний
        }
        order.setTotalAmount(finalPayableAmount); // Фізагальна сума до сплати

        String sql = isNewOrder ?
                "INSERT INTO orders (order_time, table_id, client_id, employee_id, " +
                        "payment_method, total_amount) VALUES (?, ?, ?, ?, ?, ?)" :
                "UPDATE orders SET order_time = ?, table_id = ?, client_id = ?, " +
                        "employee_id = ?, payment_method = ?, total_amount = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, isNewOrder ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
            if (order.getOrderTime() == null && isNewOrder) order.setOrderTime(LocalDateTime.now());
            pstmt.setTimestamp(1, Timestamp.valueOf(order.getOrderTime()));

            if (order.getTable() != null) pstmt.setInt(2, order.getTable().getId());
            else if (!isNewOrder && order.getTableId() != null) pstmt.setInt(2, order.getTableId());
            else pstmt.setNull(2, java.sql.Types.INTEGER);

            if (order.getClient() != null) pstmt.setInt(3, order.getClient().getId());
            else if (!isNewOrder && order.getClientId() != null) pstmt.setInt(3, order.getClientId());
            else pstmt.setNull(3, java.sql.Types.INTEGER);

            if (order.getEmployee() != null) pstmt.setInt(4, order.getEmployee().getId());
            else if (!isNewOrder && order.getEmployeeId() != null) pstmt.setInt(4, order.getEmployeeId());
            else pstmt.setNull(4, java.sql.Types.INTEGER);

            pstmt.setString(5, order.getPaymentMethod());
            pstmt.setDouble(6, order.getTotalAmount());

            if (!isNewOrder) pstmt.setInt(7, order.getId());

            pstmt.executeUpdate();

            if (isNewOrder) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) order.setId(generatedKeys.getInt(1));
                    else throw new SQLException("Помилка при створенні замовлення, ID не отримано.");
                }
            }
        }
        return order;
    }

    public Order createOrder(Order order, double appliedBonusDiscountIfAny) throws SQLException {
        return saveOrUpdateOrderCore(order, appliedBonusDiscountIfAny, true);
    }

    public void updateOrder(Order order, double preCalculatedBonusDiscount) throws SQLException {
        saveOrUpdateOrderCore(order, preCalculatedBonusDiscount, false);
    }

    public Order getOrderById(int id) throws SQLException {
        String query = "SELECT id, order_time, table_id, client_id, employee_id, payment_method, total_amount FROM orders WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return createOrderFromResultSet(rs);
            }
        }
        return null;
    }

    public void deleteOrder(int id) throws SQLException {
        String deleteOrderItemsSql = "DELETE FROM order_items WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteOrderItemsSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
        String deleteOrderSql = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteOrderSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT id, order_time, table_id, client_id, employee_id," +
                " payment_method, total_amount FROM orders ORDER BY order_time DESC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) orders.add(createOrderFromResultSet(rs));
            }
        }
        return orders;
    }

    public List<Table> getAllTables() throws SQLException {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT id, table_number, capacity FROM tables";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) tables.add(new Table(rs.getInt("id"),
                        rs.getString("table_number"), rs.getInt("capacity")));
            }
        }
        return tables;
    }
    public Client addClient(Client client) throws SQLException {
        String sql = "INSERT INTO clients (first_name, last_name," +
                " phone_number, email, loyalty_points) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, client.getFirstName());
            pstmt.setString(2, client.getLastName());
            pstmt.setString(3, client.getPhoneNumber());
            pstmt.setString(4, client.getEmail());
            pstmt.setDouble(5, Math.max(0, client.getLoyaltyPoints()));
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) client.setId(generatedKeys.getInt(1));
                else throw new SQLException("Помилка при створенні клієнта, ID не отримано.");
            }
        }
        return client;
    }

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>(); // Перенесено сюди, щоб було в області видимості
        String query = "SELECT id, first_name, last_name, phone_number, email, loyalty_points FROM clients";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(new Client(rs.getInt("id"),
                            rs.getString("first_name"), rs.getString("last_name"),
                            rs.getString("phone_number"),
                            rs.getString("email"), rs.getDouble("loyalty_points")));
                }
            } // Закриваюча дужка для ResultSet
        } // Закриваюча дужка для PreparedStatement
        return clients; // Тепер clients знаходиться в області видимості
    }
    public List<Employee> getAllEmployees(String positionName) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT e.id, e.first_name," +
                " e.last_name, e.position_id, e.password FROM employees e");
        if (positionName != null && !positionName.trim().isEmpty()) {
            queryBuilder.append(" JOIN positions p ON e.position_id = p.id WHERE " +
                    "p.position_name = ?");
        }
        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            if (positionName != null && !positionName.trim().isEmpty()) stmt.setString(1, positionName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                            getPositionById(rs.getInt("position_id")), rs.getString("password")));
                }
            }
        }
        return employees;
    }
    public List<Employee> getAllEmployees() throws SQLException { return getAllEmployees(null); }


    public OrderItem createOrderItem(OrderItem orderItem) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price_at_order) VALUES (?, ?, ?, ?)";
        int newOrderItemId = -1; // Ініціалізуємо для зберігання згенерованого ID

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, orderItem.getOrderId());
            pstmt.setInt(2, orderItem.getMenuItemId());
            pstmt.setInt(3, orderItem.getQuantity());
            pstmt.setDouble(4, orderItem.getPriceAtOrder());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newOrderItemId = generatedKeys.getInt(1);
                    orderItem.setId(newOrderItemId); // Встановлюємо ID для поверненого об'єкта
                } else {
                    throw new SQLException("Помилка при створенні позиції замовлення, ID не отримано.");
                }
            }
        }

        // логіка створення завдання для кухні
        if (newOrderItemId != -1) {
            // Отримуємо орієнтовний час приготування для цієї страви з menu_cooking_times
            int estimatedCookingTime = kitchenService.getEstimatedCookingTimeForMenuItem(orderItem.getMenuItemId());
            if (estimatedCookingTime == 0) {
                // Якщо для цієї страви час приготування не встановлено, використовуємо значення за замовчуванням
                System.out.println("Попередження: Час приготування для страви ID " + orderItem.getMenuItemId() + " не знайдено. Встановлюємо 20 хвилин за замовчуванням.");
                estimatedCookingTime = 20; // Це значення може бути конфігурованим
            }

            // --- ВИПРАВЛЕНО ТУТ ---
            // newOrderItemId тепер є orderItemId
            // priority встановлено на 1 як значення за замовчуванням
            boolean taskCreated = kitchenService.createKitchenTask(newOrderItemId, 1, estimatedCookingTime);

            if (!taskCreated) {
                System.err.println("Помилка: Не вдалося створити кухонне завдання для order_item ID: " + newOrderItemId);
                // Тут може знадобитися додаткова логіка:
                // - Логування більш серйозної помилки
                // - Можливо, відкат транзакції для всього замовлення
                // - Повідомлення користувачу
                throw new SQLException("Не вдалося створити кухонне завдання для позиції замовлення.");
            }
        }

        return orderItem;
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) throws SQLException {
        List<OrderItem> orderItems = new ArrayList<>();
        String query = "SELECT id, order_id, menu_item_id, quantity, price_at_order FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) orderItems.add(createOrderItemFromResultSet(rs));
            }
        }
        return orderItems;
    }
    public void updateOrderItem(OrderItem orderItem) throws SQLException {
        String sql = "UPDATE order_items SET order_id = ?, menu_item_id = ?," +
                " quantity = ?, price_at_order = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderItem.getOrderId());
            pstmt.setInt(2, orderItem.getMenuItemId());
            pstmt.setInt(3, orderItem.getQuantity());
            pstmt.setDouble(4, orderItem.getPriceAtOrder());
            pstmt.setInt(5, orderItem.getId());
            pstmt.executeUpdate();
        }
    }
    public void deleteOrderItem(int id) throws SQLException {
        String sql = "DELETE FROM order_items WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    public void insertInitialTables(int numberOfTables) throws SQLException {
        String sql = "INSERT INTO tables (table_number, capacity) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 1; i <= numberOfTables; i++) {
                pstmt.setString(1, "T" + String.format("%02d", i));
                pstmt.setInt(2, 4);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println(numberOfTables + " початкових столиків додано.");
        }
    }
    public Table getTableByNumber(String tableNumber) throws SQLException {
        String query = "SELECT id, table_number, capacity FROM tables WHERE table_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tableNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Table(
                        rs.getInt("id"),
                        rs.getString("table_number"),
                        rs.getInt("capacity")
                );
            }
        }
        return null;
    }
}