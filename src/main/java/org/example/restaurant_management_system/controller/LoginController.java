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

import org.example.restaurant_management_system.model.Employee;
import org.example.restaurant_management_system.service.EmployeeService;
import org.example.restaurant_management_system.exception.AuthenticationException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    @FXML
    public void handleLogin(ActionEvent event) throws IOException {
        try {
            int employeeId = Integer.parseInt(usernameField.getText());
            String password = passwordField.getText();

            Employee employee = EmployeeService.authenticateById(employeeId, password);

            if (employee != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/styles/MainStyle.css").toExternalForm());

                MainController mainController = loader.getController();
                mainController.setEmployee(employee);

                Stage stage = new Stage();
                stage.setTitle("Система управління рестораном");
                stage.setScene(scene);
                stage.show();

                ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
            }

        } catch (NumberFormatException e) {
            errorLabel.setText("ID має бути числом.");
        } catch (AuthenticationException ae) {
            errorLabel.setText(ae.getMessage());
        }
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
