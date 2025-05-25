package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import java.util.Objects;

public class Client {
    private final IntegerProperty id;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty phoneNumber;
    private final StringProperty email;
    private final DoubleProperty loyaltyPoints;

    //конструктори

    public Client() {
        this.id = new SimpleIntegerProperty();
        this.firstName = new SimpleStringProperty();
        this.lastName = new SimpleStringProperty();
        this.phoneNumber = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.loyaltyPoints = new SimpleDoubleProperty(0.0);
    }

    // Завантаження з бд
    public Client(int id, String firstName, String lastName, String phoneNumber, String email, double loyaltyPoints) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.email = new SimpleStringProperty(email);
        this.loyaltyPoints = new SimpleDoubleProperty(loyaltyPoints);
    }

    // новий запис без id
    public Client(String firstName, String lastName, String phoneNumber, String email, double loyaltyPoints) {
        this.id = new SimpleIntegerProperty(); // id буде згенеровано бд
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
        this.email = new SimpleStringProperty(email);
        this.loyaltyPoints = new SimpleDoubleProperty(loyaltyPoints);
    }

    //геттери
    public int getId() { return id.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getPhoneNumber() { return phoneNumber.get(); }
    public String getEmail() { return email.get(); }
    public double getLoyaltyPoints() { return loyaltyPoints.get(); }

    //сеттери
    public void setId(int id) { this.id.set(id); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber.set(phoneNumber); }
    public void setEmail(String email) { this.email.set(email); }
    public void setLoyaltyPoints(double loyaltyPoints) { this.loyaltyPoints.set(loyaltyPoints); }


    public IntegerProperty idProperty() { return id; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty phoneNumberProperty() { return phoneNumber; }
    public StringProperty emailProperty() { return email; }
    public DoubleProperty loyaltyPointsProperty() { return loyaltyPoints; }

    //перевизначення
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id.get() == client.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }
    @Override
    public String toString() {
        return getFirstName() + " " + getLastName() + " (" + getPhoneNumber() + ")";
    }
}