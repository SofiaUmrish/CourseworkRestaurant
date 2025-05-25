package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import java.util.Objects;

public class OrderItem {
    private final IntegerProperty id;
    private final IntegerProperty orderId;
    private final ObjectProperty<Order> order;

    private final IntegerProperty menuItemId;
    private final ObjectProperty<MenuItem> menuItem;

    private final IntegerProperty quantity;
    private final DoubleProperty priceAtOrder;

    //конструктори

    public OrderItem() {
        this.id = new SimpleIntegerProperty();
        this.orderId = new SimpleIntegerProperty();
        this.order = new SimpleObjectProperty<>();
        this.menuItemId = new SimpleIntegerProperty();
        this.menuItem = new SimpleObjectProperty<>();
        this.quantity = new SimpleIntegerProperty();
        this.priceAtOrder = new SimpleDoubleProperty(0.0);

    }

    // завантаження з бд з id зв'язаних об'єктів
    public OrderItem(int id, int orderId, int menuItemId, int quantity, double priceAtOrder) {
        this.id = new SimpleIntegerProperty(id);
        this.orderId = new SimpleIntegerProperty(orderId);
        this.order = new SimpleObjectProperty<>();
        this.menuItemId = new SimpleIntegerProperty(menuItemId);
        this.menuItem = new SimpleObjectProperty<>();
        this.quantity = new SimpleIntegerProperty(quantity);
        this.priceAtOrder = new SimpleDoubleProperty(priceAtOrder);
  }

    // завантаження з бд або створення
    public OrderItem(int id, Order order, MenuItem menuItem, int quantity, double priceAtOrder) {
        this.id = new SimpleIntegerProperty(id);
        this.order = new SimpleObjectProperty<>(order);
        this.orderId = new SimpleIntegerProperty(order != null ? order.getId() : 0);
        this.menuItem = new SimpleObjectProperty<>(menuItem);
        this.menuItemId = new SimpleIntegerProperty(menuItem != null ? menuItem.getId() : 0);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.priceAtOrder = new SimpleDoubleProperty(priceAtOrder);
   }

    // створення нового запису без id з об'єктами
    public OrderItem(Order order, MenuItem menuItem, int quantity, double priceAtOrder) {
        this.id = new SimpleIntegerProperty();
        this.order = new SimpleObjectProperty<>(order);
        this.orderId = new SimpleIntegerProperty(order != null ? order.getId() : 0);
        this.menuItem = new SimpleObjectProperty<>(menuItem);
        this.menuItemId = new SimpleIntegerProperty(menuItem != null ? menuItem.getId() : 0);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.priceAtOrder = new SimpleDoubleProperty(priceAtOrder);
  }

    //геттери
    public int getId() { return id.get(); }
    public int getOrderId() { return orderId.get(); }
    public Order getOrder() { return order.get(); }
    public int getMenuItemId() { return menuItemId.get(); }
    public MenuItem getMenuItem() { return menuItem.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getPriceAtOrder() { return priceAtOrder.get(); }

    //сеттери
    public void setId(int id) { this.id.set(id); }
    public void setOrderId(int orderId) { this.orderId.set(orderId); }
    public void setOrder(Order order) {
        this.order.set(order);
        if (order != null) {
            this.orderId.set(order.getId());
        }
    }
    public void setMenuItemId(int menuItemId) { this.menuItemId.set(menuItemId); }
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem.set(menuItem);
        if (menuItem != null) {
            this.menuItemId.set(menuItem.getId());
        }
    }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setPriceAtOrder(double priceAtOrder) { this.priceAtOrder.set(priceAtOrder); }


    public IntegerProperty idProperty() { return id; }
    public IntegerProperty orderIdProperty() { return orderId; }
    public ObjectProperty<Order> orderProperty() { return order; }
    public IntegerProperty menuItemIdProperty() { return menuItemId; }
    public ObjectProperty<MenuItem> menuItemProperty() { return menuItem; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty priceAtOrderProperty() { return priceAtOrder; }

    // загальна сума
    public DoubleProperty totalPriceProperty() {
        return new SimpleDoubleProperty(getQuantity() * getPriceAtOrder());
    }

    public double getTotalPrice() {
        return getQuantity() * getPriceAtOrder();
    }

    //перевизначення
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem that = (OrderItem) o;
        return id.get() == that.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id.get() +
                ", orderId=" + orderId.get() +
                ", menuItemId=" + menuItemId.get() +
                ", quantity=" + quantity.get() +
                ", priceAtOrder=" + priceAtOrder.get() + '}';
    }
}