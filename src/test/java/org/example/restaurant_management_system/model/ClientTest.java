package org.example.restaurant_management_system.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    @DisplayName("Test no-argument constructor initializes properties correctly")
    void noArgConstructor_InitializesProperties() {
        Client client = new Client();

        assertEquals(0, client.getId(), "ID має бути 0 за замовчуванням.");
        assertNull(client.getFirstName(), "FirstName має бути null за замовчуванням.");
        assertNull(client.getLastName(), "LastName має бути null за замовчуванням.");
        assertNull(client.getPhoneNumber(), "PhoneNumber має бути null за замовчуванням.");
        assertNull(client.getEmail(), "Email має бути null за замовчуванням.");
        assertEquals(0.0, client.getLoyaltyPoints(),
                0.001, "LoyaltyPoints має бути 0.0 за замовчуванням.");

        // Перевірка, що properties створені
        assertNotNull(client.idProperty());
        assertNotNull(client.firstNameProperty());
        assertNotNull(client.lastNameProperty());
        assertNotNull(client.phoneNumberProperty());
        assertNotNull(client.emailProperty());
        assertNotNull(client.loyaltyPointsProperty());
    }

    @Test
    @DisplayName("Test constructor with all arguments sets properties correctly")
    void fullConstructor_SetsProperties() {
        int expectedId = 1;
        String expectedFirstName = "Іван";
        String expectedLastName = "Петренко";
        String expectedPhoneNumber = "0501234567";
        String expectedEmail = "ivan@example.com";
        double expectedLoyaltyPoints = 100.5;

        Client client = new Client(expectedId, expectedFirstName,
                expectedLastName, expectedPhoneNumber, expectedEmail, expectedLoyaltyPoints);

        assertEquals(expectedId, client.getId());
        assertEquals(expectedFirstName, client.getFirstName());
        assertEquals(expectedLastName, client.getLastName());
        assertEquals(expectedPhoneNumber, client.getPhoneNumber());
        assertEquals(expectedEmail, client.getEmail());
        assertEquals(expectedLoyaltyPoints, client.getLoyaltyPoints(), 0.001);
    }

    @Test
    @DisplayName("Test constructor without ID sets properties and default ID")
    void constructorWithoutId_SetsPropertiesAndDefaultId() {
        String expectedFirstName = "Ольга";
        String expectedLastName = "Іваненко";
        String expectedPhoneNumber = "0997654321";
        String expectedEmail = "olga@example.com";
        double expectedLoyaltyPoints = 50.0;

        Client client = new Client(expectedFirstName, expectedLastName,
                expectedPhoneNumber, expectedEmail, expectedLoyaltyPoints);

        assertEquals(0, client.getId(), "ID має бути 0 за замовчуванням, якщо не вказано.");
        assertEquals(expectedFirstName, client.getFirstName());
        assertEquals(expectedLastName, client.getLastName());
        assertEquals(expectedPhoneNumber, client.getPhoneNumber());
        assertEquals(expectedEmail, client.getEmail());
        assertEquals(expectedLoyaltyPoints, client.getLoyaltyPoints(), 0.001);
    }

    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        Client client = new Client();
        int testId = 123;
        client.setId(testId);
        assertEquals(testId, client.getId());
    }

    @Test
    @DisplayName("Test setFirstName and getFirstName work correctly")
    void setFirstNameAndGetFirstName() {
        Client client = new Client();
        String testName = "ТестІм'я";
        client.setFirstName(testName);
        assertEquals(testName, client.getFirstName());
    }

    @Test
    @DisplayName("Test setLastName and getLastName work correctly")
    void setLastNameAndGetLastName() {
        Client client = new Client();
        String testName = "ТестПрізвище";
        client.setLastName(testName);
        assertEquals(testName, client.getLastName());
    }

    @Test
    @DisplayName("Test setPhoneNumber and getPhoneNumber work correctly")
    void setPhoneNumberAndGetPhoneNumber() {
        Client client = new Client();
        String testPhone = "1234567890";
        client.setPhoneNumber(testPhone);
        assertEquals(testPhone, client.getPhoneNumber());
    }

    @Test
    @DisplayName("Test setEmail and getEmail work correctly")
    void setEmailAndGetEmail() {
        Client client = new Client();
        String testEmail = "test@test.com";
        client.setEmail(testEmail);
        assertEquals(testEmail, client.getEmail());
    }

    @Test
    @DisplayName("Test setLoyaltyPoints and getLoyaltyPoints work correctly")
    void setLoyaltyPointsAndGetLoyaltyPoints() {
        Client client = new Client();
        double testPoints = 75.25;
        client.setLoyaltyPoints(testPoints);
        assertEquals(testPoints, client.getLoyaltyPoints(), 0.001);
    }

    @Test
    @DisplayName("Test idProperty returns correct IntegerProperty")
    void idProperty_ReturnsCorrectProperty() {
        Client client = new Client();
        IntegerProperty idProp = client.idProperty();
        assertNotNull(idProp);
        assertEquals(client.getId(), idProp.get());

        int newId = 55;
        idProp.set(newId);
        assertEquals(newId, client.getId());
    }

    @Test
    @DisplayName("Test firstNameProperty returns correct StringProperty")
    void firstNameProperty_ReturnsCorrectProperty() {
        Client client = new Client();
        StringProperty firstNameProp = client.firstNameProperty();
        assertNotNull(firstNameProp);
        assertEquals(client.getFirstName(), firstNameProp.get());

        String newName = "НовеІм'я";
        firstNameProp.set(newName);
        assertEquals(newName, client.getFirstName());
    }

    @Test
    @DisplayName("Test lastNameProperty returns correct StringProperty")
    void lastNameProperty_ReturnsCorrectProperty() {
        Client client = new Client();
        StringProperty lastNameProp = client.lastNameProperty();
        assertNotNull(lastNameProp);
        assertEquals(client.getLastName(), lastNameProp.get());

        String newName = "НовеПрізвище";
        lastNameProp.set(newName);
        assertEquals(newName, client.getLastName());
    }

    @Test
    @DisplayName("Test phoneNumberProperty returns correct StringProperty")
    void phoneNumberProperty_ReturnsCorrectProperty() {
        Client client = new Client();
        StringProperty prop = client.phoneNumberProperty();
        assertNotNull(prop);
        assertEquals(client.getPhoneNumber(), prop.get());

        String newValue = "0000000000";
        prop.set(newValue);
        assertEquals(newValue, client.getPhoneNumber());
    }

    @Test
    @DisplayName("Test emailProperty returns correct StringProperty")
    void emailProperty_ReturnsCorrectProperty() {
        Client client = new Client();
        StringProperty prop = client.emailProperty();
        assertNotNull(prop);
        assertEquals(client.getEmail(), prop.get());

        String newValue = "new@example.com";
        prop.set(newValue);
        assertEquals(newValue, client.getEmail());
    }

    @Test
    @DisplayName("Test loyaltyPointsProperty returns correct DoubleProperty")
    void loyaltyPointsProperty_ReturnsCorrectProperty() {
        Client client = new Client();
        DoubleProperty prop = client.loyaltyPointsProperty();
        assertNotNull(prop);
        assertEquals(client.getLoyaltyPoints(), prop.get(), 0.001);

        double newValue = 123.45;
        prop.set(newValue);
        assertEquals(newValue, client.getLoyaltyPoints(), 0.001);
    }

    @Test
    @DisplayName("Test equals is reflexive")
    void equals_IsReflexive() {
        Client client1 = new Client(1, "Ім'я", "Прізвище",
                "1", "e@1.com", 10);
        assertTrue(client1.equals(client1), "Клієнт має дорівнювати сам собі.");
    }

    @Test
    @DisplayName("Test equals is symmetric")
    void equals_IsSymmetric() {
        Client client1 = new Client(1, "Ім'я",
                "Прізвище", "1", "e@1.com", 10);
        Client client2 = new Client(1, "ІншеІм'я",
                "ІншеПрізвище", "2", "e@2.com", 20);
        assertTrue(client1.equals(client2), "Клієнти з однаковим ID мають бути рівними.");
        assertTrue(client2.equals(client1), "Рівність має бути симетричною.");
    }

    @Test
    @DisplayName("Test equals is transitive (not strictly testable with ID only comparison, but for completeness)")
    void equals_IsTransitive() {
        Client client1 = new Client(1, "A",
                "A", "1", "a@a.com", 1);
        Client client2 = new Client(1, "B",
                "B", "2", "b@b.com", 2);
        Client client3 = new Client(1, "C",
                "C", "3", "c@c.com", 3);

        assertTrue(client1.equals(client2));
        assertTrue(client2.equals(client3));
        assertTrue(client1.equals(client3), "Рівність має бути транзитивною.");
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    void equals_DifferentIds_ReturnsFalse() {
        Client client1 = new Client(1, "Ім'я",
                "Прізвище", "1", "e@1.com", 10);
        Client client2 = new Client(2, "Ім'я",
                "Прізвище", "1", "e@1.com", 10); // Інший ID
        assertFalse(client1.equals(client2), "Клієнти з різними ID не мають бути рівними.");
    }

    @Test
    @DisplayName("Test equals returns false for null object")
    void equals_NullObject_ReturnsFalse() {
        Client client1 = new Client(1, "Ім'я",
                "Прізвище", "1", "e@1.com", 10);
        assertFalse(client1.equals(null), "Клієнт не має дорівнювати null.");
    }

    @Test
    @DisplayName("Test equals returns false for object of different class")
    void equals_DifferentClass_ReturnsFalse() {
        Client client1 = new Client(1, "Ім'я",
                "Прізвище", "1", "e@1.com", 10);
        Object otherObject = new Object();
        assertFalse(client1.equals(otherObject),
                "Клієнт не має дорівнювати об'єкту іншого класу.");
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void hashCode_IsConsistent() {
        Client client1 = new Client(1, "Ім'я",
                "Прізвище", "1", "e@1.com", 10);
        int initialHashCode = client1.hashCode();
        client1.setFirstName("НовеІм'я");
        assertEquals(initialHashCode, client1.hashCode(),
                "HashCode має бути послідовним, якщо ID не змінюється.");
    }

    @Test
    @DisplayName("Test hashCode for equal objects")
    void hashCode_ForEqualObjects() {
        Client client1 = new Client(1, "Ім'я1",
                "Прізвище1", "1", "e1@1.com", 10);
        Client client2 = new Client(1, "Ім'я2",
                "Прізвище2", "2", "e2@2.com", 20); // Той самий ID
        assertTrue(client1.equals(client2));
        assertEquals(client1.hashCode(), client2.hashCode(),
                "Рівні об'єкти мають мати однаковий hashCode.");
    }

    @Test
    @DisplayName("Test hashCode for different objects (different ID)")
    void hashCode_ForDifferentObjects_DifferentId() {
        Client client1 = new Client(1, "Ім'я",
                "Прізвище", "1", "e@1.com", 10);
        Client client2 = new Client(2, "Ім'я",
                "Прізвище", "1", "e@1.com", 10);
        assertNotEquals(client1.hashCode(), client2.hashCode(),
                "Різні ID зазвичай дають різний hashCode.");
    }

    // --- Тести для toString ---
    @Test
    @DisplayName("Test toString returns correct format")
    void toString_ReturnsCorrectFormat() {
        String firstName = "Тест";
        String lastName = "Клієнт";
        String phone = "0509876543";
        Client client = new Client(firstName, lastName, phone, "test@client.com", 0);
        String expectedString = firstName + " " + lastName + " (" + phone + ")";
        assertEquals(expectedString, client.toString());
    }

    @Test
    @DisplayName("Test toString handles null first name")
    void toString_HandlesNullFirstName() {
        String lastName = "ПрізвищеБезІмені";
        String phone = "111222333";
        Client client = new Client(null, lastName, phone,
                "no@name.com", 0);
        // getFirstName() поверне null
        String expectedString = "null " + lastName + " (" + phone + ")";
        assertEquals(expectedString, client.toString());
    }

    @Test
    @DisplayName("Test toString handles null last name")
    void toString_HandlesNullLastName() {
        String firstName = "Ім'яБезПрізвища";
        String phone = "222333444";
        Client client = new Client(firstName, null, phone,
                "no@lastname.com", 0);
        String expectedString = firstName + " null (" + phone + ")";
        assertEquals(expectedString, client.toString());
    }

    @Test
    @DisplayName("Test toString handles null phone number")
    void toString_HandlesNullPhoneNumber() {
        String firstName = "Ім'я";
        String lastName = "Прізвище";
        Client client = new Client(firstName, lastName, null,
                "no@phone.com", 0);
        String expectedString = firstName + " " + lastName + " (null)";
        assertEquals(expectedString, client.toString());
    }

    @Test
    @DisplayName("Test toString handles all nulls for name and phone")
    void toString_HandlesAllNullsForNameAndPhone() {
        Client client = new Client(null, null,
                null, null, 0);
        String expectedString = "null null (null)";
        assertEquals(expectedString, client.toString());
    }
}