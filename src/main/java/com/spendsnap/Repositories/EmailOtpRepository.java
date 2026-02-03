package com.spendsnap.Repositories;

import com.spendsnap.Entities.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp,Long> {
       Optional<EmailOtp> findByEmail(String email);
       @Modifying
       @Transactional
       @Query("DELETE FROM EmailOtp e WHERE e.email = :email")
       void deleteByEmail(@Param("email") String email);

}
