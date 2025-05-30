package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.model.Order;
import org.example.restaurant_management_system.model.OrderItem;
import org.example.restaurant_management_system.model.MenuItem;
import org.example.restaurant_management_system.model.Client;
import org.example.restaurant_management_system.model.Table;
import org.example.restaurant_management_system.model.Employee;
import org.example.restaurant_management_system.model.Position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;


public class OrderServiceTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private KitchenService mockKitchenService;

    private OrderService orderService;

    @Mock
    private PreparedStatement mockMenuItemStmt;
    @Mock
    private ResultSet mockMenuItemRs;

    @InjectMocks
    private OrderService OrderService;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(mockConnection);

        try {
            java.lang.reflect.Field kitchenServiceField = OrderService.class.getDeclaredField("kitchenService");
            kitchenServiceField.setAccessible(true);
            kitchenServiceField.set(orderService, mockKitchenService);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Не вдалося ввести мок-KitchenService за допомогою рефлексії: " + e.getMessage());
        }

        when(mockConnection.prepareStatement(anyString())).thenAnswer(new Answer<PreparedStatement>() {
            @Override
            public PreparedStatement answer(InvocationOnMock invocation) throws Throwable {
                PreparedStatement ps = mock(PreparedStatement.class);
                when(ps.executeQuery()).thenReturn(mockResultSet);
                when(ps.executeUpdate()).thenReturn(1);
                when(ps.getGeneratedKeys()).thenReturn(mock(ResultSet.class));
                return ps;
            }
        });

        when(mockConnection.prepareStatement(anyString(), anyInt())).
                thenAnswer(new Answer<PreparedStatement>() {
            @Override
            public PreparedStatement answer(InvocationOnMock invocation) throws Throwable {

                PreparedStatement ps = mock(PreparedStatement.class);
                when(ps.executeQuery()).thenReturn(mockResultSet);
                when(ps.executeUpdate()).thenReturn(1);
                ResultSet generatedKeysRs = mock(ResultSet.class);
                when(generatedKeysRs.next()).thenReturn(false);
                when(ps.getGeneratedKeys()).thenReturn(generatedKeysRs);
                return ps;
            }
        });

        doNothing().when(mockConnection).setAutoCommit(anyBoolean());
        doNothing().when(mockConnection).commit();
        doNothing().when(mockConnection).rollback();
    }

    private void mockPreparedStatementForSql(String sqlPart,
                                             PreparedStatement mockStmtToReturn) throws SQLException {

        when(mockConnection.prepareStatement(argThat(sql -> sql != null && sql.contains(sqlPart))))
                .thenReturn(mockStmtToReturn);
    }

    private void mockMenuItemLookup(MenuItem menuItem) throws SQLException {
        reset(mockMenuItemStmt, mockMenuItemRs);

        mockPreparedStatementForSql("FROM menu_items WHERE id = ?", mockMenuItemStmt);
        when(mockMenuItemStmt.executeQuery()).thenReturn(mockMenuItemRs);

        if (menuItem != null) {
            when(mockMenuItemRs.next()).thenReturn(true, false);
            when(mockMenuItemRs.getInt("id")).thenReturn(menuItem.getId());
            when(mockMenuItemRs.getString("name")).thenReturn(menuItem.getName());
            when(mockMenuItemRs.getInt("category_id")).thenReturn(menuItem.getCategoryId());
            when(mockMenuItemRs.getDouble("price")).thenReturn(menuItem.getPrice());
            when(mockMenuItemRs.getBoolean("vegetarian")).thenReturn(menuItem.isVegetarian());
            when(mockMenuItemRs.getBoolean("allergen")).thenReturn(menuItem.isAllergen());
            when(mockMenuItemRs.getBoolean("gluten_free")).thenReturn(menuItem.isGlutenFree());
        } else {
            when(mockMenuItemRs.next()).thenReturn(false);
        }
    }


    private void mockPreparedStatementForSql(String sqlContains, PreparedStatement specificMock,
                                             int returnGeneratedKeys) throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null && sql.contains(sqlContains)),
                eq(returnGeneratedKeys)))
                .thenReturn(specificMock);
    }

    private void mockClientLookup(Client clientToReturn) throws SQLException {
        PreparedStatement mockGetClientStmt = mock(PreparedStatement.class);
        ResultSet mockGetClientRs = mock(ResultSet.class);
        mockPreparedStatementForSql("FROM clients WHERE id = ?", mockGetClientStmt);
        when(mockGetClientStmt.executeQuery()).thenReturn(mockGetClientRs);

        if (clientToReturn != null) {
            when(mockGetClientRs.next()).thenReturn(true, false);
            when(mockGetClientRs.getInt("id")).thenReturn(clientToReturn.getId());
            when(mockGetClientRs.getDouble("loyalty_points")).
                    thenReturn(clientToReturn.getLoyaltyPoints());

            when(mockGetClientRs.getString("first_name")).thenReturn(clientToReturn.getFirstName());
            when(mockGetClientRs.getString("last_name")).thenReturn(clientToReturn.getLastName());
            when(mockGetClientRs.getString("phone_number")).thenReturn(clientToReturn.getPhoneNumber());
            when(mockGetClientRs.getString("email")).thenReturn(clientToReturn.getEmail());
        } else {
            when(mockGetClientRs.next()).thenReturn(false);
        }
    }

    private void mockTableLookup(Table tableToReturn) throws SQLException {
        PreparedStatement mockGetTableStmt = mock(PreparedStatement.class);
        ResultSet mockGetTableRs = mock(ResultSet.class);
        mockPreparedStatementForSql("FROM tables WHERE id = ?", mockGetTableStmt);
        when(mockGetTableStmt.executeQuery()).thenReturn(mockGetTableRs);
        if (tableToReturn != null) {
            when(mockGetTableRs.next()).thenReturn(true);
            when(mockGetTableRs.getInt("id")).thenReturn(tableToReturn.getId());
            when(mockGetTableRs.getString("table_number")).thenReturn(tableToReturn.getTableNumber());
            when(mockGetTableRs.getInt("capacity")).thenReturn(tableToReturn.getCapacity());
        } else {
            when(mockGetTableRs.next()).thenReturn(false);
        }
    }

    private void mockEmployeeLookup(Employee employeeToReturn) throws SQLException {
        PreparedStatement mockGetEmployeeStmt = mock(PreparedStatement.class);
        ResultSet mockGetEmployeeRs = mock(ResultSet.class);
        mockPreparedStatementForSql("FROM employees WHERE id = ?", mockGetEmployeeStmt);
        when(mockGetEmployeeStmt.executeQuery()).thenReturn(mockGetEmployeeRs);

        if (employeeToReturn != null) {
            when(mockGetEmployeeRs.next()).thenReturn(true);
            when(mockGetEmployeeRs.getInt("id")).thenReturn(employeeToReturn.getId());
            when(mockGetEmployeeRs.getString("first_name")).thenReturn(employeeToReturn.getFirstName());
            when(mockGetEmployeeRs.getString("last_name")).thenReturn(employeeToReturn.getLastName());
            when(mockGetEmployeeRs.getInt("position_id")).
                    thenReturn(employeeToReturn.getPosition().getId());

            PreparedStatement mockGetPositionStmt = mock(PreparedStatement.class);
            ResultSet mockGetPositionRs = mock(ResultSet.class);
            mockPreparedStatementForSql("FROM positions WHERE id = ?", mockGetPositionStmt);
            when(mockGetPositionStmt.executeQuery()).thenReturn(mockGetPositionRs);
            when(mockGetPositionRs.next()).thenReturn(true);
            when(mockGetPositionRs.getInt("id")).thenReturn(employeeToReturn.getPosition().getId());
            when(mockGetPositionRs.getString("position_name")).
                    thenReturn(employeeToReturn.getPosition().getName());
        } else {
            when(mockGetEmployeeRs.next()).thenReturn(false);
        }
    }

    @Test
    void createOrder_Success_NoClientNoBonusPayment() throws SQLException {
        Order newOrder = new Order(0, LocalDateTime.now(), 1,
                0, 1, "Готівка", 100.0);
        int expectedOrderId = 101;

        PreparedStatement mockOrderInsertStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeysRs = mock(ResultSet.class);
        mockPreparedStatementForSql("INSERT INTO orders",
                mockOrderInsertStmt, Statement.RETURN_GENERATED_KEYS);

        when(mockOrderInsertStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeysRs);
        when(mockGeneratedKeysRs.next()).thenReturn(true);
        when(mockGeneratedKeysRs.getInt(1)).thenReturn(expectedOrderId);
        when(mockOrderInsertStmt.executeUpdate()).thenReturn(1);

        Order createdOrder = orderService.createOrder(newOrder, 0.0);

        assertNotNull(createdOrder);
        assertEquals(expectedOrderId, createdOrder.getId());
        assertEquals(100.0, createdOrder.getTotalAmount());
        verify(mockOrderInsertStmt).setNull(eq(3), eq(java.sql.Types.INTEGER));
        verify(mockOrderInsertStmt).executeUpdate();
    }

    @Test
    void createOrder_Success_BasicOrderNoClientNoBonus() throws SQLException {
        Table table = new Table(1, "Table 1", 4);

        Employee employee = new Employee(1, "Worker",
                "One", new Position(1, "Waiter"), "password");

        Order newOrder = new Order(0, LocalDateTime.now(), table.getId(),
                0, employee.getId(), "Готівка", 100.0);
        newOrder.setTable(table);
        newOrder.setEmployee(employee);

        int expectedOrderId = 10;

        PreparedStatement mockOrderInsertStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeysRs = mock(ResultSet.class);

        String orderInsertSql = "INSERT INTO orders (order_time, table_id, " +
                "client_id, employee_id, payment_method, total_amount) VALUES (?, ?, ?, ?, ?, ?)";
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.matches(Pattern.quote(orderInsertSql))), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockOrderInsertStmt);

        when(mockOrderInsertStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeysRs);
        when(mockGeneratedKeysRs.next()).thenReturn(true);
        when(mockGeneratedKeysRs.getInt(1)).thenReturn(expectedOrderId);
        when(mockOrderInsertStmt.executeUpdate()).thenReturn(1);

        Order createdOrder = orderService.createOrder(newOrder,
                0);

        assertNotNull(createdOrder);
        assertEquals(expectedOrderId, createdOrder.getId());
        assertEquals(100.0, createdOrder.getTotalAmount());
        assertNull(createdOrder.getClient());

        InOrder inOrder = inOrder(mockOrderInsertStmt);

        inOrder.verify(mockOrderInsertStmt).setTimestamp(eq(1), any(Timestamp.class));
        inOrder.verify(mockOrderInsertStmt).setInt(eq(2), eq(table.getId()));
        inOrder.verify(mockOrderInsertStmt).setNull(eq(3), eq(java.sql.Types.INTEGER));
        inOrder.verify(mockOrderInsertStmt).setInt(eq(4), eq(employee.getId()));
        inOrder.verify(mockOrderInsertStmt).setString(eq(5), eq("Готівка"));
        inOrder.verify(mockOrderInsertStmt).setDouble(eq(6), eq(100.0));

        inOrder.verify(mockOrderInsertStmt).executeUpdate();
    }
    @Test
    void createOrder_SQLExceptionOnInsert() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("INSERT INTO orders")), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("DB Error"));

        assertThrows(SQLException.class, () -> orderService.createOrder(new Order(0,
                LocalDateTime.now(), 1, 0, 1, "Готівка",
                100.0), 0.0));
    }



    @Test
    void updateOrder_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(
                argThat(sql -> sql != null && sql.contains("UPDATE orders SET")),
                eq(Statement.NO_GENERATED_KEYS)))
                .thenThrow(new SQLException("DB Error"));

        assertThrows(SQLException.class, () -> orderService.updateOrder(new Order(1,
                LocalDateTime.now(), 1, 0, 1, "Cash",
                100.0), 0.0));
    }

    @Test
    void getOrderById_Success() throws SQLException {
        int orderId = 1;

        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(argThat(sql -> sql != null && sql.contains("SELECT id," +
                " order_time, table_id, client_id, employee_id, payment_method," +
                " total_amount FROM orders WHERE id = ?"))))
                .thenReturn(mockPreparedStatement);

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(orderId);
        when(mockResultSet.getTimestamp("order_time")).
                thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        when(mockResultSet.getObject("table_id", Integer.class)).thenReturn(10);
        when(mockResultSet.getObject("client_id", Integer.class)).thenReturn(20);
        when(mockResultSet.getObject("employee_id", Integer.class)).thenReturn(30);
        when(mockResultSet.getString("payment_method")).thenReturn("Картка");
        when(mockResultSet.getDouble("total_amount")).thenReturn(150.0);

        mockTableLookup(new Table(10, "T10", 4));
        mockClientLookup(new Client(20, "John", "Doe",
                "123", "j@d.com", 10.0));

        mockEmployeeLookup(new Employee(30, "Jane", "Smith",
                new Position(1, "Cashier"), "pass"));

        Order order = orderService.getOrderById(orderId);

        assertNotNull(order);
        assertEquals(orderId, order.getId());
        assertNotNull(order.getTable());
        assertNotNull(order.getClient());
        assertNotNull(order.getEmployee());

        verify(mockConnection).prepareStatement(argThat(sql -> sql != null
                && sql.contains("SELECT id, order_time, table_id, client_id, " +
                "employee_id, payment_method, total_amount FROM orders WHERE id = ?")));

        verify(mockPreparedStatement).setInt(1, orderId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet, atLeastOnce()).getInt(anyString());
    }


    @Test
    void getOrderById_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("SELECT id, order_time, table_id, client_id," +
                " employee_id, payment_method, total_amount FROM orders WHERE id = ?")))).
                thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, () -> orderService.getOrderById(1));
    }
    @Test
    void deleteOrder_Success() throws SQLException {
        int orderId = 1;
        PreparedStatement mockDeleteOrderItemsStmt = mock(PreparedStatement.class);
        PreparedStatement mockDeleteOrderStmt = mock(PreparedStatement.class);

        mockPreparedStatementForSql("DELETE FROM order_items WHERE order_id = ?",
                mockDeleteOrderItemsStmt);

        when(mockDeleteOrderItemsStmt.executeUpdate()).thenReturn(5);

        mockPreparedStatementForSql("DELETE FROM orders WHERE id = ?",
                mockDeleteOrderStmt);

        when(mockDeleteOrderStmt.executeUpdate()).thenReturn(1);

        orderService.deleteOrder(orderId);

        InOrder inOrder = inOrder(mockDeleteOrderItemsStmt, mockDeleteOrderStmt);
        inOrder.verify(mockDeleteOrderItemsStmt).setInt(1, orderId);
        inOrder.verify(mockDeleteOrderItemsStmt).executeUpdate();
        inOrder.verify(mockDeleteOrderStmt).setInt(1, orderId);
        inOrder.verify(mockDeleteOrderStmt).executeUpdate();
    }

    @Test
    void deleteOrder_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, () -> orderService.deleteOrder(1));
    }


    @Test
    void getAllOrders_SQLException() throws SQLException {

        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("SELECT id, order_time, table_id, client_id," +
                " employee_id, payment_method, total_amount FROM orders")))).
                thenThrow(new SQLException("DB Error"));

        assertThrows(SQLException.class, () -> orderService.getAllOrders());
    }

    @Test
    void getAllTables_SuccessAndEmptyList() throws SQLException {

        reset(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("table_number")).thenReturn("T01", "T02");
        when(mockResultSet.getInt("capacity")).thenReturn(4, 6);

        List<Table> tables = orderService.getAllTables();
        assertNotNull(tables);
        assertEquals(2, tables.size());
        assertEquals("T01", tables.get(0).getTableNumber());
        verify(mockConnection, times(1)).
                prepareStatement(argThat(sql -> sql != null && sql.contains("SELECT id," +
                        " table_number, capacity FROM tables")));

        reset(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<Table> emptyTables = orderService.getAllTables();
        assertNotNull(emptyTables);
        assertTrue(emptyTables.isEmpty());
        verify(mockConnection, times(2)).
                prepareStatement(argThat(sql -> sql != null && sql.contains("SELECT id," +
                        " table_number, capacity FROM tables")));
    }

    @Test
    void getAllTables_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null && sql.contains("SELECT id," +
                " table_number, capacity FROM tables")))).thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, () -> orderService.getAllTables());
    }

    @Test
    void addClient_Success() throws SQLException {
        Client newClient = new Client(0, "Новий", "Клієнт",
                "0987654321", "new@client.com", 10.5);
        PreparedStatement mockAddClientStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeysRs = mock(ResultSet.class);

        mockPreparedStatementForSql("INSERT INTO clients", mockAddClientStmt,
                Statement.RETURN_GENERATED_KEYS);

        when(mockAddClientStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeysRs);
        when(mockGeneratedKeysRs.next()).thenReturn(true);
        when(mockGeneratedKeysRs.getInt(1)).thenReturn(501);
        when(mockAddClientStmt.executeUpdate()).thenReturn(1);

        Client addedClient = orderService.addClient(newClient);
        assertNotNull(addedClient);
        assertEquals(501, addedClient.getId());
        verify(mockAddClientStmt).executeUpdate();
    }

    @Test
    void addClient_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("INSERT INTO clients")), eq(Statement.RETURN_GENERATED_KEYS)))

                .thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, () -> orderService.addClient(new Client(0,
                "F", "L", "P", "E", 0.0)));
    }

    @Test
    void getAllClients_SuccessAndEmptyList() throws SQLException {

        reset(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("first_name")).thenReturn("Олег", "Марія");
        when(mockResultSet.getDouble("loyalty_points")).thenReturn(5.0, 15.0);

        List<Client> clients = orderService.getAllClients();
        assertNotNull(clients);
        assertEquals(2, clients.size());
        assertEquals("Олег", clients.get(0).getFirstName());
        verify(mockConnection, times(1)).
                prepareStatement(argThat(sql -> sql != null && sql.contains("SELECT id, " +
                        "first_name, last_name, phone_number, email, loyalty_points FROM clients")));


        reset(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        List<Client> emptyClients = orderService.getAllClients();
        assertNotNull(emptyClients);
        assertTrue(emptyClients.isEmpty());
        verify(mockConnection, times(2)).
                prepareStatement(argThat(sql -> sql != null
                        && sql.contains("SELECT id, first_name, last_name, " +
                        "phone_number, email, loyalty_points FROM clients")));
    }

    @Test
    void getAllClients_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("SELECT id, first_name, last_name, phone_number," +
                " email, loyalty_points FROM clients")))).thenThrow(new SQLException("DB Error"));

        assertThrows(SQLException.class, () -> orderService.getAllClients());
    }

    @Test
    void createOrderItem_Success_WithKitchenTask() throws SQLException {
        OrderItem newOrderItem = new OrderItem(0, 1, 101,
                2, 50.0);
        int expectedOrderItemId = 201;

        PreparedStatement mockOrderItemInsertStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeysRs = mock(ResultSet.class);
        mockPreparedStatementForSql("INSERT INTO order_items",
                mockOrderItemInsertStmt, Statement.RETURN_GENERATED_KEYS);

        when(mockOrderItemInsertStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeysRs);
        when(mockGeneratedKeysRs.next()).thenReturn(true);
        when(mockGeneratedKeysRs.getInt(1)).thenReturn(expectedOrderItemId);
        when(mockOrderItemInsertStmt.executeUpdate()).thenReturn(1);

        when(mockKitchenService.getEstimatedCookingTimeForMenuItem(newOrderItem.getMenuItemId())).
                thenReturn(30);

        when(mockKitchenService.createKitchenTask(anyInt(), eq(1),
                eq(30))).thenReturn(true);

        OrderItem createdOrderItem = orderService.createOrderItem(newOrderItem);

        assertNotNull(createdOrderItem);
        assertEquals(expectedOrderItemId, createdOrderItem.getId());
        verify(mockKitchenService).getEstimatedCookingTimeForMenuItem(newOrderItem.getMenuItemId());

        verify(mockKitchenService).createKitchenTask(eq(expectedOrderItemId),
                eq(1), eq(30));
        verify(mockOrderItemInsertStmt).executeUpdate();
    }



    @Test
    void createOrderItem_SQLExceptionOnInsert() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("INSERT INTO order_items")), eq(Statement.RETURN_GENERATED_KEYS)))

                .thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, () -> orderService.createOrderItem(new OrderItem(0,
                1, 1, 1, 10.0)));
    }

    @Test
    void getOrderItemsByOrderId_SuccessAndEmptyList() throws SQLException {
        int orderId = 1;

        PreparedStatement mockGetOrderItemsStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("SELECT id, order_id," +
                " menu_item_id, quantity, price_at_order FROM order_items " +
                "WHERE order_id = ?"))))
                .thenReturn(mockGetOrderItemsStmt);
        when(mockGetOrderItemsStmt.executeQuery()).thenReturn(mockResultSet);


        reset(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getInt("order_id")).thenReturn(orderId, orderId);
        when(mockResultSet.getInt("menu_item_id")).thenReturn(101, 102);
        when(mockResultSet.getInt("quantity")).thenReturn(1, 2);
        when(mockResultSet.getDouble("price_at_order")).thenReturn(25.0,
                50.0);

        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("FROM menu_items WHERE id = ?"))))
                .thenReturn(mockMenuItemStmt);

        ResultSet localMockMenuItemRs101 = mock(ResultSet.class);
        when(localMockMenuItemRs101.next()).thenReturn(true, false);
        when(localMockMenuItemRs101.getInt("id")).thenReturn(101);
        when(localMockMenuItemRs101.getString("name")).thenReturn("Soup");
        when(localMockMenuItemRs101.getInt("category_id")).thenReturn(1);
        when(localMockMenuItemRs101.getDouble("price")).thenReturn(20.0);
        when(localMockMenuItemRs101.getBoolean("vegetarian")).thenReturn(false);
        when(localMockMenuItemRs101.getBoolean("allergen")).thenReturn(false);
        when(localMockMenuItemRs101.getBoolean("gluten_free")).thenReturn(false);

        ResultSet localMockMenuItemRs102 = mock(ResultSet.class);
        when(localMockMenuItemRs102.next()).thenReturn(true, false);
        when(localMockMenuItemRs102.getInt("id")).thenReturn(102);
        when(localMockMenuItemRs102.getString("name")).thenReturn("Salad");
        when(localMockMenuItemRs102.getInt("category_id")).thenReturn(2);
        when(localMockMenuItemRs102.getDouble("price")).thenReturn(40.0);
        when(localMockMenuItemRs102.getBoolean("vegetarian")).thenReturn(true);
        when(localMockMenuItemRs102.getBoolean("allergen")).thenReturn(false);
        when(localMockMenuItemRs102.getBoolean("gluten_free")).thenReturn(false);

        when(mockMenuItemStmt.executeQuery())
                .thenReturn(localMockMenuItemRs101)
                .thenReturn(localMockMenuItemRs102);

        List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);
        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals(101, items.get(0).getMenuItemId());
        assertNotNull(items.get(0).getMenuItem(), "MenuItem for item " +
                "101 should not be null");

        assertEquals("Soup", items.get(0).getMenuItem().getName());
        assertEquals(102, items.get(1).getMenuItemId());
        assertNotNull(items.get(1).getMenuItem(), "MenuItem for item 102" +
                " should not be null");
        assertEquals("Salad", items.get(1).getMenuItem().getName());

        verify(mockConnection, times(1)).
                prepareStatement(argThat(sql -> sql.contains("FROM order_items " +
                        "WHERE order_id = ?")));
        verify(mockGetOrderItemsStmt).setInt(1, orderId);
        verify(mockGetOrderItemsStmt).executeQuery();

        verify(mockConnection, atLeast(2)).
                prepareStatement(argThat(sql -> sql.contains("FROM menu_items WHERE id = ?")));
        verify(mockMenuItemStmt, times(2)).setInt(eq(1), anyInt());
        verify(mockMenuItemStmt, times(2)).executeQuery();

        reset(mockConnection, mockResultSet, mockGetOrderItemsStmt,
                mockMenuItemStmt, mockMenuItemRs);

        when(mockConnection.prepareStatement(anyString())).
                thenAnswer(invocation -> mock(PreparedStatement.class));


        PreparedStatement mockEmptyGetOrderItemsStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("FROM order_items WHERE order_id = ?"))))
                .thenReturn(mockEmptyGetOrderItemsStmt);
        when(mockEmptyGetOrderItemsStmt.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(false);

        mockMenuItemLookup(null);

        List<OrderItem> emptyItems = orderService.getOrderItemsByOrderId(orderId);
        assertNotNull(emptyItems);
        assertTrue(emptyItems.isEmpty());

        verify(mockConnection, atLeastOnce()).prepareStatement(argThat(sql
                -> sql.contains("FROM order_items WHERE order_id = ?")));
        verify(mockEmptyGetOrderItemsStmt).setInt(1, orderId);
        verify(mockEmptyGetOrderItemsStmt).executeQuery();

        verify(mockMenuItemStmt, atMostOnce()).executeQuery();
    }

    @Test
    void getOrderItemsByOrderId_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("SELECT id, order_id, menu_item_id, " +
                "quantity, price_at_order FROM order_items WHERE order_id = ?")))).
                thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, () -> orderService.getOrderItemsByOrderId(1));
    }

    @Test
    void updateOrderItem_Success() throws SQLException {
        OrderItem orderItem = new OrderItem(1, 101,
                201, 3, 150.0);
        PreparedStatement mockUpdateOrderItemStmt = mock(PreparedStatement.class);
        mockPreparedStatementForSql("UPDATE order_items SET order_id = ?," +
                " menu_item_id = ?, quantity = ?, price_at_order = ? WHERE id = ?",
                mockUpdateOrderItemStmt);
        when(mockUpdateOrderItemStmt.executeUpdate()).thenReturn(1);

        orderService.updateOrderItem(orderItem);
        verify(mockUpdateOrderItemStmt).executeUpdate();
    }

    @Test
    void updateOrderItem_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("UPDATE order_items SET")))).
                thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, () ->
                orderService.updateOrderItem(new OrderItem(1,
                        1, 1, 1, 1.0)));
    }

    @Test
    void deleteOrderItem_Success() throws SQLException {
        PreparedStatement mockDeleteOrderItemStmt = mock(PreparedStatement.class);
        mockPreparedStatementForSql("DELETE FROM order_items WHERE id = ?",
                mockDeleteOrderItemStmt);
        when(mockDeleteOrderItemStmt.executeUpdate()).thenReturn(1);

        orderService.deleteOrderItem(1);
        verify(mockDeleteOrderItemStmt).executeUpdate();
    }

    @Test
    void deleteOrderItem_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("DELETE FROM order_items WHERE id = ?")))).
                thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, () -> orderService.deleteOrderItem(1));
    }

    @Test
    void insertInitialTables_Success() throws SQLException {
        int numberOfTables = 3;
        PreparedStatement mockInsertTablesStmt = mock(PreparedStatement.class);
        mockPreparedStatementForSql("INSERT INTO " +
                "tables (table_number, capacity) VALUES (?, ?)", mockInsertTablesStmt);
        when(mockInsertTablesStmt.executeBatch()).thenReturn(new int[]{1, 1, 1});

        orderService.insertInitialTables(numberOfTables);
        verify(mockInsertTablesStmt, times(numberOfTables)).addBatch();
        verify(mockInsertTablesStmt).executeBatch();
    }

    @Test
    void insertInitialTables_SQLException() throws SQLException {
        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("INSERT INTO tables"))))
                .thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, ()
                -> orderService.insertInitialTables(3));
    }

    @Test
    void getTableByNumber_SuccessAndNotFound() throws SQLException {

        PreparedStatement mockGetTableStmt = mock(PreparedStatement.class);
        ResultSet mockGetTableRs = mock(ResultSet.class);
        mockPreparedStatementForSql("FROM tables WHERE table_number = ?",
                mockGetTableStmt);
        when(mockGetTableStmt.executeQuery()).thenReturn(mockGetTableRs);

        when(mockGetTableRs.next()).thenReturn(true, false);
        when(mockGetTableRs.getInt("id")).thenReturn(1);
        when(mockGetTableRs.getString("table_number")).thenReturn("T01");
        when(mockGetTableRs.getInt("capacity")).thenReturn(4);

        Table table = orderService.getTableByNumber("T01");
        assertNotNull(table);
        assertEquals("T01", table.getTableNumber());
        verify(mockGetTableStmt).setString(1, "T01");
        verify(mockGetTableStmt).executeQuery();

        reset(mockGetTableRs, mockGetTableStmt);

        mockPreparedStatementForSql("FROM tables WHERE table_number = ?",
                mockGetTableStmt);
        when(mockGetTableStmt.executeQuery()).thenReturn(mockGetTableRs);
        when(mockGetTableRs.next()).thenReturn(false);

        Table notFoundTable = orderService.getTableByNumber("T99");
        assertNull(notFoundTable);
        verify(mockGetTableStmt).setString(1, "T99");
        verify(mockGetTableStmt).executeQuery();
    }

    @Test
    void getTableByNumber_SQLException() throws SQLException {
        PreparedStatement mockGetTableStmt = mock(PreparedStatement.class);
        mockPreparedStatementForSql("FROM tables WHERE table_number = ?",
                mockGetTableStmt);

        when(mockGetTableStmt.executeQuery()).thenThrow(new SQLException("DB Error"));
        assertThrows(SQLException.class, ()
                -> orderService.getTableByNumber("T01"));
    }

    @Test
    void getAllEmployees_NoFilter_SuccessAndEmptyList() throws SQLException {

        reset(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("first_name")).thenReturn("Петро",
                "Олена");
        when(mockResultSet.getInt("position_id")).thenReturn(101, 102);

        PreparedStatement mockGetPositionStmt = mock(PreparedStatement.class);
        ResultSet mockGetPositionRs = mock(ResultSet.class);
        mockPreparedStatementForSql("FROM positions WHERE id = ?",
                mockGetPositionStmt);
        when(mockGetPositionStmt.executeQuery()).thenReturn(mockGetPositionRs);
        when(mockGetPositionRs.next()).thenReturn(true, true);
        when(mockGetPositionRs.getInt("id")).thenReturn(101, 102);
        when(mockGetPositionRs.getString("position_name")).
                thenReturn("Manager", "Chef");

        List<Employee> employees = orderService.getAllEmployees();
        assertNotNull(employees);
        assertEquals(2, employees.size());
        assertEquals("Manager", employees.get(0).getPosition().getName());
        verify(mockConnection, atLeast(1)).
                prepareStatement(argThat(sql -> sql != null &&
                        sql.contains("FROM employees")));

        reset(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Employee> emptyEmployees = orderService.getAllEmployees();
        assertNotNull(emptyEmployees);
        assertTrue(emptyEmployees.isEmpty());
        verify(mockConnection, atLeast(2)).prepareStatement(argThat(
                sql -> sql != null && sql.contains("FROM employees")));
    }

    @Test
    void getAllEmployees_WithFilter_SuccessAndEmptyList() throws SQLException {
        String positionNameFilter = "Manager";

        PreparedStatement mockGetAllEmployeesStmtForSuccess = mock(PreparedStatement.class);
        ResultSet mockGetAllEmployeesRsForSuccess = mock(ResultSet.class);

        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("FROM employees e JOIN positions p ON" +
                " e.position_id = p.id WHERE p.position_name = ?"))))
                .thenReturn(mockGetAllEmployeesStmtForSuccess);
        when(mockGetAllEmployeesStmtForSuccess.executeQuery()).
                thenReturn(mockGetAllEmployeesRsForSuccess);

        when(mockGetAllEmployeesRsForSuccess.next()).thenReturn(true, false);
        when(mockGetAllEmployeesRsForSuccess.getInt("id")).thenReturn(1);
        when(mockGetAllEmployeesRsForSuccess.getString("first_name")).
                thenReturn("Петро");

        when(mockGetAllEmployeesRsForSuccess.getInt("position_id")).
                thenReturn(101);

        PreparedStatement mockGetPositionStmtForSuccess = mock(PreparedStatement.class);
        ResultSet mockGetPositionRsForSuccess = mock(ResultSet.class);

        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("FROM positions WHERE id = ?"))))
                .thenReturn(mockGetPositionStmtForSuccess);

        when(mockGetPositionStmtForSuccess.executeQuery()).
                thenReturn(mockGetPositionRsForSuccess);

        when(mockGetPositionRsForSuccess.next()).thenReturn(true);
        when(mockGetPositionRsForSuccess.getInt("id")).thenReturn(101);
        when(mockGetPositionRsForSuccess.getString("position_name")).
                thenReturn("Manager");

        List<Employee> employees = orderService.getAllEmployees(positionNameFilter);
        assertNotNull(employees);
        assertEquals(1, employees.size());
        assertEquals("Manager", employees.get(0).getPosition().getName());

        verify(mockGetAllEmployeesStmtForSuccess).setString(1,
                positionNameFilter);
        verify(mockGetAllEmployeesStmtForSuccess).executeQuery();
        verify(mockGetPositionStmtForSuccess).setInt(1, 101);
        verify(mockGetPositionStmtForSuccess).executeQuery();

        reset(mockConnection);

        PreparedStatement mockGetAllEmployeesStmtForEmpty = mock(PreparedStatement.class);
        ResultSet mockGetAllEmployeesRsForEmpty = mock(ResultSet.class);

        when(mockConnection.prepareStatement(argThat(sql -> sql != null
                && sql.contains("FROM employees e JOIN positions p " +
                "ON e.position_id = p.id WHERE p.position_name = ?"))))
                .thenReturn(mockGetAllEmployeesStmtForEmpty);
        when(mockGetAllEmployeesStmtForEmpty.executeQuery()).
                thenReturn(mockGetAllEmployeesRsForEmpty);

        when(mockGetAllEmployeesRsForEmpty.next()).thenReturn(false);

        List<Employee> emptyEmployees = orderService.getAllEmployees("NonExistent");
        assertNotNull(emptyEmployees);
        assertTrue(emptyEmployees.isEmpty());

        verify(mockGetAllEmployeesStmtForEmpty).setString(1, "NonExistent");
        verify(mockGetAllEmployeesStmtForEmpty).executeQuery();

        verify(mockConnection, never()).prepareStatement(argThat(sql ->
                sql != null && sql.contains("FROM positions WHERE id = ?")));
    }
}