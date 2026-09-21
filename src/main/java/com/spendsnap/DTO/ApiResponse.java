package com.spendsnap.dto;

import lombok.*;

@Data
@Builder
public class ApiResponse {
       Boolean success;
       String message;

}
