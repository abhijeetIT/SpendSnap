package com.spendsnap.Repositories;

import com.spendsnap.Entities.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    List<Expense> findByUserId(Long userId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId")
    Long countTransactionsByUserId(@Param("userId") Long userId);

    // All expenses for a user
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId")
    List<Expense> findAllByUser(@Param("userId") Long userId);

    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
            "AND (:startDate IS NULL OR e.date >= :startDate) " +
            "AND (:endDate IS NULL OR e.date <= :endDate)")
    Page<Expense> findByFilters(@Param("userId") Long userId,
                                @Param("categoryId") Long categoryId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                Pageable pageable);


    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "ORDER BY e.date DESC, e.id DESC " +  // Sort by date newest first, then by ID as tiebreaker
            "LIMIT 5")
    List<Expense> findTop5RecentTransactions(@Param("userId") Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND MONTH(e.date) = MONTH(CURRENT_DATE)")
    Double sumByUserIdAndCurrentMonth(@Param("userId") Long userId);

    Long countByCategoryId(Long categoryId); //for admin categories delete option

    //----------for categories pages,may be help as different pages ---------------
    // Find expenses by category and user
    List<Expense> findByCategoryIdAndUserId(Long categoryId, Long userId);

    // Get total amount spent in a category by user
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.category.id = :categoryId AND e.user.id = :userId")
    Double getTotalAmountByCategoryAndUser(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

    // Count expenses in a category by user
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.category.id = :categoryId AND e.user.id = :userId")
    Long countByCategoryAndUser(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

}

