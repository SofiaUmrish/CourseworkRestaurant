package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ReportingService {

    public static class SaleReportEntry {
        // запис у звіті про продажі
        private String dateOrPeriod;
        private String categoryName;
        private String menuItemName;
        private String employeeName;
        private double totalAmount;

        //новий запис звіту про продажі
        public SaleReportEntry(String dateOrPeriod, String categoryName, String menuItemName, String employeeName, double totalAmount) {
            this.dateOrPeriod = dateOrPeriod;
            this.categoryName = categoryName;
            this.menuItemName = menuItemName;
            this.employeeName = employeeName;
            this.totalAmount = totalAmount;
        }

        public String getDateOrPeriod() { return dateOrPeriod; }
        public String getCategoryName() { return categoryName; }
        public String getMenuItemName() { return menuItemName; }
        public String getEmployeeName() { return employeeName; }
        public double getTotalAmount() { return totalAmount; }
    }

    public static class PopularMenuItemReportEntry {
        //запис у звіті про популярні позиції меню
        private String menuItemName;
        private int quantitySold;

        //новий запис про популярну позицію меню
        public PopularMenuItemReportEntry(String menuItemName, int quantitySold) {
            this.menuItemName = menuItemName;
            this.quantitySold = quantitySold;
        }

        public String getMenuItemName() { return menuItemName; }
        public int getQuantitySold() { return quantitySold; }
    }

    public static class FinancialSummary {
        //звіт з доходами і витратами
        private double totalSales;
        private double procurementCosts;
        private double maintenanceCosts;
        private double totalExpenses;
        private double netProfit;

        //звіт
        public FinancialSummary(double totalSales, double procurementCosts, double maintenanceCosts) {
            this.totalSales = totalSales;
            this.procurementCosts = procurementCosts;
            this.maintenanceCosts = maintenanceCosts;
            this.totalExpenses = procurementCosts + maintenanceCosts;
            this.netProfit = totalSales - this.totalExpenses;
        }

        public double getTotalSales() { return totalSales; }
        public double getProcurementCosts() { return procurementCosts; }
        public double getMaintenanceCosts() { return maintenanceCosts; }
        public double getTotalExpenses() { return totalExpenses; }
        public double getNetProfit() { return netProfit; }
    }

    // отримує список записів про продажі за вказаний період з фільтрацією
    public List<SaleReportEntry> getSalesByPeriod(LocalDate startDate, LocalDate endDate,
                                                  String periodType, String categoryName,
                                                  String menuItemName, String employeeName) throws SQLException {
        List<SaleReportEntry> sales = new ArrayList<>();
        String sql = buildSalesQuery(periodType, categoryName, menuItemName, employeeName);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            pstmt.setDate(paramIndex++, Date.valueOf(startDate));
            pstmt.setDate(paramIndex++, Date.valueOf(endDate));

            if (categoryName != null && !categoryName.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + categoryName + "%");
            }
            if (menuItemName != null && !menuItemName.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + menuItemName + "%");
            }
            if (employeeName != null && !employeeName.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + employeeName + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String period = rs.getString("period_label");
                String cat = rs.getString("category_name");
                String item = rs.getString("item_name");
                String emp = rs.getString("employee_name");
                double total = rs.getDouble("total_sales");
                sales.add(new SaleReportEntry(period, cat, item, emp, total));
            }
        }
        return sales;
    }

    //отримання даних про продажі з урахуванням фільтрів
    private String buildSalesQuery(String periodType, String categoryName, String menuItemName, String employeeName) {
        StringBuilder sql = new StringBuilder();

        String periodExpression;
        switch (periodType.toUpperCase()) {
            case "ТИЖДЕНЬ":
                String weekStart = "DATE_SUB(o.order_time, INTERVAL DAYOFWEEK(o.order_time)-1 DAY)";
                periodExpression = "CONCAT(DATE_FORMAT(" + weekStart + ", '%Y-%m-%d'), ' - ', DATE_FORMAT(DATE_ADD(" + weekStart + ", INTERVAL 6 DAY), '%Y-%m-%d'))";
                break;
            case "МІСЯЦЬ":
                periodExpression = "DATE_FORMAT(o.order_time, '%Y-%m')";
                break;
            case "ДЕНЬ":
            default:
                periodExpression = "DATE_FORMAT(o.order_time, '%Y-%m-%d')";
                break;
        }

        String categoryExpression = "COALESCE(c.name, 'Без категорії')";
        String employeeExpression = "COALESCE(CONCAT_WS(' ', e.first_name, e.last_name), 'Невідомий офіціант')";

        sql.append("SELECT ")
                .append(periodExpression).append(" AS period_label, ")
                .append(categoryExpression).append(" AS category_name, ")
                .append("mi.name AS item_name, ")
                .append(employeeExpression).append(" AS employee_name, ")
                .append("SUM(oi.quantity * mi.price) AS total_sales ");

        sql.append("FROM orders o ")
                .append("JOIN order_items oi ON o.id = oi.order_id ")
                .append("JOIN menu_items mi ON oi.menu_item_id = mi.id ")
                .append("LEFT JOIN categories c ON mi.category_id = c.id ")
                .append("LEFT JOIN employees e ON o.employee_id = e.id ");

        sql.append("WHERE DATE(o.order_time) BETWEEN ? AND ? ");

        if (categoryName != null && !categoryName.isEmpty()) {
            sql.append("AND (").append(categoryExpression).append(") LIKE ? ");
        }
        if (menuItemName != null && !menuItemName.isEmpty()) {
            sql.append("AND mi.name LIKE ? ");
        }
        if (employeeName != null && !employeeName.isEmpty()) {
            sql.append("AND (").append(employeeExpression).append(") LIKE ? ");
        }

        sql.append("GROUP BY period_label, category_name, item_name, employee_name ");

        sql.append("ORDER BY period_label, total_sales DESC");

        return sql.toString();
    }

    //список найпопулярніших позицій меню за вказаний період
    public List<PopularMenuItemReportEntry> getPopularMenuItems(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<PopularMenuItemReportEntry> popularItems = new ArrayList<>();
        String sql = "SELECT mi.name AS item_name, SUM(oi.quantity) AS quantity_sold " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.id " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                "WHERE DATE(o.order_time) BETWEEN ? AND ? " +
                "GROUP BY mi.name " +
                "ORDER BY quantity_sold DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String itemName = rs.getString("item_name");
                int quantity = rs.getInt("quantity_sold");
                popularItems.add(new PopularMenuItemReportEntry(itemName, quantity));
            }
        }
        return popularItems;
    }

    //звіт за вказаний період
    public FinancialSummary getFinancialSummary(LocalDate startDate, LocalDate endDate) throws SQLException {
        // враховує бонуси з total_amount таблиці orders
        double totalSales = calculateTotalSales(startDate, endDate);

        // для procurementCosts функція яка рахує суму без знижок
        double grossSalesForProcurement = calculateGrossSalesForProcurement(startDate, endDate);
        double procurementCosts = grossSalesForProcurement * 0.30; // 30% від суми без знижок

        long monthsBetween = ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1).plusMonths(1));
        if (monthsBetween == 0) monthsBetween = 1;

        double totalMonthlySalariesForAllPositions = calculateTotalMonthlySalariesBasedOnEmployeeCount();
        double maintenanceCosts = totalMonthlySalariesForAllPositions * monthsBetween;

        return new FinancialSummary(totalSales, procurementCosts, maintenanceCosts);
    }

    //загальна сума продажів за вказаний період
    private double calculateTotalSales(LocalDate startDate, LocalDate endDate) throws SQLException {
        double totalSales = 0.0;
        String sql = "SELECT SUM(o.total_amount) AS total_sales " +
                "FROM orders o " +
                "WHERE DATE(o.order_time) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalSales = rs.getDouble("total_sales");
            }
        }
        return totalSales;
    }

    //сума продажів без знижок для витрат на закупівлю
    private double calculateGrossSalesForProcurement(LocalDate startDate, LocalDate endDate) throws SQLException {
        double grossSales = 0.0;
        String sql = "SELECT SUM(oi.quantity * mi.price) AS gross_sales_amount " +
                "FROM orders o " +
                "JOIN order_items oi ON o.id = oi.order_id " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                "WHERE DATE(o.order_time) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                grossSales = rs.getDouble("gross_sales_amount");
            }
        }
        return grossSales;
    }

    //щомісячні зарплати для всіх працівників
    private double calculateTotalMonthlySalariesBasedOnEmployeeCount() throws SQLException {
        double totalMonthlySalaries = 0.0;
        String sql = "SELECT SUM(s.monthly_salary * (SELECT COUNT(e.id)" +
                " FROM employees e WHERE e.position_id = p.id))" +
                " AS total_salaries " +
                "FROM positions p " +
                "JOIN salaries s ON p.id = s.positions_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                totalMonthlySalaries = rs.getDouble("total_salaries");
            }
        }
        return totalMonthlySalaries;
    }

    //список усіх категорій страв
    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        }
        return categories;
    }

    //список усіх позицій меню
    public List<String> getAllMenuItems() throws SQLException {
        List<String> menuItems = new ArrayList<>();
        String sql = "SELECT name FROM menu_items ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                menuItems.add(rs.getString("name"));
            }
        }
        return menuItems;
    }

    //список усіх офіціантів
    public List<String> getAllEmployees() throws SQLException {
        List<String> employees = new ArrayList<>();
        String sql = "SELECT first_name, last_name FROM employees e JOIN positions p ON e.position_id = p.id " +
                "WHERE p.position_name = 'Офіціант' ORDER BY first_name, last_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                employees.add(rs.getString("first_name") + " " + rs.getString("last_name"));
            }
        }
        return employees;
    }
}