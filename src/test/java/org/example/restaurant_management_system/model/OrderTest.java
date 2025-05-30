package org.example.restaurant_management_system.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderTest {

    private Table mockTable;
    private Client mockClient;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockTable = mock(Table.class);
        when(mockTable.getId()).thenReturn(10);

        mockClient = mock(Client.class);
        when(mockClient.getId()).thenReturn(20);

        mockEmployee = mock(Employee.class);
        when(mockEmployee.getId()).thenReturn(30);
    }

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        Order order = new Order();

        assertEquals(0, order.getId());
        assertNotNull(order.getOrderTime(), "OrderTime має бути встановлено поточним часом.");
        assertNull(order.getTableId(), "TableId має бути null (геттер повертає null для 0).");
        assertEquals(0, order.tableIdProperty().get(),
                "Внутрішнє tableIdProperty має бути 0.");

        assertNull(order.getTable());
        assertNull(order.getClientId(), "ClientId має бути null.");
        assertEquals(0, order.clientIdProperty().get(),
                "Внутрішнє clientIdProperty має бути 0.");

        assertNull(order.getClient());
        assertNull(order.getEmployeeId(), "EmployeeId має бути null.");
        assertEquals(0, order.employeeIdProperty().get(),
                "Внутрішнє employeeIdProperty має бути 0.");

        assertNull(order.getEmployee());
        assertNull(order.getPaymentMethod(), "PaymentMethod має бути null.");
        assertEquals(0.0, order.getTotalAmount(), 0.001);

        assertNotNull(order.idProperty());
        assertNotNull(order.orderTimeProperty());
        assertNotNull(order.tableIdProperty());
        assertNotNull(order.tableProperty());
        assertNotNull(order.clientIdProperty());
        assertNotNull(order.clientProperty());
        assertNotNull(order.employeeIdProperty());
        assertNotNull(order.employeeProperty());
        assertNotNull(order.paymentMethodProperty());
        assertNotNull(order.totalAmountProperty());
    }

    @Test
    @DisplayName("Test constructor for DB load (with IDs)")
    void constructorForDbLoad_WithIds_SetsProperties() {
        int id = 1;
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        Integer tableId = 11;
        Integer clientId = 22;
        Integer employeeId = 33;
        String payment = "Картка";
        double amount = 150.0;

        Order order = new Order(id, time, tableId, clientId, employeeId, payment, amount);

        assertEquals(id, order.getId());
        assertEquals(time, order.getOrderTime());
        assertEquals(tableId, order.getTableId());
        assertNull(order.getTable());
        assertEquals(clientId, order.getClientId());
        assertNull(order.getClient());
        assertEquals(employeeId, order.getEmployeeId());
        assertNull(order.getEmployee());
        assertEquals(payment, order.getPaymentMethod());
        assertEquals(amount, order.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("Test constructor for DB load with null IDs")
    void constructorForDbLoad_WithNullIds_SetsIdsToZeroAndGettersReturnNull() {
        Order order = new Order(1, LocalDateTime.now(), (Integer) null,
                (Integer) null, (Integer) null, "Готівка", 100.0);

        assertNull(order.getTableId());
        assertEquals(0, order.tableIdProperty().get());
        assertNull(order.getClientId());
        assertEquals(0, order.clientIdProperty().get());
        assertNull(order.getEmployeeId());
        assertEquals(0, order.employeeIdProperty().get());
    }


    @Test
    @DisplayName("Test constructor with full objects and ID")
    void constructorWithFullObjectsAndId_SetsProperties() {
        int id = 2;
        LocalDateTime time = LocalDateTime.now();
        String payment = "Бонуси";
        double amount = 200.0;

        Order order = new Order(id, time, mockTable, mockClient,
                mockEmployee, payment, amount);

        assertEquals(id, order.getId());
        assertEquals(time, order.getOrderTime());
        assertSame(mockTable, order.getTable());
        assertEquals(mockTable.getId(), order.getTableId());
        assertSame(mockClient, order.getClient());
        assertEquals(mockClient.getId(), order.getClientId());
        assertSame(mockEmployee, order.getEmployee());
        assertEquals(mockEmployee.getId(), order.getEmployeeId());
        assertEquals(payment, order.getPaymentMethod());
        assertEquals(amount, order.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("Test constructor with full objects and ID handles null associations")
    void constructorWithFullObjectsAndId_HandlesNullAssociations() {
        Order order = new Order(1, LocalDateTime.now(), (Table) null,
                (Client) null, (Employee) null, "Г", 50.0);

        assertNull(order.getTable()); assertNull(order.getTableId());
        assertNull(order.getClient()); assertNull(order.getClientId());
        assertNull(order.getEmployee()); assertNull(order.getEmployeeId());
    }


    @Test
    @DisplayName("Test constructor for new record (without ID, with objects)")
    void constructorForNewRecord_WithObjects_SetsProperties() {
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        String payment = "Термінал";
        double amount = 75.50;

        Order order = new Order(time, mockTable, mockClient, mockEmployee, payment, amount);

        assertEquals(0, order.getId());
        assertEquals(time, order.getOrderTime());
        assertSame(mockTable, order.getTable());
        assertEquals(mockTable.getId(), order.getTableId());
        assertSame(mockClient, order.getClient());
        assertEquals(mockClient.getId(), order.getClientId());
        assertSame(mockEmployee, order.getEmployee());
        assertEquals(mockEmployee.getId(), order.getEmployeeId());
    }

    @Test
    @DisplayName("Test constructor for new record handles null associations")
    void constructorForNewRecord_HandlesNullAssociations() {
        Order order = new Order(LocalDateTime.now(), (Table) null, (Client) null,
                (Employee) null, "Г", 50.0);

        assertNull(order.getTable()); assertNull(order.getTableId());
        assertNull(order.getClient()); assertNull(order.getClientId());
        assertNull(order.getEmployee()); assertNull(order.getEmployeeId());
    }


    @Test
    @DisplayName("Test setTable updates table and tableId")
    void setTable_UpdatesTableAndTableId() {
        Order order = new Order();
        order.setTable(mockTable);

        assertSame(mockTable, order.getTable());
        assertEquals(mockTable.getId(), order.getTableId());
    }

    @Test
    @DisplayName("Test setTable with null updates table to null and tableId to 0 (internal)," +
            " getter returns null")

    void setTable_WithNull_UpdatesTableAndTableIdToZero() {
        Order order = new Order(LocalDateTime.now(), mockTable, (Client) null,
                (Employee) null, "Г", 0.0);

        assertEquals(10, (int) order.getTableId());

        order.setTable(null);

        assertNull(order.getTable());
        assertNull(order.getTableId(), "getTableId() має повернути null," +
                " коли внутрішній tableId 0.");

        assertEquals(0, order.tableIdProperty().get(),
                "Внутрішнє tableIdProperty має бути 0.");
    }

    @Test
    @DisplayName("Test setTableId with null sets internal ID to 0, getter returns null")
    void setTableId_WithNull_SetsInternalIdToZero() {
        Order order = new Order();
        order.setTableId(15);
        assertEquals(15, (int)order.getTableId());

        order.setTableId(null);
        assertNull(order.getTableId(), "getTableId() має повернути null.");
        assertEquals(0, order.tableIdProperty().get(),
                "Внутрішнє tableIdProperty має бути 0.");
    }

    @Test
    @DisplayName("Test setClient updates client and clientId")
    void setClient_UpdatesClientAndClientId() {
        Order order = new Order();
        order.setClient(mockClient);
        assertSame(mockClient, order.getClient());
        assertEquals(mockClient.getId(), order.getClientId());
    }

    @Test
    @DisplayName("Test setClient with null updates client to null and clientId to 0 (internal)")
    void setClient_WithNull_UpdatesClientAndClientIdToZero() {
        Order order = new Order();
        order.setClient(mockClient);
        order.setClient(null);
        assertNull(order.getClient());
        assertNull(order.getClientId());
        assertEquals(0, order.clientIdProperty().get());
    }

    @Test
    @DisplayName("Test setClientId with null sets internal ID to 0")
    void setClientId_WithNull_SetsInternalIdToZero() {
        Order order = new Order();
        order.setClientId(25);
        order.setClientId(null);
        assertNull(order.getClientId());
        assertEquals(0, order.clientIdProperty().get());
    }


    @Test
    @DisplayName("Test setEmployee updates employee and employeeId")
    void setEmployee_UpdatesEmployeeAndEmployeeId() {
        Order order = new Order();
        order.setEmployee(mockEmployee);
        assertSame(mockEmployee, order.getEmployee());
        assertEquals(mockEmployee.getId(), order.getEmployeeId());
    }

    @Test
    @DisplayName("Test setEmployee with null updates employee to null and employeeId to 0 (internal)")
    void setEmployee_WithNull_UpdatesEmployeeAndEmployeeIdToZero() {
        Order order = new Order();
        order.setEmployee(mockEmployee);
        order.setEmployee(null);
        assertNull(order.getEmployee());
        assertNull(order.getEmployeeId());
        assertEquals(0, order.employeeIdProperty().get());
    }

    @Test
    @DisplayName("Test setEmployeeId with null sets internal ID to 0")
    void setEmployeeId_WithNull_SetsInternalIdToZero() {
        Order order = new Order();
        order.setEmployeeId(35);
        order.setEmployeeId(null);
        assertNull(order.getEmployeeId());
        assertEquals(0, order.employeeIdProperty().get());
    }

    @Test
    @DisplayName("Test tableProperty updates table BUT NOT tableId (CURRENT BEHAVIOR)")
    void tableProperty_UpdatesTable_ButNotTableId_CurrentBehavior() {
        Order order = new Order();
        order.tableProperty().set(mockTable);

        assertSame(mockTable, order.getTable());
        assertNull(order.getTableId());
        assertEquals(0, order.tableIdProperty().get());
    }

    @Test
    @DisplayName("Test clientProperty updates client BUT NOT clientId (CURRENT BEHAVIOR)")
    void clientProperty_UpdatesClient_ButNotClientId_CurrentBehavior() {
        Order order = new Order();
        order.clientProperty().set(mockClient);
        assertSame(mockClient, order.getClient());
        assertNull(order.getClientId());
        assertEquals(0, order.clientIdProperty().get());
    }

    @Test
    @DisplayName("Test employeeProperty updates employee BUT NOT employeeId (CURRENT BEHAVIOR)")
    void employeeProperty_UpdatesEmployee_ButNotEmployeeId_CurrentBehavior() {
        Order order = new Order();
        order.employeeProperty().set(mockEmployee);
        assertSame(mockEmployee, order.getEmployee());
        assertNull(order.getEmployeeId());
        assertEquals(0, order.employeeIdProperty().get());
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        Order order1 = new Order(1, LocalDateTime.now(), 10,
                20, 30, "Картка", 100.0);
        assertTrue(order1.equals(order1));
    }

    @Test
    @DisplayName("Test equals is symmetric for same ID")
    void equals_IsSymmetric_SameId() {
        LocalDateTime time = LocalDateTime.now();
        Order order1 = new Order(1, time, 10, 20,
                30, "Картка", 100.0);

        Order order2 = new Order(1, time.minusHours(1), mockTable,
                mockClient, mockEmployee, "Готівка", 200.0);

        assertTrue(order1.equals(order2));
        assertTrue(order2.equals(order1));
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        Order order1 = new Order(1, LocalDateTime.now(), (Integer) null,
                (Integer) null, (Integer) null, "К", 10.0);

        Order order2 = new Order(2, LocalDateTime.now(), (Integer) null,
                (Integer) null, (Integer) null, "К", 10.0);

        assertFalse(order1.equals(order2));
    }

    @Test
    @DisplayName("Test equals handles objects with ID 0 (CURRENT Order.equals BEHAVIOR)")
    void equals_ObjectsWithIdZero_CurrentBehavior() {
        Order order1 = new Order();
        Order order2 = new Order();
        Order order3 = order1;

        assertTrue(order1.equals(order2), "Два різних екземпляри з " +
                "ID=0 будуть рівними через поточну логіку equals.");

        assertTrue(order1.equals(order3), "Екземпляр має бути рівний сам собі.");

        order1.setId(5);
        order2.setId(5);
        assertTrue(order1.equals(order2), "Об'єкти з однаковим ненульовим " +
                "ID мають бути рівними.");
    }


    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        Order order1 = new Order(1, LocalDateTime.now(), (Integer) null,
                (Integer) null, (Integer) null, "К", 10.0);
        int initialHashCode = order1.hashCode();
        order1.setTotalAmount(200.0);
        assertEquals(initialHashCode, order1.hashCode());
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void hashCode_ForEqualObjects() {
        Order order1 = new Order(1, LocalDateTime.now(), (Integer) null,
                (Integer) null, (Integer) null, "К", 10.0);

        Order order2 = new Order(1, LocalDateTime.now().minusDays(1),
                (Integer) null, (Integer) null, (Integer) null, "Г", 20.0);

        assertTrue(order1.equals(order2));
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    @DisplayName("Test toString contains relevant information")
    void toString_ContainsRelevantInfo() {
        Order order = new Order(123, LocalDateTime.of(2023, 10,
                26, 14, 30), 5, 15,
                25, "Картка", 250.75);

        String str = order.toString();
        assertTrue(str.contains("id=123"));
        assertTrue(str.contains("orderTime=2023-10-26T14:30"));
        assertTrue(str.contains("tableId=5"));
        assertTrue(str.contains("clientId=15"));
        assertTrue(str.contains("employeeId=25"));
        assertTrue(str.contains("paymentMethod='Картка'"));
        assertTrue(str.contains("totalAmount=250.75"));
    }

    @Test
    @DisplayName("Test toString handles null paymentMethod")
    void toString_HandlesNullPaymentMethod() {
        Order order = new Order();
        order.setId(1);
        String str = order.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("tableId=0"));
        assertTrue(str.contains("clientId=0"));
        assertTrue(str.contains("employeeId=0"));
        assertTrue(str.contains("paymentMethod='null'"));
    }
}