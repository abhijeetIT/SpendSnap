package com.spendsnap.Services.Implementation;

import com.spendsnap.Entities.Expense;
import com.spendsnap.Repositories.ExpenseRepository;
import com.spendsnap.Services.ExpenseServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseServicesImp implements ExpenseServices {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public boolean addExpense(Expense expense) {
        try{
            expenseRepository.save(expense);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public Expense getExpense(Long id) {
        return expenseRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Override
    public List<Expense> getExpenseTransaction(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    public Long totalTransaction(Long id){
        try {
            Long count = expenseRepository.countTransactionsByUserId(id);
            return count;
        }catch (Exception e){
            return 0L;

        }
    }

    @Override
    public List<Expense> recentExpense5(Long userId) {
        return expenseRepository.findTop5RecentTransactions(userId);
    }

    @Override
    public Double currentMonthExpense(Long userId) {
        return expenseRepository.sumByUserIdAndCurrentMonth(userId);
    }


    @Override
    public Page<Expense> getFilteredExpenses(
            Long userId,
            Long categoryId,
            String dateFilter,
            int currentPage,
            int size
    ) {

        LocalDate startDate = null;
        LocalDate endDate = null;
        // Step 2: Convert dateFilter string into date range
        LocalDate today = LocalDate.now();
        switch (dateFilter) {
            case "today":
                    startDate = today;
                    endDate = today;
                    break;
            case "week":
                    startDate = today.with(DayOfWeek.MONDAY);
                    endDate = today.with(DayOfWeek.SUNDAY);
                    break;
            case "month":
                startDate = today.withDayOfMonth(1);
                endDate = today.withDayOfMonth(today.lengthOfMonth());
                break;
            case "year":
                startDate = today.withDayOfYear(1);
                endDate = today.withDayOfYear(today.lengthOfYear());
                break;
            default:
                // No filter applied → leave dates null
                break;
        }

        Pageable pageable = PageRequest.of(currentPage, size, Sort.by("date").descending());

        return expenseRepository.findByFilters(
                userId,
                categoryId == 0 ? null : categoryId,
                startDate,
                endDate,
                pageable
        );
    }

    @Override
    public Boolean deleteExpense(Long id) {
        try{
            expenseRepository.deleteById(id);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public Boolean updateExpense(Expense expense) {
        try {
            expenseRepository.save(expense);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public Long totalExpenseInCategoryId(Long id) {
        return expenseRepository.countByCategoryId(id);
    }


}
