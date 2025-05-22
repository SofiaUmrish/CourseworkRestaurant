package org.example.restaurant_management_system.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;


public class OrderController {

    @FXML
    private TextField tableOrClientField;

    @FXML
    private ChoiceBox<String> orderTypeBox;

    @FXML
    private TextArea orderDetailsArea;

    @FXML
    private ChoiceBox<String> paymentMethodBox;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        orderTypeBox.setValue("Їжа");
        paymentMethodBox.setValue("Готівка");

        // Підключення CSS
        Platform.runLater(() -> {
            Scene scene = tableOrClientField.getScene();
            if (scene != null) {
                scene.getStylesheets().add(getClass().getResource("/styles/OrderStyle.css").toExternalForm());
            }
        });
    }

    @FXML
    private void handleSaveOrder() {
        String tableOrClient = tableOrClientField.getText().trim();
        String orderType = orderTypeBox.getValue();
        String details = orderDetailsArea.getText().trim();
        String payment = paymentMethodBox.getValue();

        if (tableOrClient.isEmpty() || details.isEmpty()) {
            statusLabel.setText("Будь ласка, заповніть усі поля.");
            return;
        }

        System.out.println("ЗАМОВЛЕННЯ:");
        System.out.println("Клієнт/Столик: " + tableOrClient);
        System.out.println("Тип: " + orderType);
        System.out.println("Деталі: " + details);
        System.out.println("Оплата: " + payment);

        statusLabel.setText("Замовлення збережено успішно.");
        tableOrClientField.clear();
        orderDetailsArea.clear();
    }

}
