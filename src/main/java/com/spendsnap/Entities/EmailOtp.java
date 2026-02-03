package com.spendsnap.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_otp")
public class EmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String otp;

    @Column(nullable = true)
    private LocalDateTime expiresAt;

    public EmailOtp() {
    }

    public Long getId() { return id; }

    public String getEmail() { return email; }

    public String getOtp() { return otp; }

    public LocalDateTime getExpiresAt() { return expiresAt; }

    public void setId(Long id) { this.id = id; }

    public void setEmail(String email) { this.email = email; }

    public void setOtp(String otp) { this.otp = otp; }

    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

}
