package org.example.restaurant_management_system.controller;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.restaurant_management_system.model.Role;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;


    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Role authenticatedRole = authenticate(username, password);

        if (authenticatedRole != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/styles/MainStyle.css").toExternalForm());
                MainController mainController = loader.getController();
                mainController.setRole(authenticate(username, password));

                Stage stage = new Stage();
                stage.setTitle("Система управління рестораном");
                stage.setScene(scene);
                stage.show();

                // закриваємо вікно логіну
                ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            errorLabel.setText("Невірне ім’я користувача або пароль!");
        }
    }



    private Role authenticate(String username, String password) {
        return switch (username.toLowerCase()) {
            case "cook" -> password.equals("123") ? new Role(1, "COOK") : null;
            case "waiter" -> password.equals("123") ? new Role(2, "WAITER") : null;
            case "storekeeper" -> password.equals("123") ? new Role(3, "STOREKEEPER") : null;
            case "analyst" -> password.equals("123") ? new Role(4, "ANALYST") : null;
            case "manager" -> password.equals("admin") ? new Role(5, "MANAGER") : null;
            default -> null;
        };
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Успішний вхід");
        alert.setHeaderText(null);
        alert.setContentText("Вхід успішний! Ласкаво просимо.");
        alert.showAndWait();
    }

    private void showErrorAlert() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Помилка входу");
        alert.setHeaderText(null);
        alert.setContentText("Невірне ім'я користувача або пароль.");
        alert.showAndWait();
    }
}
