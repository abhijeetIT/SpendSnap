package com.spendsnap.Repositories;

import com.spendsnap.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByEmail(String email); // use this for otp email verification

    Optional<User> findByEmail(String email);

    // Count active users
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();

    // Count inactive users
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = false")
    Long countInactiveUsers();


    //=======for admin pannal dashboard===================
    // Search by name or email with pagination
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = true WHERE u.id = :id")
    void activateUser(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :id")
    void deactivateUser(Long id);

    // 🔐 Update password using email
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    int updatePasswordByEmail(String email, String password);
}
