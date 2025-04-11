package com.xalts.expensetracker.service;

import com.xalts.expensetracker.entity.*;
import com.xalts.expensetracker.repository.*;
import com.xalts.expensetracker.dto.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public Expense createExpense(Expense expense, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        expense.setUser(user);
        if (expense.getDate() == null) expense.setDate(LocalDate.now());
        return expenseRepository.save(expense);
    }
    public Expense createExpense(String userEmail,Expense expense) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        expense.setUser(user);
        if (expense.getDate() == null) expense.setDate(LocalDate.now());
        return expenseRepository.save(expense);
    }

    public List<Expense> getUserExpenses(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        return expenseRepository.findByUser(user);
    }

    public Expense updateExpense(Long id, Expense newExpense, String userEmail) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        expense.setAmount(newExpense.getAmount());
        expense.setCategory(newExpense.getCategory());
        expense.setDescription(newExpense.getDescription());
        expense.setDate(newExpense.getDate() != null ? newExpense.getDate() : expense.getDate());

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id, String userEmail) {
        Expense expense = expenseRepository.findById(id).orElseThrow();
        if (!expense.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        expenseRepository.delete(expense);
    }

    public double getTotalInRange(String userEmail, LocalDate start, LocalDate end) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        return expenseRepository.findByUserAndDateBetween(user, start, end)
                .stream().mapToDouble(Expense::getAmount).sum();
    }

    public Map<String, Double> getTotalsByCategory(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        List<Object[]> result = expenseRepository.getCategoryTotals(user);

        Map<String, Double> categoryMap = new HashMap<>();
        for (Object[] obj : result) {
            categoryMap.put((String) obj[0], (Double) obj[1]);
        }

        return categoryMap;
    }

    public Map<String, Object> getMonthlyReport(String userEmail, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        double total = getTotalInRange(userEmail, start, end);
        Map<String, Double> byCategory = getTotalsByCategory(userEmail);
        return Map.of("total", total, "categoryBreakdown", byCategory);
    }

    public double getTotalExpensesByDateRange(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);

        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public Map<String, Double> getExpensesByCategory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        List<Expense> expenses = expenseRepository.findByUser(user);

        return expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    public Map<String, Object> generateMonthlyReport(String userEmail, int month, int year) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);

        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();

        Map<String, Double> categoryWise = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        Map<String, Object> report = new HashMap<>();
        report.put("total", total);
        report.put("categoryBreakdown", categoryWise);
        report.put("month", month);
        report.put("year", year);

        return report;
    }

//    public MonthlyReport generateMonthlyReport(String userEmail, int month, int year) {
//        User user = userRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));
//
//        LocalDate startDate = LocalDate.of(year, month, 1);
//        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
//
//        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, startDate, endDate);
//
//        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
//
//        Map<String, Double> categoryWise = expenses.stream()
//                .collect(Collectors.groupingBy(
//                        Expense::getCategory,
//                        Collectors.summingDouble(Expense::getAmount)
//                ));
//
//        return new MonthlyReport(total, categoryWise, month, year);
//    }
}
