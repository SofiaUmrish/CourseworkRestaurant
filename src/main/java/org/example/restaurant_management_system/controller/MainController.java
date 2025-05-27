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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MainController {

    private static final Logger LOGGER = LogManager.getLogger(MainController.class);

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
        LOGGER.info("Ініціалізовано MainController.");
        LOGGER.error("Тест критичної помилки для email!");

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
        String positionName = position.getName();

        usernameLabel.setText(positionName + ":\n\n" + employee.getFirstName() + " " + employee.getLastName());
        usernameLabel.setVisible(true);
        LOGGER.info("Встановлено співробітника: {} {}", employee.getFirstName(), employee.getLastName());

        switch (positionName) {
            case "Кухар":
                kitchenButton.setDisable(false);
                LOGGER.info("Увімкнено кнопку кухні для Кухаря.");
                break;
            case "Офіціант":
                ordersButton.setDisable(false);
                LOGGER.info("Увімкнено кнопку замовлень для Офіціанта.");
                break;
            case "Комірник":
                inventoryButton.setDisable(false);
                LOGGER.info("Увімкнено кнопку інвентаризації для Комірника.");
                break;
            case "Аналітик":
                reportingButton.setDisable(false);
                LOGGER.info("Увімкнено кнопку звітності для Аналітика.");
                break;
            case "Менеджер":
                ordersButton.setDisable(false);
                kitchenButton.setDisable(false);
                inventoryButton.setDisable(false);
                reportingButton.setDisable(false);
                menuButton.setDisable(false);
                LOGGER.info("Увімкнено всі кнопки для Менеджера.");
                break;
            default:
                LOGGER.warn("Невідома посада: {}", positionName);
        }

        loginTopButton.setVisible(false);
        logoutTopButton.setVisible(true);
    }

    @FXML
    public void logoutAction(ActionEvent event) {
        LOGGER.info("Ініційовано вихід із системи.");
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
        LOGGER.info("Кнопки вимкнено після виходу.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Scene loginScene = new Scene(loader.load());
            String css = getClass().getResource("/styles/LoginStyle.css").toExternalForm();
            loginScene.getStylesheets().add(css);

            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            currentStage.setTitle("Вхід до системи");
            currentStage.setScene(loginScene);
            LOGGER.info("Перехід до LoginView.");

        } catch (IOException e) {
            LOGGER.error("Не вдалося завантажити LoginView.fxml під час виходу: {}", e.getMessage(), e);
        }
    }

    private void loadView(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                LOGGER.error("FXML не знайдено за шляхом: {}", fxmlPath);
                System.err.println("FXML не знайдено за шляхом: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            String cssPath = "/styles/MainStyle.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                LOGGER.warn("MainStyle.css не знайдено за шляхом: {}", cssPath);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            LOGGER.info("Завантажено вигляд: {} з заголовком: {}", fxmlPath, title);
        } catch (IOException e) {
            LOGGER.error("Не вдалося завантажити вигляд {}: {}", fxmlPath, e.getMessage(), e);
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
            LOGGER.info("Спроба відкрити OrderView.fxml для співробітника: {}", employee.getPosition().getName());
            System.out.println("Спроба відкрити OrderView.fxml");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OrderView.fxml"));
                Parent view = loader.load();

                Scene scene = new Scene(view);

                String cssPath = "/styles/InventoryStyle.css";
                if (getClass().getResource(cssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                    LOGGER.debug("Завантажено CSS: {}", cssPath);
                } else {
                    LOGGER.warn("CSS файл не знайдено: {}", cssPath);
                }

                Stage stage = new Stage();
                stage.setTitle("Управління Замовленнями");
                stage.setScene(scene);
                stage.show();
                LOGGER.info("OrderView.fxml успішно відкрито.");

            } catch (IOException e) {
                LOGGER.error("Не вдалося завантажити OrderView.fxml: {}", e.getMessage(), e);
                System.err.println("Не вдалося завантажити OrderView.fxml");
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Несанкціонована спроба відкрити OrdersView співробітником з посадою: {}",
                    (employee != null ? employee.getPosition().getName() : "null"));
        }
    }


    @FXML
    public void openKitchenView(ActionEvent event) {
        if (employee != null && (
                employee.getPosition().getName().equalsIgnoreCase("Кухар") ||
                        employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            LOGGER.info("Спроба відкрити KitchenView.fxml для співробітника: {}", employee.getPosition().getName());
            System.out.println("Спроба відкрити KitchenView.fxml");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/KitchenView.fxml"));
                Parent view = loader.load();

                Scene scene = new Scene(view);

                String inventoryCssPath = "/styles/InventoryStyle.css";
                if (getClass().getResource(inventoryCssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(inventoryCssPath).toExternalForm());
                    LOGGER.debug("Завантажено CSS: {}", inventoryCssPath);
                    System.out.println("Завантажено CSS: " + inventoryCssPath);
                } else {
                    LOGGER.warn("CSS файл не знайдено: {}", inventoryCssPath);
                    System.err.println("CSS файл не знайдено: " + inventoryCssPath);
                }

                String kitchenCssPath = "/styles/KitchenStyle.css";
                if (getClass().getResource(kitchenCssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(kitchenCssPath).toExternalForm());
                    LOGGER.debug("Завантажено CSS: {}", kitchenCssPath);
                    System.out.println("Завантажено CSS: " + kitchenCssPath);
                } else {
                    LOGGER.warn("CSS файл не знайдено: {}", kitchenCssPath);
                    System.err.println("CSS файл не знайдено: " + kitchenCssPath);
                }


                Stage stage = new Stage();
                stage.setTitle("Управління Кухнею");
                stage.setScene(scene);
                stage.show();
                LOGGER.info("KitchenView.fxml успішно відкрито.");

            } catch (IOException e) {
                LOGGER.error("Не вдалося завантажити KitchenView.fxml: {}", e.getMessage(), e);
                System.err.println("Не вдалося завантажити KitchenView.fxml");
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Несанкціонована спроба відкрити KitchenView співробітником з посадою: {}",
                    (employee != null ? employee.getPosition().getName() : "null"));
        }
    }

    @FXML
    public void openInventoryView(ActionEvent event) {
        if (employee != null && (

                employee.getPosition().getName().equalsIgnoreCase("Комірник") ||
                        employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            LOGGER.info("Спроба відкрити InventoryView.fxml для співробітника: {}", employee.getPosition().getName());
            System.out.println("Спроба відкрити InventoryView.fxml");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/InventoryView.fxml"));
                Parent view = loader.load();

                Scene scene = new Scene(view);

                String cssPath = "/styles/InventoryStyle.css";
                if (getClass().getResource(cssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                    LOGGER.debug("Завантажено CSS: {}", cssPath);
                } else {
                    LOGGER.warn("CSS файл не знайдено: {}", cssPath);
                }

                Stage stage = new Stage();
                stage.setTitle("Облік складу і запасів ");
                stage.setScene(scene);
                stage.show();
                LOGGER.info("InventoryView.fxml успішно відкрито.");

            } catch (IOException e) {
                LOGGER.error("Не вдалося завантажити InventoryView.fxml: {}", e.getMessage(), e);
                System.err.println("Не вдалося завантажити InventoryView.fxml");
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Несанкціонована спроба відкрити InventoryView співробітником з посадою: {}",
                    (employee != null ? employee.getPosition().getName() : "null"));
        }
    }



    @FXML
    public void openReportingView(ActionEvent event) {
        if (employee != null && (
                employee.getPosition().getName().equalsIgnoreCase("Аналітик") ||
                        employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            LOGGER.info("Спроба відкрити ReportingView.fxml для співробітника: {}", employee.getPosition().getName());
            System.out.println("Спроба відкрити ReportingView.fxml");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ReportingView.fxml"));
                Parent view = loader.load();

                Scene scene = new Scene(view);

                String inventoryCssPath = "/styles/InventoryStyle.css";
                if (getClass().getResource(inventoryCssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(inventoryCssPath).toExternalForm());
                    LOGGER.debug("Завантажено CSS: {}", inventoryCssPath);
                    System.out.println("Завантажено CSS: " + inventoryCssPath);
                } else {
                    LOGGER.warn("CSS файл не знайдено: {}", inventoryCssPath);
                }

                String kitchenCssPath = "/styles/ReportingStyle.css";
                if (getClass().getResource(kitchenCssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(kitchenCssPath).toExternalForm());
                    LOGGER.debug("Завантажено CSS: {}", kitchenCssPath);
                    System.out.println("Завантажено CSS: " + kitchenCssPath);
                } else {
                    LOGGER.warn("CSS файл не знайдено: {}", kitchenCssPath);
                }


                Stage stage = new Stage();
                stage.setTitle("Звітність та Аналітика");
                stage.setScene(scene);
                stage.show();
                LOGGER.info("ReportingView.fxml успішно відкрито.");

            } catch (IOException e) {
                LOGGER.error("Не вдалося завантажити ReportingView.fxml: {}", e.getMessage(), e);
                System.err.println("Не вдалося завантажити ReportingView.fxml");
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Несанкціонована спроба відкрити ReportingView співробітником з посадою: {}",
                    (employee != null ? employee.getPosition().getName() : "null"));
        }
    }


    @FXML
    public void openMenuView(ActionEvent event) {
        if (employee != null && (

                employee.getPosition().getName().equalsIgnoreCase("Менеджер")
        )) {
            LOGGER.info("Спроба відкрити MenuView.fxml для співробітника: {}", employee.getPosition().getName());
            System.out.println("Спроба відкрити MenuView.fxml");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MenuView.fxml"));
                Parent view = loader.load();

                Scene scene = new Scene(view);

                String cssPath = "/styles/MenuStyle.css";
                if (getClass().getResource(cssPath) != null) {
                    scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                    LOGGER.debug("Завантажено CSS: {}", cssPath);
                } else {
                    LOGGER.warn("CSS файл не знайдено: {}", cssPath);
                }

                Stage stage = new Stage();
                stage.setTitle("Управління Меню");
                stage.setScene(scene);
                stage.show();
                LOGGER.info("MenuView.fxml успішно відкрито.");

            } catch (IOException e) {
                LOGGER.error("Не вдалося завантажити MenuView.fxml: {}", e.getMessage(), e);
                System.err.println("Не вдалося завантажити MenuView.fxml");
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Несанкціонована спроба відкрити MenuView співробітником з посадою: {}",
                    (employee != null ? employee.getPosition().getName() : "null"));
        }
    }



    @FXML
    public void loginAction(ActionEvent event) {
        LOGGER.info("Ініційовано дію входу.");
        loadView("view/LoginView.fxml", "Вхід до системи");
    }
}