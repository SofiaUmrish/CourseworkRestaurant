package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderItemTest {

    private Order mockOrder;
    private MenuItem mockMenuItem;

    @BeforeEach
    void setUp() {
        mockOrder = mock(Order.class);
        when(mockOrder.getId()).thenReturn(1000);

        mockMenuItem = mock(MenuItem.class);
        when(mockMenuItem.getId()).thenReturn(100);
    }

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        OrderItem item = new OrderItem();

        assertEquals(0, item.getId());
        assertEquals(0, item.getOrderId());
        assertNull(item.getOrder());
        assertEquals(0, item.getMenuItemId());
        assertNull(item.getMenuItem());
        assertEquals(0, item.getQuantity());
        assertEquals(0.0, item.getPriceAtOrder(), 0.001);

        assertNotNull(item.idProperty());
    }

    @Test
    @DisplayName("Test constructor for DB load (with IDs)")
    void constructorForDbLoad_WithIds_SetsProperties() {
        int id = 1;
        int orderId = 101;
        int menuItemId = 201;
        int quantity = 2;
        double price = 50.25;

        OrderItem item = new OrderItem(id, orderId, menuItemId, quantity, price);

        assertEquals(id, item.getId());
        assertEquals(orderId, item.getOrderId());
        assertNull(item.getOrder());
        assertEquals(menuItemId, item.getMenuItemId());
        assertNull(item.getMenuItem());
        assertEquals(quantity, item.getQuantity());
        assertEquals(price, item.getPriceAtOrder(), 0.001);
    }

    @Test
    @DisplayName("Test constructor with full objects and ID")
    void constructorWithFullObjectsAndId_SetsProperties() {
        int id = 2;
        int quantity = 3;
        double price = 75.0;

        OrderItem item = new OrderItem(id, mockOrder, mockMenuItem, quantity, price);

        assertEquals(id, item.getId());
        assertSame(mockOrder, item.getOrder());
        assertEquals(mockOrder.getId(), item.getOrderId());
        assertSame(mockMenuItem, item.getMenuItem());
        assertEquals(mockMenuItem.getId(), item.getMenuItemId());
        assertEquals(quantity, item.getQuantity());
        assertEquals(price, item.getPriceAtOrder(), 0.001);
    }

    @Test
    @DisplayName("Test constructor with full objects and ID handles null Order")
    void constructorWithFullObjectsAndId_HandlesNullOrder() {
        OrderItem item = new OrderItem(1, null, mockMenuItem, 1, 10.0);
        assertNull(item.getOrder());
        assertEquals(0, item.getOrderId());
        assertSame(mockMenuItem, item.getMenuItem());
        assertEquals(mockMenuItem.getId(), item.getMenuItemId());
    }

    @Test
    @DisplayName("Test constructor with full objects and ID handles null MenuItem")
    void constructorWithFullObjectsAndId_HandlesNullMenuItem() {
        OrderItem item = new OrderItem(1, mockOrder, null, 1, 10.0);
        assertSame(mockOrder, item.getOrder());
        assertEquals(mockOrder.getId(), item.getOrderId());
        assertNull(item.getMenuItem());
        assertEquals(0, item.getMenuItemId());
    }

    @Test
    @DisplayName("Test constructor for new record (without ID, with objects)")
    void constructorForNewRecord_WithObjects_SetsProperties() {
        int quantity = 1;
        double price = 120.0;

        OrderItem item = new OrderItem(mockOrder, mockMenuItem, quantity, price);

        assertEquals(0, item.getId());
        assertSame(mockOrder, item.getOrder());
        assertEquals(mockOrder.getId(), item.getOrderId());
        assertSame(mockMenuItem, item.getMenuItem());
        assertEquals(mockMenuItem.getId(), item.getMenuItemId());
        assertEquals(quantity, item.getQuantity());
        assertEquals(price, item.getPriceAtOrder(), 0.001);
    }

    @Test
    @DisplayName("Test setOrder updates order and orderId")
    void setOrder_UpdatesOrderAndOrderId() {
        OrderItem item = new OrderItem();
        item.setOrder(mockOrder);

        assertSame(mockOrder, item.getOrder());
        assertEquals(mockOrder.getId(), item.getOrderId());
    }

    @Test
    @DisplayName("Test setOrder with null updates order to null (orderId remains unchanged - CURRENT LOGIC)")
    void setOrder_WithNull_UpdatesOrderToNull_OrderIdUnchanged() {
        OrderItem item = new OrderItem(1, mockOrder, mockMenuItem, 1, 10.0);
        assertEquals(mockOrder.getId(), item.getOrderId());

        item.setOrder(null);

        assertNull(item.getOrder());
        assertEquals(mockOrder.getId(), item.getOrderId(), "OrderId НЕ має змінюватися, " +
                "якщо новий Order null.");
    }


    @Test
    @DisplayName("Test setMenuItem updates menuItem and menuItemId")
    void setMenuItem_UpdatesMenuItemAndMenuItemId() {
        OrderItem item = new OrderItem();
        item.setMenuItem(mockMenuItem);

        assertSame(mockMenuItem, item.getMenuItem());
        assertEquals(mockMenuItem.getId(), item.getMenuItemId());
    }

    @Test
    @DisplayName("Test setMenuItem with null updates menuItem to null (menuItemId" +
            " remains unchanged - CURRENT LOGIC)")
    void setMenuItem_WithNull_UpdatesMenuItemToNull_MenuItemIdUnchanged() {
        OrderItem item = new OrderItem(1, mockOrder, mockMenuItem, 1, 10.0);
        assertEquals(mockMenuItem.getId(), item.getMenuItemId());

        item.setMenuItem(null);

        assertNull(item.getMenuItem());
        assertEquals(mockMenuItem.getId(), item.getMenuItemId(), "MenuItemId НЕ" +
                " має змінюватися, якщо новий MenuItem null.");
    }

    @Test
    @DisplayName("Test setQuantity and getQuantity work correctly")
    void setQuantityAndGetQuantity() {
        OrderItem item = new OrderItem();
        int quantity = 5;
        item.setQuantity(quantity);
        assertEquals(quantity, item.getQuantity());
    }

    @Test
    @DisplayName("Test setPriceAtOrder and getPriceAtOrder work correctly")
    void setPriceAtOrderAndGetPriceAtOrder() {
        OrderItem item = new OrderItem();
        double price = 9.99;
        item.setPriceAtOrder(price);
        assertEquals(price, item.getPriceAtOrder(), 0.001);
    }

    @Test
    @DisplayName("Test orderProperty updates order BUT NOT orderId (CURRENT BEHAVIOR)")
    void orderProperty_UpdatesOrder_ButNotOrderId_CurrentBehavior() {
        OrderItem item = new OrderItem();
        item.orderProperty().set(mockOrder);

        assertSame(mockOrder, item.getOrder());
        assertEquals(0, item.getOrderId(), "OrderId НЕ має оновитися" +
                " автоматично через orderProperty.");
    }

    @Test
    @DisplayName("Test menuItemProperty updates menuItem BUT NOT menuItemId (CURRENT BEHAVIOR)")
    void menuItemProperty_UpdatesMenuItem_ButNotMenuItemId_CurrentBehavior() {
        OrderItem item = new OrderItem();
        item.menuItemProperty().set(mockMenuItem);

        assertSame(mockMenuItem, item.getMenuItem());
        assertEquals(0, item.getMenuItemId(), "MenuItemId НЕ має оновитися " +
                "автоматично через menuItemProperty.");
    }

    @Test
    @DisplayName("Test getTotalPrice calculates correctly")
    void getTotalPrice_CalculatesCorrectly() {
        OrderItem item = new OrderItem();
        item.setQuantity(3);
        item.setPriceAtOrder(10.50);
        assertEquals(31.50, item.getTotalPrice(), 0.001);
    }

    @Test
    @DisplayName("Test totalPriceProperty returns correct calculated value")
    void totalPriceProperty_ReturnsCorrectCalculatedValue() {
        OrderItem item = new OrderItem();
        item.setQuantity(2);
        item.setPriceAtOrder(25.0);

        DoubleProperty totalPriceProp = item.totalPriceProperty();
        assertNotNull(totalPriceProp);
        assertEquals(50.0, totalPriceProp.get(), 0.001);
    }

    @Test
    @DisplayName("Test totalPriceProperty is a new instance each time (as per current implementation)")
    void totalPriceProperty_IsNewInstanceEachCall() {
        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setPriceAtOrder(10.0);

        DoubleProperty prop1 = item.totalPriceProperty();
        DoubleProperty prop2 = item.totalPriceProperty();

        assertNotSame(prop1, prop2, "totalPriceProperty() має " +
                "повертати новий екземпляр SimpleDoubleProperty щоразу.");
        assertEquals(prop1.get(), prop2.get(), 0.001);
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        OrderItem item1 = new OrderItem(1, 100, 200,
                1, 10.0);

        assertTrue(item1.equals(item1));
    }

    @Test
    @DisplayName("Test equals is symmetric for same ID")
    void equals_IsSymmetric_SameId() {
        OrderItem item1 = new OrderItem(1, 100, 200,
                1, 10.0);
        OrderItem item2 = new OrderItem(1, mockOrder, mockMenuItem,
                2, 20.0);
        assertTrue(item1.equals(item2));
        assertTrue(item2.equals(item1));
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        OrderItem item1 = new OrderItem(1, 100, 200,
                1, 10.0);

        OrderItem item2 = new OrderItem(2, 100, 200,
                1, 10.0);

        assertFalse(item1.equals(item2));
    }

    @Test
    @DisplayName("Test equals handles objects with ID 0 (CURRENT OrderItem.equals BEHAVIOR)")
    void equals_ObjectsWithIdZero_CurrentBehavior() {
        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();
        OrderItem item3 = item1;

        assertTrue(item1.equals(item2), "Два різних екземпляри з ID=0 будуть" +
                " рівними через поточну логіку equals.");
        assertTrue(item1.equals(item3));
    }

    @Test
    @DisplayName("Test toString contains relevant information")
    void toString_ContainsRelevantInfo() {
        OrderItem item = new OrderItem(7, 107, 207, 3, 12.50);
        String str = item.toString();
        assertTrue(str.contains("id=7"));
        assertTrue(str.contains("orderId=107"));
        assertTrue(str.contains("menuItemId=207"));
        assertTrue(str.contains("quantity=3"));
        assertTrue(str.contains("priceAtOrder=12.5"));
    }
}