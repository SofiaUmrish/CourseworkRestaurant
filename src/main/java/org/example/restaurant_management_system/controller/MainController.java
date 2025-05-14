package org.example.restaurant_management_system.controller;

import org.example.restaurant_management_system.model.Role;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private Button ordersButton;
    @FXML private Button kitchenButton;
    @FXML private Button inventoryButton;
    @FXML private Button reportingButton;
    @FXML private Button menuButton;
    @FXML private Button loginTopButton;
    @FXML private Button logoutTopButton;
    @FXML private Label usernameLabel;

    private Role userRole;

    // ініціалізація — кнопки неактивні, користувач неавторизований
    @FXML
    private void initialize() {
        usernameLabel.setVisible(false);
        logoutTopButton.setVisible(false);

        ordersButton.setDisable(true);
        kitchenButton.setDisable(true);
        inventoryButton.setDisable(true);
        reportingButton.setDisable(true);
        menuButton.setDisable(true);
    }

    // метод для входу та відкриття доступу
    public void setRole(Role role) {
        this.userRole = role;
        usernameLabel.setText(role.getPosition());  // показуємо посаду на лейблі
        usernameLabel.setVisible(true);

        // Доступ до кнопок залежно від посади
        switch (role.getPosition()) {
            case "COOK":
                kitchenButton.setDisable(false); // Кухар
                break;
            case "WAITER":
                ordersButton.setDisable(false); // Офіціант
                break;
            case "STOREKEEPER":
                inventoryButton.setDisable(false); // Працівник складу
                break;
            case "ANALYST":
                reportingButton.setDisable(false); // Аналітик
                break;
            case "MANAGER":
                ordersButton.setDisable(false);
                kitchenButton.setDisable(false);
                inventoryButton.setDisable(false);
                reportingButton.setDisable(false);
                menuButton.setDisable(false); // Менеджер має доступ до всіх кнопок
                break;
        }

        loginTopButton.setVisible(false); // Ховаємо кнопку логіну
        logoutTopButton.setVisible(true); // Показуємо кнопку логауту
    }

    // Дія для логауту
    @FXML
    public void logoutAction(ActionEvent event) {
        System.out.println("Вихід з системи");

        userRole = null;
        usernameLabel.setText("");
        usernameLabel.setVisible(false);

        loginTopButton.setVisible(true);
        logoutTopButton.setVisible(false);

        // Відключаємо всі кнопки доступу
        ordersButton.setDisable(true);
        kitchenButton.setDisable(true);
        inventoryButton.setDisable(true);
        reportingButton.setDisable(true);
        menuButton.setDisable(true);

        try {
            // Завантажуємо вікно входу
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Scene loginScene = new Scene(loader.load());
            String css = getClass().getResource("/styles/LoginStyle.css").toExternalForm();
            loginScene.getStylesheets().add(css);
            // Отримуємо сцену поточного вікна
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.setTitle("Вхід до системи");
            currentStage.setScene(loginScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // Завантаження вікна для перегляду
    private void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));
            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(getClass().getResource("/styles/MainStyle.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Відкриття вікон
    @FXML
    public void openOrdersView(ActionEvent event) {
        if (userRole != null && userRole.getPosition().equals("WAITER")) {
            loadView("view/OrderView.fxml", "Управління Замовленнями");
        }
    }

    @FXML
    public void openKitchenView(ActionEvent event) {
        if (userRole != null && userRole.getPosition().equals("COOK")) {
            loadView("view/KitchenView.fxml", "Управління Кухнею");
        }
    }

    @FXML
    public void openInventoryView(ActionEvent event) {
        if (userRole != null && userRole.getPosition().equals("STOREKEEPER")) {
            loadView("view/InventoryView.fxml", "Облік Складу");
        }
    }

    @FXML
    public void openReportingView(ActionEvent event) {
        if (userRole != null && userRole.getPosition().equals("ANALYST")) {
            loadView("view/ReportingView.fxml", "Звітність та Аналітика");
        }
    }

    @FXML
    public void openMenuView(ActionEvent event) {
        if (userRole != null && userRole.getPosition().equals("MANAGER")) {
            loadView("view/MenuView.fxml", "Управління Меню");
        }
    }


    // Дія для кнопки "Вхід"
    @FXML
    public void loginAction(ActionEvent event) {
        loadView("view/LoginView.fxml", "Вхід до системи");
    }

}

