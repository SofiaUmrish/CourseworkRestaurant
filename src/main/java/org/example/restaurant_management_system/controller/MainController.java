package org.example.restaurant_management_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.restaurant_management_system.model.Employee;
import org.example.restaurant_management_system.model.Position;

import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML private Button ordersButton;
    @FXML private Button kitchenButton;
    @FXML private Button inventoryButton;
    @FXML private Button reportingButton;
    @FXML private Button menuButton;
    @FXML private Button loginTopButton;
    @FXML private Button logoutTopButton;
    @FXML private Label usernameLabel;

    private Employee employee;

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

    public void setEmployee(Employee employee) {
        this.employee = employee;
        Position position = employee.getPosition();
        String positionName = position.getName();  // виправлено тут

        usernameLabel.setText(positionName + ":\n\n" + employee.getFirstName() + " " + employee.getLastName());
        usernameLabel.setVisible(true);

        switch (positionName) {
            case "Кухар":
                kitchenButton.setDisable(false);
                break;
            case "Офіціант":
                ordersButton.setDisable(false);
                break;
            case "Комірник":
                inventoryButton.setDisable(false);
                break;
            case "Аналітик":
                reportingButton.setDisable(false);
                break;
            case "Менеджер":
                ordersButton.setDisable(false);
                kitchenButton.setDisable(false);
                inventoryButton.setDisable(false);
                reportingButton.setDisable(false);
                menuButton.setDisable(false);
                break;
        }

        loginTopButton.setVisible(false);
        logoutTopButton.setVisible(true);
    }

    @FXML
    public void logoutAction(ActionEvent event) {
        employee = null;
        usernameLabel.setText("");
        usernameLabel.setVisible(false);

        loginTopButton.setVisible(true);
        logoutTopButton.setVisible(false);

        ordersButton.setDisable(true);
        kitchenButton.setDisable(true);
        inventoryButton.setDisable(true);
        reportingButton.setDisable(true);
        menuButton.setDisable(true);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Scene loginScene = new Scene(loader.load());
            String css = getClass().getResource("/styles/LoginStyle.css").toExternalForm();
            loginScene.getStylesheets().add(css);

            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.setTitle("Вхід до системи");
            currentStage.setScene(loginScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML не знайдено за шляхом: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Додаємо стиль, якщо потрібно
            String cssPath = "/styles/MainStyle.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Не вдалося завантажити: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    public void openOrdersView(ActionEvent event) {
        if (employee != null && (
                employee.getPosition().getName().equalsIgnoreCase("Офіціант") ||
                        employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            System.out.println("Спроба відкрити OrderView.fxml");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderView.fxml"));
                Parent view = loader.load();

                // Створюємо нову сцену і вікно
                Scene scene = new Scene(view);

                // Додаємо стиль, якщо є
                String cssPath = "/styles/MainStyle.css";
                if (getClass().getResource(cssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                }

                Stage stage = new Stage();
                stage.setTitle("Управління Замовленнями");
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                System.err.println("Не вдалося завантажити OrderView.fxml");
                e.printStackTrace();
            }
        }
    }


    @FXML
    public void openKitchenView(ActionEvent event) {
        if (employee != null && (
                employee.getPosition().getName().equalsIgnoreCase("Кухар") ||
                        employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            loadView("view/KitchenView.fxml", "Управління Кухнею");
        }
    }

    @FXML
    public void openInventoryView(ActionEvent event) {
        if (employee != null && (
                employee.getPosition().getName().equalsIgnoreCase("Комірник") ||
                        employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            loadView("view/InventoryView.fxml", "Облік Складу");
        }
    }

    @FXML
    public void openReportingView(ActionEvent event) {
        if (employee != null && (
                employee.getPosition().getName().equalsIgnoreCase("Аналітик") ||
                        employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            loadView("view/ReportingView.fxml", "Звітність та Аналітика");
        }
    }

    @FXML
    public void openMenuView(ActionEvent event) {
        if (employee != null && (

                        employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            System.out.println("Спроба відкрити MenuView.fxml");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuView.fxml"));
                Parent view = loader.load();

                // Створюємо нову сцену і вікно
                Scene scene = new Scene(view);

                // Додаємо стиль, якщо є
                String cssPath = "/styles/MenuStyle.css";
                if (getClass().getResource(cssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                }

                Stage stage = new Stage();
                stage.setTitle("Управління Меню");
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                System.err.println("Не вдалося завантажити MenuView.fxml");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void loginAction(ActionEvent event) {
        loadView("view/LoginView.fxml", "Вхід до системи");
    }
}
