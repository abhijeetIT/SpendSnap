package com.spendsnap.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OTPVerifyRequest {
    String email;
    String otp;

}
