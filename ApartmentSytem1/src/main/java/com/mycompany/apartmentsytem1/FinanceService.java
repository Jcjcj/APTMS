package com.mycompany.apartmentsytem1;

public class FinanceService {

    private ProfitDAO profitDao = new ProfitDAO();
    private ExpenseDAO expenseDao = new ExpenseDAO();
    
    private static final double TAX_RATE = 0.12; // 12% tax

    // Object to hold all the monthly numbers for your UI
    public static class MonthlyReport {
        public double revenue;
        public double buildingExpenses;
        public double roomExpenses;
        public double totalExpenses;
        public double grossProfit;
        public double taxDeduction;
        public double netProfit;
    }

    // Object to hold all the annual numbers for your UI
    public static class AnnualReport {
        public double totalRevenue;
        public double totalExpenses;
        public double grossProfit;
        public double capital;
        public double roiPercentage;
    }

    // --- GENERATE MONTHLY DASHBOARD MATH ---
    public MonthlyReport getMonthlyReport(int apartmentId, String month) {
        MonthlyReport report = new MonthlyReport();
        
        report.revenue = profitDao.getMonthlyRevenue(apartmentId, month);
        report.buildingExpenses = expenseDao.getBuildingExpensesOnly(apartmentId, month);
        report.roomExpenses = expenseDao.getRoomExpensesOnly(apartmentId, month);
        
        report.totalExpenses = report.buildingExpenses + report.roomExpenses;
        report.grossProfit = report.revenue - report.totalExpenses;
        report.taxDeduction = report.grossProfit > 0 ? (report.grossProfit * TAX_RATE) : 0.0;
        report.netProfit = report.grossProfit - report.taxDeduction;

        return report;
    }

  // --- GENERATE ANNUAL DASHBOARD MATH ---
    public AnnualReport getAnnualReport(int apartmentId, String year) {
        AnnualReport report = new AnnualReport();

        report.totalRevenue = profitDao.getAnnualRevenue(apartmentId, year);
        
        // FIXED: Changed "%" + year TO year + "%"
        // This ensures "2026%" matches "2026-01", "2026-05", etc.
        double bldgExp = expenseDao.getBuildingExpensesOnly(apartmentId, year + "%"); 
        double roomExp = expenseDao.getRoomExpensesOnly(apartmentId, year + "%");
        report.totalExpenses = bldgExp + roomExp;

        report.grossProfit = report.totalRevenue - report.totalExpenses;
        report.capital = profitDao.getCapitalTotal(apartmentId);
        
        report.roiPercentage = report.capital > 0 ? (report.grossProfit / report.capital) * 100 : 0.0;

        return report;
    }
}