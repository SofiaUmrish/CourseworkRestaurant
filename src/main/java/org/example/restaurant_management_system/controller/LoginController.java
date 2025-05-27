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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginController {

    private static final Logger LOGGER = LogManager.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        LOGGER.debug("MainController встановлено в LoginController.");
    }


    @FXML
    public void handleLogin(ActionEvent event) throws IOException {
        LOGGER.info("Спроба входу.");
        try {
            int employeeId = Integer.parseInt(usernameField.getText());
            String password = passwordField.getText();

            LOGGER.debug("Спроба аутентифікації для ID співробітника: {}", employeeId);
            Employee employee = EmployeeService.authenticateById(employeeId, password);

            if (employee != null) {
                LOGGER.info("Аутентифікація успішна для співробітника: {} {}",
                        employee.getFirstName(), employee.getLastName());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource("/styles/MainStyle.css").toExternalForm());
                LOGGER.debug("Завантажено MainView.fxml та MainStyle.css.");

                MainController mainController = loader.getController();
                mainController.setEmployee(employee);

                Stage stage = new Stage();
                stage.setTitle("Система управління рестораном");
                stage.setScene(scene);
                stage.show();
                LOGGER.info("Відкрито головне вікно системи.");

                ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
                LOGGER.info("Вікно входу закрито.");
            } else {
                LOGGER.warn("Аутентифікація не вдалася для ID співробітника: {}", employeeId);
                errorLabel.setText("Невідома помилка входу. Спробуйте ще раз.");
            }

        } catch (NumberFormatException e) {
            LOGGER.error("Помилка формату ID при вході: {}", usernameField.getText(), e);
            errorLabel.setText("ID має бути числом.");
        } catch (AuthenticationException ae) {
            LOGGER.warn("Помилка аутентифікації: {}", ae.getMessage());
            errorLabel.setText(ae.getMessage());
        } catch (IOException e) {
            LOGGER.error("Помилка завантаження MainView.fxml: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Непередбачена помилка під час входу: {}", e.getMessage(), e);
            errorLabel.setText("Виникла непередбачена помилка. Спробуйте пізніше.");
        }
    }

    private void showSuccessAlert() {
        LOGGER.info("Відображення сповіщення про успішний вхід.");
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Успішний вхід");
        alert.setHeaderText(null);
        alert.setContentText("Вхід успішний! Ласкаво просимо.");
        alert.showAndWait();
    }

    private void showErrorAlert() {
        LOGGER.warn("Відображення сповіщення про помилку входу.");
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Помилка входу");
        alert.setHeaderText(null);
        alert.setContentText("Невірне ім'я користувача або пароль.");
        alert.showAndWait();
    }
}