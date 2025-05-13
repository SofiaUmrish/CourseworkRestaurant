package org.example.restaurant_management_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
