package org.example.restaurant_management_system.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.example.restaurant_management_system.service.ReportingService;
import javafx.application.Platform;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ReportingController  {

    private static final Logger LOGGER = LogManager.getLogger(ReportingController.class);

    private ReportingService reportingService;

    //секція звіт про продажі
    @FXML private VBox salesSection; //керування видимістю секції
    @FXML private DatePicker salesStartDatePicker;
    @FXML private DatePicker salesEndDatePicker;
    @FXML private ComboBox<String> salesPeriodComboBox;
    @FXML private ComboBox<String> salesCategoryComboBox;
    @FXML private ComboBox<String> salesMenuItemComboBox;
    @FXML private ComboBox<String> salesEmployeeComboBox;
    @FXML private Button generateSalesButton;
    @FXML private TableView<ReportingService.SaleReportEntry> salesTableView;
    @FXML private TableColumn<ReportingService.SaleReportEntry, String> salesDateColumn;
    @FXML private TableColumn<ReportingService.SaleReportEntry, String> salesCategoryCol;
    @FXML private TableColumn<ReportingService.SaleReportEntry, String> salesMenuItemCol;
    @FXML private TableColumn<ReportingService.SaleReportEntry, String> salesEmployeeCol;
    @FXML private TableColumn<ReportingService.SaleReportEntry, Double> salesTotalAmountCol;

    //секція рейтинг популярних позицій
    @FXML private VBox popularitySection; //керування видимістю секції
    @FXML private DatePicker popularityStartDatePicker;
    @FXML private DatePicker popularityEndDatePicker;
    @FXML private Button generatePopularityButton;
    @FXML private TableView<ReportingService.PopularMenuItemReportEntry> popularityTableView;
    @FXML private TableColumn<ReportingService.PopularMenuItemReportEntry, String> popularityMenuItemCol;
    @FXML private TableColumn<ReportingService.PopularMenuItemReportEntry, Integer> popularityQuantityCol;

    //секція звіт про витрати та прибуток
    @FXML private VBox expensesSection; //керування видимістю секції
    @FXML private DatePicker expensesStartDatePicker;
    @FXML private DatePicker expensesEndDatePicker;
    @FXML private Button generateExpensesButton;
    @FXML private Label totalSalesLabel;
    @FXML private Label procurementCostsLabel;
    @FXML private Label maintenanceCostsLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netProfitLabel;



    public void initialize() {
        LOGGER.info("Ініціалізація ReportingController...");

        reportingService = new ReportingService();

        // Ініціалізація, як і раніше
        salesPeriodComboBox.setItems(FXCollections.observableArrayList("День", "Тиждень", "Місяць"));
        salesPeriodComboBox.getSelectionModel().selectFirst();
        salesTableView.setPlaceholder(new Label("Згенеруйте дані"));
        popularityTableView.setPlaceholder(new Label("Згенеруйте дані"));

        loadSalesFilters();

        salesDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateOrPeriod"));
        salesCategoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        salesMenuItemCol.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        salesEmployeeCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        salesTotalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        popularityMenuItemCol.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        popularityQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));

        LocalDate today = LocalDate.now();
        salesStartDatePicker.setValue(today.withDayOfMonth(1));
        salesEndDatePicker.setValue(today);
        popularityStartDatePicker.setValue(today.withDayOfMonth(1));
        popularityEndDatePicker.setValue(today);
        expensesStartDatePicker.setValue(today.withDayOfMonth(1));
        expensesEndDatePicker.setValue(today);

        //секція продажів за замовчуванням
        showSalesSection();
        LOGGER.info("ReportingController успішно ініціалізовано.");
    }

    //завантаження фільтрів
    private void loadSalesFilters() {
        LOGGER.info("Завантаження фільтрів для звіту про продажі...");
        new Thread(() -> {
            try {
                List<String> categories = reportingService.getAllCategories();
                List<String> menuItems = reportingService.getAllMenuItems();
                List<String> employees = reportingService.getAllEmployees();

                Platform.runLater(() -> {
                    salesCategoryComboBox.setItems(FXCollections.observableArrayList(categories));
                    salesMenuItemComboBox.setItems(FXCollections.observableArrayList(menuItems));
                    salesEmployeeComboBox.setItems(FXCollections.observableArrayList(employees));

                    salesCategoryComboBox.getItems().add(0, "Усі категорії");
                    salesCategoryComboBox.getSelectionModel().select("Усі категорії");

                    salesMenuItemComboBox.getItems().add(0, "Усі позиції");
                    salesMenuItemComboBox.getSelectionModel().select("Усі позиції");

                    salesEmployeeComboBox.getItems().add(0, "Усі офіціанти");
                    salesEmployeeComboBox.getSelectionModel().select("Усі офіціанти");
                    LOGGER.info("Фільтри для звіту про продажі завантажено та встановлено успішно.");
                });
            } catch (SQLException e) {
                LOGGER.error( "Помилка SQL при завантаженні фільтрів для продажів: " + e.getMessage(), e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Помилка завантаження фільтрів", "Не вдалося завантажити дані для фільтрів: " + e.getMessage()));
            } catch (Exception e) { // Ловимо також загальні винятки
                LOGGER.error("Невідома помилка при завантаженні фільтрів для продажів: " + e.getMessage(), e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Помилка завантаження фільтрів", "Не вдалося завантажити дані для фільтрів через невідому помилку."));
            }
        }).start();
    }


    //методи для видимості секцій
    @FXML
    private void showSalesSection() {
        LOGGER.info("Перемикання на секцію 'Звіт про продажі'.");
        salesSection.setVisible(true);
        salesSection.setManaged(true); //видимість у вікні

        popularitySection.setVisible(false);
        popularitySection.setManaged(false);

        expensesSection.setVisible(false);
        expensesSection.setManaged(false);
    }

    @FXML
    private void showPopularitySection() {
        LOGGER.info("Перемикання на секцію 'Рейтинг популярних позицій'.");
        salesSection.setVisible(false);
        salesSection.setManaged(false);

        popularitySection.setVisible(true);
        popularitySection.setManaged(true);

        expensesSection.setVisible(false);
        expensesSection.setManaged(false);
    }

    @FXML
    private void showExpensesSection() {
        LOGGER.info("Перемикання на секцію 'Звіт про витрати та прибуток'.");
        salesSection.setVisible(false);
        salesSection.setManaged(false);

        popularitySection.setVisible(false);
        popularitySection.setManaged(false);

        expensesSection.setVisible(true);
        expensesSection.setManaged(true);
    }

    //генерування продажів
    @FXML
    private void handleGenerateSales() {
        LOGGER.info("Спроба згенерувати звіт про продажі.");
        LocalDate startDate = salesStartDatePicker.getValue();
        LocalDate endDate = salesEndDatePicker.getValue();
        String periodType = salesPeriodComboBox.getValue();
        String category = salesCategoryComboBox.getValue();
        String menuItem = salesMenuItemComboBox.getValue();
        String employee = salesEmployeeComboBox.getValue();

        if (startDate == null || endDate == null || periodType == null) {
            showAlert(Alert.AlertType.WARNING, "Відсутні дані", "Будь ласка, оберіть початкову та кінцеву дати, а також тип періоду.");
            LOGGER.error("Генерація звіту про продажі скасована: відсутні обов'язкові дані (дати або тип періоду).");
            return;
        }

        // Логуємо обрані параметри фільтрації
        LOGGER.info(String.format("Параметри звіту про продажі: Початок=%s, Кінець=%s, Період=%s, Категорія=%s, Позиція=%s, Офіціант=%s",
                startDate, endDate, periodType, category, menuItem, employee));


        if ("Усі категорії".equals(category)) category = null;
        if ("Усі позиції".equals(menuItem)) menuItem = null;
        if ("Усі офіціанти".equals(employee)) employee = null;

        try {
            List<ReportingService.SaleReportEntry> salesData = reportingService.getSalesByPeriod(startDate, endDate, periodType, category, menuItem, employee);
            salesTableView.setItems(FXCollections.observableArrayList(salesData));
            LOGGER.info("Звіт про продажі успішно згенеровано. Кількість записів: " + salesData.size());
        } catch (SQLException e) {
            LOGGER.error( "Помилка SQL при генерації звіту про продажі: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Помилка генерації звіту", "Не вдалося згенерувати звіт про продажі: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Невідома помилка при генерації звіту про продажі: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Помилка генерації звіту", "Не вдалося згенерувати звіт про продажі через невідому помилку.");
        }
    }

    //генерування популярних позицій з меню
    @FXML
    private void handleGeneratePopularity() {
        LOGGER.info("Спроба згенерувати рейтинг популярних позицій.");
        LocalDate startDate = popularityStartDatePicker.getValue();
        LocalDate endDate = popularityEndDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Відсутні дані", "Будь ласка, оберіть початкову та кінцеву дати.");
            LOGGER.error("Генерація рейтингу популярності скасована: відсутні дати.");
            return;
        }

        LOGGER.info(String.format("Параметри рейтингу популярності: Початок=%s, Кінець=%s", startDate, endDate));

        try {
            List<ReportingService.PopularMenuItemReportEntry> popularityData = reportingService.getPopularMenuItems(startDate, endDate);
            popularityTableView.setItems(FXCollections.observableArrayList(popularityData));
            LOGGER.info("Рейтинг популярних позицій успішно згенеровано. Кількість записів: " + popularityData.size());
        } catch (SQLException e) {
            LOGGER.error( "Помилка SQL при генерації рейтингу популярності: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Помилка генерації рейтингу", "Не вдалося згенерувати рейтинг популярності: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error( "Невідома помилка при генерації рейтингу популярності: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Помилка генерації рейтингу", "Не вдалося згенерувати рейтинг популярності через невідому помилку.");
        }
    }

    //генерування про витрати і прибуток
    @FXML
    private void handleGenerateExpenses() {
        LOGGER.info("Спроба згенерувати звіт про витрати та прибуток.");
        LocalDate startDate = expensesStartDatePicker.getValue();
        LocalDate endDate = expensesEndDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Відсутні дані", "Будь ласка, оберіть початкову та кінцеву дати.");
            LOGGER.error("Генерація звіту про витрати скасована: відсутні дати.");
            return;
        }

        LOGGER.info(String.format("Параметри звіту про витрати: Початок=%s, Кінець=%s", startDate, endDate));

        try {
            ReportingService.FinancialSummary summary = reportingService.getFinancialSummary(startDate, endDate);
            totalSalesLabel.setText(String.format("Загальний обсяг продажів: %.2f грн", summary.getTotalSales()));
            procurementCostsLabel.setText(String.format("Витрати на закупівлю (30%% від продажів): %.2f грн", summary.getProcurementCosts()));
            maintenanceCostsLabel.setText(String.format("Витрати на утримання (Зарплати): %.2f грн", summary.getMaintenanceCosts()));
            totalExpensesLabel.setText(String.format("Загальні витрати: %.2f грн", summary.getTotalExpenses()));
            netProfitLabel.setText(String.format("Чистий прибуток: %.2f грн", summary.getNetProfit()));
            LOGGER.info(String.format("Звіт про витрати та прибуток успішно згенеровано: Продажі=%.2f, Витрати на закупівлю=%.2f, Витрати на утримання=%.2f, Загальні витрати=%.2f, Чистий прибуток=%.2f",
                    summary.getTotalSales(), summary.getProcurementCosts(), summary.getMaintenanceCosts(), summary.getTotalExpenses(), summary.getNetProfit()));
        } catch (SQLException e) {
            LOGGER.error( "Помилка SQL при генерації звіту про витрати та прибуток: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Помилка генерації звіту", "Не вдалося згенерувати звіт про витрати: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error( "Невідома помилка при генерації звіту про витрати та прибуток: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Помилка генерації звіту", "Не вдалося згенерувати звіт про витрати через невідому помилку.");
        }
    }

    //для відображення помилок
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
        // Логуємо показ Alert для відстеження взаємодії з користувачем
        LOGGER.error( String.format("Показано Alert користувачеві: Тип=%s, Заголовок='%s', Повідомлення='%s'", type, title, message));
    }
}