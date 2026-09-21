package com.spendsnap.service;

public interface EmailService {
       Boolean generateAndSendOTP(String userEmail,String purpose);
       Boolean verifyOTP(String userEmail , String Otp);
}
