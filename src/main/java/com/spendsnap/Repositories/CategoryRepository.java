package com.spendsnap.Repositories;

import com.spendsnap.Entities.Category;
import com.spendsnap.Entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    //---------------------------for categories and may use in different pannel like admin -------------
    // Find all categories for a specific user with expense count and total amount
    @Query("SELECT c, COUNT(e.id) as expenseCount, COALESCE(SUM(e.amount), 0) as totalAmount " +
            "FROM Category c " +
            "LEFT JOIN Expense e ON c.id = e.category.id AND e.user.id = :userId " +
            "GROUP BY c.id " +
            "ORDER BY totalAmount DESC")
    List<Object[]> findCategoriesWithExpenseData(@Param("userId") Long userId);

    // Find all expenses for a specific category and user
    @Query("SELECT e FROM Expense e WHERE e.category.id = :categoryId AND e.user.id = :userId ORDER BY e.date DESC")
    List<Expense> findExpensesByCategoryAndUser(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

    // Check if category exists for a specific user (through expenses)
    @Query("SELECT COUNT(e) > 0 FROM Expense e WHERE e.category.id = :categoryId AND e.user.id = :userId")
    boolean existsByCategoryIdAndUserId(@Param("categoryId") Long categoryId, @Param("userId") Long userId);
}
