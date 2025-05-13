package org.example.restaurant_management_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private Button ordersButton;

    @FXML
    private Button kitchenButton;

    @FXML
    private Button inventoryButton;

    @FXML
    private Button reportingButton;

    @FXML
    private Button menuButton;

    @FXML
    private Button loginButton;  // Кнопка Вхід

    @FXML
    private Button logoutButton; // Кнопка Вихід

    @FXML
    private Label usernameLabel; // Лейбл для відображення імені користувача

    // Логіка для відкриття різних вікон
    @FXML
    public void openOrdersView(ActionEvent event) throws IOException {
        loadView("view/OrderView.fxml", "Управління Замовленнями", event);
    }

    @FXML
    public void openKitchenView(ActionEvent event) throws IOException {
        loadView("view/KitchenView.fxml", "Управління Кухнею", event);
    }

    @FXML
    public void openInventoryView(ActionEvent event) throws IOException {
        loadView("view/InventoryView.fxml", "Облік Складу", event);
    }

    @FXML
    public void openReportingView(ActionEvent event) throws IOException {
        loadView("view/ReportingView.fxml", "Звітність та Аналітика", event);
    }

    @FXML
    public void openMenuView(ActionEvent event) throws IOException {
        loadView("view/MenuView.fxml", "Управління Меню", event);
    }

    // Логіка для входу
    @FXML
    public void loginAction(ActionEvent event) {
        // Логіка входу (після входу показуємо ім'я користувача)
        System.out.println("Вхід до системи");

        // Симуляція отримання імені користувача після авторизації
        String username = "Ім'я користувача";  // Замініть на реальну логіку отримання імені користувача

        // Відображення імені користувача
        usernameLabel.setText(username);
        usernameLabel.setVisible(true);

        // Сховати кнопки "Вхід" і показати "Вихід"
        loginButton.setVisible(false);
        logoutButton.setVisible(true);

        // Відкрити доступ до основних кнопок після входу
        ordersButton.setDisable(false);
        kitchenButton.setDisable(false);
        inventoryButton.setDisable(false);
        reportingButton.setDisable(false);
        menuButton.setDisable(false);
    }

    // Логіка для виходу
    @FXML
    public void logoutAction(ActionEvent event) {
        // Логіка для виходу (наприклад, очищення інформації та закриття вікна)
        System.out.println("Вихід з системи");

        // Очищення імені користувача та приховування лейбла
        usernameLabel.setText("");
        usernameLabel.setVisible(false);

        // Показати кнопку "Вхід" і сховати "Вихід"
        loginButton.setVisible(true);
        logoutButton.setVisible(false);

        // Закрити доступ до основних кнопок після виходу
        ordersButton.setDisable(true);
        kitchenButton.setDisable(true);
        inventoryButton.setDisable(true);
        reportingButton.setDisable(true);
        menuButton.setDisable(true);

        // Закриття поточного вікна (можна додати свою логіку)
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();  // Закриває поточне вікно
    }

    // Метод для завантаження різних вікон
    private void loadView(String fxmlPath, String title, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));
        Scene scene = new Scene(loader.load());

        // Додавання стилів
        scene.getStylesheets().add(getClass().getResource("*/styles/MainStyle.css").toExternalForm());

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();

        // Опціонально: закрити поточне вікно
        // ((Node)(event.getSource())).getScene().getWindow().hide();
    }
}
