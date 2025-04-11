package com.xalts.expensetracker.controller;

import com.xalts.expensetracker.entity.Expense;
import com.xalts.expensetracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    private String getEmail(UserDetails user) {
        return user.getUsername();
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense,
                                                 @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.createExpense(expense, getEmail(user)));
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.getUserExpenses(getEmail(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> update(@PathVariable Long id,
                                          @RequestBody Expense updated,
                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.updateExpense(id, updated, getEmail(user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails user) {
        expenseService.deleteExpense(id, getEmail(user));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getTotalInRange(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(expenseService.getTotalInRange(getEmail(user), start, end));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Map<String, Double>> getByCategory(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.getTotalsByCategory(getEmail(user)));
    }

    @GetMapping("/monthly-report")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getMonthlyReport(getEmail(user), year, month));
    }
}
