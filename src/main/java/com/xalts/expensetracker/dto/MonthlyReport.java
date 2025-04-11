package com.xalts.expensetracker.dto;

import java.util.Map;

public class MonthlyReport {
    private double total;
    private Map<String, Double> categoryBreakdown;

    public MonthlyReport(double total, Map<String, Double> categoryBreakdown) {
        this.total = total;
        this.categoryBreakdown = categoryBreakdown;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Map<String, Double> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<String, Double> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }
}
