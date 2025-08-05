package com.spendsnap.Repositorys;

import com.spendsnap.Entities.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget,Integer> {
}
