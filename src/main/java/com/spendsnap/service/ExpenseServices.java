package com.spendsnap.service;

import com.spendsnap.entity.Category;
import com.spendsnap.entity.Expense;
import com.spendsnap.entity.User;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseServices {
    boolean addExpense(Expense expense);

    Expense getExpense(Long id);

    List<Expense> getExpenseTransaction(Long userId);

    Long totalTransaction(Long id);

    List<Expense> recentExpense5(Long userId);

    Double currentMonthExpense(Long userId);

    Page<Expense> getFilteredExpenses(
            Long userId,
            Long categoryId,
            String dateFilter,
            int currentPage,
            int size
    );

    Boolean deleteExpense(Long id);

    Boolean updateExpense(Expense expense);

    Long totalExpenseInCategoryId(Long id);

}
