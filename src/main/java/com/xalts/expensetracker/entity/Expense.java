package com.xalts.expensetracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;

    private String description;

    private String category;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Expense(double amount, String description, String category, LocalDate date, User user) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.user = user;
    }

}
