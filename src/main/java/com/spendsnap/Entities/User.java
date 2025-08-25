package com.spendsnap.Entities;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Valid
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String password;

    private String profilePictureUrl = null;

    private String phoneNumber = null;

    private String gender; // MALE, FEMALE, OTHER

    private LocalDate dateOfBirth = null;

    private String verificationToken = null;

    private Boolean isActive = true;

    private String role = "USER";

    private LocalDateTime lastLogin;

    private Boolean emailVerified = false;

    private String resetPasswordToken; // For forgot password

    private LocalDateTime resetPasswordTokenExpiry;


    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Goal> goals = new ArrayList<>();


    public User() {
    }

    public Long getId() { return id; }

    public void setId(Long id) {this.id = id;}

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public LocalDateTime getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public String getProfilePictureUrl() {return profilePictureUrl;}

    public void setProfilePictureUrl(String profilePictureUrl) {this.profilePictureUrl = profilePictureUrl;}

    public String getPhoneNumber() {return phoneNumber;}

    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}

    public String getGender() { return gender;}

    public void setGender(String gender) {this.gender = gender;}

    public LocalDate getDateOfBirth() {return dateOfBirth;}

    public void setDateOfBirth(LocalDate dateOfBirth) {this.dateOfBirth = dateOfBirth;}


    public String getVerificationToken() {return verificationToken;}

    public void setVerificationToken(String verificationToken) {this.verificationToken = verificationToken;}

    public Boolean getActive() {return isActive;}

    public void setActive(Boolean active) {isActive = active;}

    public List<Expense> getExpenses() {return expenses;}

    public void setExpenses(List<Expense> expenses) {this.expenses = expenses;}

    public List<Budget> getBudgets() {return budgets;}

    public void setBudgets(List<Budget> budgets) {this.budgets = budgets;}

    public List<Goal> getGoals() {return goals;}

    public void setGoals(List<Goal> goals) { this.goals = goals; }

    public LocalDateTime getLastLogin() { return lastLogin; }

    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Boolean getEmailVerified() { return emailVerified;}

    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified;}

    public String getResetPasswordToken() { return resetPasswordToken; }

    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public LocalDateTime getResetPasswordTokenExpiry() { return resetPasswordTokenExpiry; }

    public void setResetPasswordTokenExpiry(LocalDateTime resetPasswordTokenExpiry) { this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;}
}