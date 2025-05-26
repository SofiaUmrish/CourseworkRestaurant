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

public class ReportingController {

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


    @FXML
    public void initialize() {
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
    }

    //завантаження фільтрів
    private void loadSalesFilters() {
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
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Помилка завантаження фільтрів", "Не вдалося завантажити дані для фільтрів: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }


    //методи для видимості секцій
    @FXML
    private void showSalesSection() {
        salesSection.setVisible(true);
        salesSection.setManaged(true); //видимість у вікні

        popularitySection.setVisible(false);
        popularitySection.setManaged(false);

        expensesSection.setVisible(false);
        expensesSection.setManaged(false);
    }

    @FXML
    private void showPopularitySection() {
        salesSection.setVisible(false);
        salesSection.setManaged(false);

        popularitySection.setVisible(true);
        popularitySection.setManaged(true);

        expensesSection.setVisible(false);
        expensesSection.setManaged(false);
    }

    @FXML
    private void showExpensesSection() {
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
        LocalDate startDate = salesStartDatePicker.getValue();
        LocalDate endDate = salesEndDatePicker.getValue();
        String periodType = salesPeriodComboBox.getValue();
        String category = salesCategoryComboBox.getValue();
        String menuItem = salesMenuItemComboBox.getValue();
        String employee = salesEmployeeComboBox.getValue();

        if (startDate == null || endDate == null || periodType == null) {
            showAlert(Alert.AlertType.WARNING, "Відсутні дані", "Будь ласка, оберіть початкову та кінцеву дати, а також тип періоду.");
            return;
        }

        if ("Усі категорії".equals(category)) category = null;
        if ("Усі позиції".equals(menuItem)) menuItem = null;
        if ("Усі офіціанти".equals(employee)) employee = null;

        try {
            List<ReportingService.SaleReportEntry> salesData = reportingService.getSalesByPeriod(startDate, endDate, periodType, category, menuItem, employee);
            salesTableView.setItems(FXCollections.observableArrayList(salesData));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка генерації звіту", "Не вдалося згенерувати звіт про продажі: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //генерування популярних позицій з меню
    @FXML
    private void handleGeneratePopularity() {
        LocalDate startDate = popularityStartDatePicker.getValue();
        LocalDate endDate = popularityEndDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Відсутні дані", "Будь ласка, оберіть початкову та кінцеву дати.");
            return;
        }

        try {
            List<ReportingService.PopularMenuItemReportEntry> popularityData = reportingService.getPopularMenuItems(startDate, endDate);
            popularityTableView.setItems(FXCollections.observableArrayList(popularityData));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка генерації рейтингу", "Не вдалося згенерувати рейтинг популярності: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //генерування про витрати і прибуток
    @FXML
    private void handleGenerateExpenses() {
        LocalDate startDate = expensesStartDatePicker.getValue();
        LocalDate endDate = expensesEndDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Відсутні дані", "Будь ласка, оберіть початкову та кінцеву дати.");
            return;
        }

        try {
            ReportingService.FinancialSummary summary = reportingService.getFinancialSummary(startDate, endDate);
            totalSalesLabel.setText(String.format("Загальний обсяг продажів: %.2f грн", summary.getTotalSales()));
            procurementCostsLabel.setText(String.format("Витрати на закупівлю (30%% від продажів): %.2f грн", summary.getProcurementCosts()));
            maintenanceCostsLabel.setText(String.format("Витрати на утримання (Зарплати): %.2f грн", summary.getMaintenanceCosts()));
            totalExpensesLabel.setText(String.format("Загальні витрати: %.2f грн", summary.getTotalExpenses()));
            netProfitLabel.setText(String.format("Чистий прибуток: %.2f грн", summary.getNetProfit()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка генерації звіту", "Не вдалося згенерувати звіт про витрати: " + e.getMessage());
            e.printStackTrace();
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
    }
}