package com.xalts.expensetracker;

import com.xalts.expensetracker.dto.UserNotFoundException;
import com.xalts.expensetracker.entity.Expense;
import com.xalts.expensetracker.entity.User;
import com.xalts.expensetracker.repository.ExpenseRepository;
import com.xalts.expensetracker.repository.UserRepository;
import com.xalts.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("john@example.com");
    }

    @Test
    void testCreateExpense() {
        Expense expense = new Expense(500.0, "Lunch", "Food", LocalDate.now(), mockUser);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        Expense result = expenseService.createExpense("john@example.com", expense);

        assertNotNull(result);
        assertEquals("Food", result.getCategory());
        assertEquals(500.0, result.getAmount());
    }

    @Test
    void testGetTotalExpensesByDateRange() {
        LocalDate start = LocalDate.of(2025, 4, 1);
        LocalDate end = LocalDate.of(2025, 4, 30);

        List<Expense> mockExpenses = Arrays.asList(
            new Expense(200.0, "Lunch", "Food", start.plusDays(1), mockUser),
            new Expense(300.0, "Dinner", "Food", start.plusDays(2), mockUser)
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(expenseRepository.findByUserAndDateBetween(mockUser, start, end)).thenReturn(mockExpenses);

        double total = expenseService.getTotalExpensesByDateRange("john@example.com", start, end);

        assertEquals(500.0, total);
    }

    @Test
    void testGetExpensesByCategory() {
        List<Expense> mockExpenses = Arrays.asList(
            new Expense(100.0, "Snacks", "Food", LocalDate.now(), mockUser),
            new Expense(200.0, "Bus", "Travel", LocalDate.now(), mockUser),
            new Expense(50.0, "Burger", "Food", LocalDate.now(), mockUser)
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(expenseRepository.findByUser(mockUser)).thenReturn(mockExpenses);

        Map<String, Double> result = expenseService.getExpensesByCategory("john@example.com");

        assertEquals(2, result.size());
        assertEquals(150.0, result.get("Food"));
        assertEquals(200.0, result.get("Travel"));
    }

    @Test
    void testGenerateMonthlyReport() {
        LocalDate date1 = LocalDate.of(2025, 4, 10);
        LocalDate date2 = LocalDate.of(2025, 4, 12);

        List<Expense> mockExpenses = Arrays.asList(
            new Expense(150.0, "Pizza", "Food", date1, mockUser),
            new Expense(250.0, "Train", "Travel", date2, mockUser)
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(expenseRepository.findByUserAndDateBetween(eq(mockUser), any(), any()))
            .thenReturn(mockExpenses);

        Map<String, Object> report = expenseService.generateMonthlyReport("john@example.com", 4, 2025);

        assertEquals(400.0, report.get("total"));
        assertEquals(150.0, Optional.ofNullable(report.get("categoryBreakdown"))
                      .filter(Map.class::isInstance)
                      .map(map -> ((Map<?, ?>) map).get("Food"))
                      .orElse(null));
        assertEquals(250.0,Optional.ofNullable(report.get("categoryBreakdown"))
                      .filter(Map.class::isInstance)
                      .map(map -> ((Map<?, ?>) map).get("Travel"))
                      .orElse(null));
    }

    @Test
    void testCreateExpenseUserNotFound() {
        Expense expense = new Expense(100.0, "Coffee", "Food", LocalDate.now(), null);

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                expenseService.createExpense("notfound@example.com", expense)
        );
        assertEquals("User not found", exception.getMessage());
    }
}
