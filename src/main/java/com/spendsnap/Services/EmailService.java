package com.spendsnap.Services;

public interface EmailService {
       Boolean generateAndSendOTP(String userEmail,String purpose);
       Boolean verifyOTP(String userEmail , String Otp);
}
