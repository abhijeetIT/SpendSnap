package com.spendsnap.Repositorys;

import com.spendsnap.Entities.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal,Integer> {

}
