package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.Objects;

public class Order {
    private final IntegerProperty id;
    private final ObjectProperty<LocalDateTime> orderTime;

    private final IntegerProperty tableId;
    private final ObjectProperty<Table> table;

    private final IntegerProperty clientId;
    private final ObjectProperty<Client> client;

    private final IntegerProperty employeeId;
    private final ObjectProperty<Employee> employee;

    private final StringProperty paymentMethod;
    private final DoubleProperty totalAmount;

    //конструктор
    public Order() {
        this.id = new SimpleIntegerProperty();
        this.orderTime = new SimpleObjectProperty<>(LocalDateTime.now());
        this.tableId = new SimpleIntegerProperty();
        this.table = new SimpleObjectProperty<>();
        this.clientId = new SimpleIntegerProperty();
        this.client = new SimpleObjectProperty<>();
        this.employeeId = new SimpleIntegerProperty();
        this.employee = new SimpleObjectProperty<>();
        this.paymentMethod = new SimpleStringProperty();
        this.totalAmount = new SimpleDoubleProperty(0.0);
    }

    // завантаження з бд з id пов'язаних об'єктів
    public Order(int id, LocalDateTime orderTime, Integer tableId, Integer clientId, Integer employeeId,
                 String paymentMethod, double totalAmount) {
        this.id = new SimpleIntegerProperty(id);
        this.orderTime = new SimpleObjectProperty<>(orderTime);
        this.tableId = new SimpleIntegerProperty(tableId != null ? tableId : 0);
        this.table = new SimpleObjectProperty<>();
        this.clientId = new SimpleIntegerProperty(clientId != null ? clientId : 0);
        this.client = new SimpleObjectProperty<>();
        this.employeeId = new SimpleIntegerProperty(employeeId != null ? employeeId : 0);
        this.employee = new SimpleObjectProperty<>();
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
    }

    // завантаження з бд або створення
    public Order(int id, LocalDateTime orderTime, Table table, Client client, Employee employee,
                 String paymentMethod, double totalAmount) {
        this.id = new SimpleIntegerProperty(id);
        this.orderTime = new SimpleObjectProperty<>(orderTime);
        this.table = new SimpleObjectProperty<>(table);
        this.tableId = new SimpleIntegerProperty(table != null ? table.getId() : 0);
        this.client = new SimpleObjectProperty<>(client);
        this.clientId = new SimpleIntegerProperty(client != null ? client.getId() : 0);
        this.employee = new SimpleObjectProperty<>(employee);
        this.employeeId = new SimpleIntegerProperty(employee != null ? employee.getId() : 0);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
    }

    // новий запис без id з об'єктами
    public Order(LocalDateTime orderTime, Table table, Client client, Employee employee,
                 String paymentMethod, double totalAmount) {
        this.id = new SimpleIntegerProperty();
        this.orderTime = new SimpleObjectProperty<>(orderTime);
        this.table = new SimpleObjectProperty<>(table);
        this.tableId = new SimpleIntegerProperty(table != null ? table.getId() : 0);
        this.client = new SimpleObjectProperty<>(client);
        this.clientId = new SimpleIntegerProperty(client != null ? client.getId() : 0);
        this.employee = new SimpleObjectProperty<>(employee);
        this.employeeId = new SimpleIntegerProperty(employee != null ? employee.getId() : 0);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
    }

    //геттери
    public int getId() { return id.get(); }
    public LocalDateTime getOrderTime() { return orderTime.get(); }
    public Integer getTableId() { return tableId.get() == 0 ? null : tableId.get(); }
    public Table getTable() { return table.get(); }
    public Integer getClientId() { return clientId.get() == 0 ? null : clientId.get(); }
    public Client getClient() { return client.get(); }
    public Integer getEmployeeId() { return employeeId.get() == 0 ? null : employeeId.get(); }
    public Employee getEmployee() { return employee.get(); }
    public String getPaymentMethod() { return paymentMethod.get(); }
    public double getTotalAmount() { return totalAmount.get(); }

    //сеттери
    public void setId(int id) { this.id.set(id); }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime.set(orderTime); }
    public void setTableId(Integer tableId) { this.tableId.set(tableId != null ? tableId : 0); }
    public void setTable(Table table) {
        this.table.set(table);
        this.tableId.set(table != null ? table.getId() : 0);
    }
    public void setClientId(Integer clientId) { this.clientId.set(clientId != null ? clientId : 0); }
    public void setClient(Client client) {
        this.client.set(client);
        this.clientId.set(client != null ? client.getId() : 0);
    }
    public void setEmployeeId(Integer employeeId) { this.employeeId.set(employeeId != null ? employeeId : 0); }
    public void setEmployee(Employee employee) {
        this.employee.set(employee);
        this.employeeId.set(employee != null ? employee.getId() : 0);
    }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod.set(paymentMethod); }
    public void setTotalAmount(double totalAmount) { this.totalAmount.set(totalAmount); }


    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<LocalDateTime> orderTimeProperty() { return orderTime; }
    public IntegerProperty tableIdProperty() { return tableId; }
    public ObjectProperty<Table> tableProperty() { return table; }
    public IntegerProperty clientIdProperty() { return clientId; }
    public ObjectProperty<Client> clientProperty() { return client; }
    public IntegerProperty employeeIdProperty() { return employeeId; }
    public ObjectProperty<Employee> employeeProperty() { return employee; }
    public StringProperty paymentMethodProperty() { return paymentMethod; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }


    //перевизначення
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.get() == order.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id.get() +
                ", orderTime=" + orderTime.get() +
                ", tableId=" + tableId.get() +
                ", clientId=" + clientId.get() +
                ", employeeId=" + employeeId.get() +
                ", paymentMethod='" + paymentMethod.get() + '\'' +
                ", totalAmount=" + totalAmount.get() + '}';
    }
}