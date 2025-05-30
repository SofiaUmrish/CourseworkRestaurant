package org.example.restaurant_management_system.service;

import org.example.restaurant_management_system.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReportingServiceTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private Statement mockStatement;
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;

    private ReportingService reportingService;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockStatement = mock(Statement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        reportingService = new ReportingService();
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseConnection.close();
    }

    @Test
    @DisplayName("Test getSalesByPeriod with daily period and all filters null")
    void getSalesByPeriod_Daily_AllFiltersNull() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 2);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("period_label")).thenReturn("2023-01-01",
                "2023-01-02");

        when(mockResultSet.getString("category_name")).thenReturn("Напої",
                "Десерти");

        when(mockResultSet.getString("item_name")).thenReturn("Кава", "Торт");

        when(mockResultSet.getString("employee_name")).thenReturn("Іван Петренко",
                "Ольга Іваненко");

        when(mockResultSet.getDouble("total_sales")).thenReturn(100.0, 150.0);

        List<ReportingService.SaleReportEntry> result = reportingService.getSalesByPeriod(
                startDate, endDate, "ДЕНЬ", null, null,
                null
        );

        assertEquals(2, result.size());
        assertEquals("2023-01-01", result.get(0).getDateOrPeriod());
        assertEquals("Кава", result.get(0).getMenuItemName());
        assertEquals(100.0, result.get(0).getTotalAmount());

        verify(mockPreparedStatement).setDate(1, Date.valueOf(startDate));
        verify(mockPreparedStatement).setDate(2, Date.valueOf(endDate));
        verify(mockPreparedStatement, never()).setString(eq(3), anyString());
    }

    @Test
    @DisplayName("Test getSalesByPeriod with weekly period and category filter")
    void getSalesByPeriod_Weekly_WithCategoryFilter() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 7);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("period_label")).
                thenReturn("2023-01-01 - 2023-01-07");

        when(mockResultSet.getString("category_name")).thenReturn("Закуски");
        when(mockResultSet.getString("item_name")).thenReturn("Чіпси");
        when(mockResultSet.getString("employee_name")).thenReturn("Невідомий офіціант");
        when(mockResultSet.getDouble("total_sales")).thenReturn(50.0);

        List<ReportingService.SaleReportEntry> result = reportingService.getSalesByPeriod(
                startDate, endDate, "ТИЖДЕНЬ", "Закуски",
                null, null
        );

        assertEquals(1, result.size());
        assertEquals("Закуски", result.get(0).getCategoryName());

        verify(mockPreparedStatement).setDate(1, Date.valueOf(startDate));
        verify(mockPreparedStatement).setDate(2, Date.valueOf(endDate));
        verify(mockPreparedStatement).setString(3, "%Закуски%");
        verify(mockPreparedStatement, never()).setString(eq(4), anyString());
    }

    @Test
    @DisplayName("Test getSalesByPeriod with monthly period and all filters active")
    void getSalesByPeriod_Monthly_AllFiltersActive() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 2, 1);
        LocalDate endDate = LocalDate.of(2023, 2, 28);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("period_label")).thenReturn("2023-02");
        when(mockResultSet.getString("category_name")).thenReturn("Основні страви");
        when(mockResultSet.getString("item_name")).thenReturn("Стейк");
        when(mockResultSet.getString("employee_name")).thenReturn("Марія Сидоренко");
        when(mockResultSet.getDouble("total_sales")).thenReturn(1200.0);

        List<ReportingService.SaleReportEntry> result = reportingService.getSalesByPeriod(
                startDate, endDate, "МІСЯЦЬ", "Основні",
                "Стейк", "Марія"
        );

        assertEquals(1, result.size());
        assertEquals("Стейк", result.get(0).getMenuItemName());
        assertEquals("Марія Сидоренко", result.get(0).getEmployeeName());


        verify(mockPreparedStatement).setString(3, "%Основні%");
        verify(mockPreparedStatement).setString(4, "%Стейк%");
        verify(mockPreparedStatement).setString(5, "%Марія%");
    }

    @Test
    @DisplayName("Test getSalesByPeriod with empty filter strings should not apply them")
    void getSalesByPeriod_EmptyFilterStrings_ShouldNotApply() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 1);

        when(mockResultSet.next()).thenReturn(false);

        reportingService.getSalesByPeriod(
                startDate, endDate, "ДЕНЬ", "", "", ""
        );

        verify(mockPreparedStatement, never()).setString(eq(3), anyString());
        verify(mockPreparedStatement, never()).setString(eq(4), anyString());
        verify(mockPreparedStatement, never()).setString(eq(5), anyString());
    }

    @Test
    @DisplayName("Test getPopularMenuItems returns list of popular items")
    void getPopularMenuItems_ReturnsPopularItems() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 3, 1);
        LocalDate endDate = LocalDate.of(2023, 3, 31);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("item_name")).thenReturn("Піца", "Салат");
        when(mockResultSet.getInt("quantity_sold")).thenReturn(50, 30);

        List<ReportingService.PopularMenuItemReportEntry> result = reportingService.
                getPopularMenuItems(startDate, endDate);

        assertEquals(2, result.size());
        assertEquals("Піца", result.get(0).getMenuItemName());
        assertEquals(50, result.get(0).getQuantitySold());
        assertEquals("Салат", result.get(1).getMenuItemName());
        assertEquals(30, result.get(1).getQuantitySold());

        verify(mockPreparedStatement).setDate(1, Date.valueOf(startDate));
        verify(mockPreparedStatement).setDate(2, Date.valueOf(endDate));
    }

    @Test
    @DisplayName("Test getPopularMenuItems returns empty list when no data")
    void getPopularMenuItems_NoData_ReturnsEmptyList() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 4, 1);
        LocalDate endDate = LocalDate.of(2023, 4, 1);
        when(mockResultSet.next()).thenReturn(false);

        List<ReportingService.PopularMenuItemReportEntry> result = reportingService.
                getPopularMenuItems(startDate, endDate);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test getFinancialSummary calculates correctly for one month")
    void getFinancialSummary_OneMonthPeriod_CalculatesCorrectly() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 5, 1);
        LocalDate endDate = LocalDate.of(2023, 5, 31);

        PreparedStatement mockSalesStmt = mock(PreparedStatement.class);
        ResultSet mockSalesRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SUM(o.total_amount)"))).thenReturn(mockSalesStmt);
        when(mockSalesStmt.executeQuery()).thenReturn(mockSalesRs);
        when(mockSalesRs.next()).thenReturn(true);
        when(mockSalesRs.getDouble("total_sales")).thenReturn(10000.0);

        PreparedStatement mockGrossSalesStmt = mock(PreparedStatement.class);
        ResultSet mockGrossSalesRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SUM(oi.quantity * mi.price)"))).
                thenReturn(mockGrossSalesStmt);

        when(mockGrossSalesStmt.executeQuery()).thenReturn(mockGrossSalesRs);
        when(mockGrossSalesRs.next()).thenReturn(true);
        when(mockGrossSalesRs.getDouble("gross_sales_amount")).thenReturn(9000.0);

        ResultSet mockSalariesRs = mock(ResultSet.class);
        when(mockStatement.executeQuery(contains("SUM(s.monthly_salary"))).thenReturn(mockSalariesRs);
        when(mockSalariesRs.next()).thenReturn(true);
        when(mockSalariesRs.getDouble("total_salaries")).thenReturn(2000.0);

        ReportingService.FinancialSummary summary = reportingService.getFinancialSummary(startDate, endDate);

        assertEquals(10000.0, summary.getTotalSales());
        assertEquals(9000.0 * 0.30, summary.getProcurementCosts());
        assertEquals(2000.0 * 1, summary.getMaintenanceCosts());
        assertEquals(2700.0 + 2000.0, summary.getTotalExpenses());
        assertEquals(10000.0 - (2700.0 + 2000.0), summary.getNetProfit());

        verify(mockSalesStmt).setDate(1, Date.valueOf(startDate));
        verify(mockSalesStmt).setDate(2, Date.valueOf(endDate));
        verify(mockGrossSalesStmt).setDate(1, Date.valueOf(startDate));
        verify(mockGrossSalesStmt).setDate(2, Date.valueOf(endDate));
    }

    @Test
    @DisplayName("Test getFinancialSummary calculates correctly for multiple months")
    void getFinancialSummary_MultipleMonths_CalculatesCorrectly() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 1, 15);
        LocalDate endDate = LocalDate.of(2023, 3, 10);

        PreparedStatement mockSalesStmt = mock(PreparedStatement.class);
        ResultSet mockSalesRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SUM(o.total_amount)"))).thenReturn(mockSalesStmt);
        when(mockSalesStmt.executeQuery()).thenReturn(mockSalesRs);
        when(mockSalesRs.next()).thenReturn(true); when(mockSalesRs.
                getDouble("total_sales")).thenReturn(30000.0);

        PreparedStatement mockGrossSalesStmt = mock(PreparedStatement.class);
        ResultSet mockGrossSalesRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SUM(oi.quantity * mi.price)")))
                .thenReturn(mockGrossSalesStmt);

        when(mockGrossSalesStmt.executeQuery()).thenReturn(mockGrossSalesRs);
        when(mockGrossSalesRs.next()).thenReturn(true);
        when(mockGrossSalesRs.getDouble("gross_sales_amount")).thenReturn(28000.0);

        ResultSet mockSalariesRs = mock(ResultSet.class);
        when(mockStatement.executeQuery(contains("SUM(s.monthly_salary"))).thenReturn(mockSalariesRs);
        when(mockSalariesRs.next()).thenReturn(true);
        when(mockSalariesRs.getDouble("total_salaries")).thenReturn(2500.0);

        ReportingService.FinancialSummary summary = reportingService.getFinancialSummary(startDate, endDate);

        long expectedMonths = java.time.temporal.ChronoUnit.MONTHS.between(
                startDate.withDayOfMonth(1),
                endDate.withDayOfMonth(1).plusMonths(1)
        );

        assertEquals(2500.0 * expectedMonths, summary.getMaintenanceCosts());
    }

    @Test
    @DisplayName("Test getFinancialSummary calculates correctly when" +
            " monthsBetween is 0 (short period within one month)")
    void getFinancialSummary_ShortPeriod_MonthsBetweenZero_UsesOneMonthForMaintenance() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 5, 1);
        LocalDate endDate = LocalDate.of(2023, 5, 15);

        PreparedStatement mockSalesStmt = mock(PreparedStatement.class);
        ResultSet mockSalesRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SUM(o.total_amount)"))).thenReturn(mockSalesStmt);
        when(mockSalesStmt.executeQuery()).thenReturn(mockSalesRs);
        when(mockSalesRs.next()).thenReturn(true);
        when(mockSalesRs.getDouble("total_sales")).thenReturn(5000.0);

        PreparedStatement mockGrossSalesStmt = mock(PreparedStatement.class);
        ResultSet mockGrossSalesRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SUM(oi.quantity * mi.price)"))).
                thenReturn(mockGrossSalesStmt);

        when(mockGrossSalesStmt.executeQuery()).thenReturn(mockGrossSalesRs);
        when(mockGrossSalesRs.next()).thenReturn(true);
        when(mockGrossSalesRs.getDouble("gross_sales_amount")).thenReturn(4800.0);

        ResultSet mockSalariesRs = mock(ResultSet.class);
        when(mockStatement.executeQuery(contains("SUM(s.monthly_salary"))).thenReturn(mockSalariesRs);
        when(mockSalariesRs.next()).thenReturn(true);
        when(mockSalariesRs.getDouble("total_salaries")).thenReturn(1800.0);

        ReportingService.FinancialSummary summary = reportingService.
                getFinancialSummary(startDate, endDate);

        assertEquals(1800.0 * 1, summary.getMaintenanceCosts(),
                "Витрати на обслуговування мають бути за 1 місяць, якщо період короткий.");
    }

    @Test
    @DisplayName("Test private calculate methods return 0 if no data")
    void privateCalculateMethods_NoData_ReturnZero() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 6, 1);
        LocalDate endDate = LocalDate.of(2023, 6, 1);

        PreparedStatement mockPStmt = mock(PreparedStatement.class);
        ResultSet mockEmptyRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPStmt);
        when(mockPStmt.executeQuery()).thenReturn(mockEmptyRs);
        when(mockEmptyRs.next()).thenReturn(false);

        Statement mockEmptyStmt = mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockEmptyStmt);
        when(mockEmptyStmt.executeQuery(anyString())).thenReturn(mockEmptyRs);


        ReportingService.FinancialSummary summary = reportingService.getFinancialSummary(startDate, endDate);

        assertEquals(0.0, summary.getTotalSales());
        assertEquals(0.0, summary.getProcurementCosts());
        assertEquals(0.0, summary.getMaintenanceCosts());
        assertEquals(0.0, summary.getTotalExpenses());
        assertEquals(0.0, summary.getNetProfit());
    }

    @Test
    @DisplayName("Test getAllCategories returns list of categories")
    void getAllCategories_ReturnsCategories() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("name")).thenReturn("Напої", "Десерти");

        List<String> categories = reportingService.getAllCategories();
        assertEquals(2, categories.size());
        assertTrue(categories.contains("Напої"));
        assertTrue(categories.contains("Десерти"));
        verify(mockStatement).executeQuery("SELECT name FROM categories ORDER BY name");
    }

    @Test
    @DisplayName("Test getAllMenuItems returns list of menu items")
    void getAllMenuItems_ReturnsMenuItems() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("name")).thenReturn("Піца Маргарита");

        List<String> menuItems = reportingService.getAllMenuItems();
        assertEquals(1, menuItems.size());
        assertEquals("Піца Маргарита", menuItems.get(0));
        verify(mockStatement).executeQuery("SELECT name FROM menu_items ORDER BY name");
    }

    @Test
    @DisplayName("Test getAllEmployees (waiters) returns list of waiters")
    void getAllEmployees_ReturnsWaiters() throws SQLException {
        when(mockConnection.prepareStatement(contains("WHERE p.position_name = 'Офіціант'"))).
                thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("first_name")).thenReturn("Іван", "Марія");
        when(mockResultSet.getString("last_name")).thenReturn("Петренко", "Іваненко");


        List<String> employees = reportingService.getAllEmployees();
        assertEquals(2, employees.size());
        assertTrue(employees.contains("Іван Петренко"));
        assertTrue(employees.contains("Марія Іваненко"));
    }

    @Test
    @DisplayName("Test getSalesByPeriod throws SQLException when DB error")
    void getSalesByPeriod_DatabaseError_ThrowsSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Тестова помилка БД"));
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 1);
        assertThrows(SQLException.class, () -> {
            reportingService.getSalesByPeriod(startDate, endDate, "ДЕНЬ",
                    null, null, null);
        });
    }
}