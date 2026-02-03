package com.spendsnap.Controllers;

import org.springframework.stereotype.Controller;

import com.spendsnap.DTO.OTPRequest;
import com.spendsnap.DTO.OTPVerifyRequest;
import com.spendsnap.DTO.ApiResponse;
import com.spendsnap.Entities.Category;
import com.spendsnap.Entities.User;
import com.spendsnap.Services.EmailService;
import com.spendsnap.Services.categoryService;
import com.spendsnap.Services.UserServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class Security {

    @Autowired
    private UserServices userServices;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private categoryService categoryService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/signUp")
    public String signUpPage(Model model){
        model.addAttribute("user",new User());
        return "Authentication/signup";
    }

    @PostMapping("/signUp")
    public String registration(@ModelAttribute("user") User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (userServices.register(user)) {
            return "redirect:/signIn?created";
        } else {
            return "error/500";
        }
    }

    /*
     * Send OTP to user's email
     * POST /send-otp
     * Request: { "email": "user@example.com" }
     * Response: { "success": true, "message": "OTP sent successfully" }
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> sendOTP(@Valid @RequestBody OTPRequest request) {
        if (userServices.isEmailAvailable(request.getEmail())) {
            try {
                emailService.generateAndSendOTP(request.getEmail(),"SIGNUP");

                return ResponseEntity.ok(
                        ApiResponse.builder()
                                .success(true)
                                .message("OTP sent successfully to your email")
                                .build()
                );

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message(e.getMessage())
                                .build()
                        );
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Email already registered")
                            .build()
                    );
        }
    }

    /**
     * Verify OTP entered by user
     * POST /verify-otp
     * Request: { "email": "user@example.com", "otp": "1234" }
     * Response: { "success": true, "message": "Email verified successfully" }
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOTP(@Valid @RequestBody OTPVerifyRequest request) {
        try {
            boolean isValid = emailService.verifyOTP(request.getEmail(), request.getOtp());

            if (isValid) {
                return ResponseEntity.ok(
                        ApiResponse.builder()
                                .success(true)
                                .message("Email verified successfully")
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("Invalid or expired OTP")
                                .build()
                        );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        }

    }

    @GetMapping("/signIn")
    public String loginPage(){
        return "Authentication/signIn";
    }



    @GetMapping("/test")
    public String test(Model model) {
        Category category = categoryService.getCategory(4L)
                .orElse(null); // Convert Optional to Category or null
        model.addAttribute("category", category);
        return "test";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(){
        return "Authentication/forgot-password";
    }

    @PostMapping("/send-reset-otp")
    public ResponseEntity<ApiResponse> resetPasswordOtp(@Valid @RequestBody OTPRequest request){
           if(!userServices.doesUserExistByEmail(request.getEmail())){    //check that email exists in db with user
               return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                       .body(ApiResponse.builder()
                               .success(false)
                               .message("This email is not registered. Please create an account to continue.")
                               .build()
                       );
           }
               try {
                   emailService.generateAndSendOTP(request.getEmail(),"FORGOT_PASSWORD");

                   return ResponseEntity.ok(
                           ApiResponse.builder()
                                   .success(true)
                                   .message("OTP sent successfully to your email")
                                   .build()
                   );

               } catch (Exception e) {
                   return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                           .body(ApiResponse.builder()
                                   .success(false)
                                   .message(e.getMessage())
                                   .build()
                           );
               }

    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse> verifyResetOtp(@Valid @RequestBody OTPVerifyRequest otpVerifyRequest){
        try {
            boolean isValid = emailService.verifyOTP(otpVerifyRequest.getEmail(), otpVerifyRequest.getOtp());

            if (isValid) {
                return ResponseEntity.ok(
                        ApiResponse.builder()
                                .success(true)
                                .message("OTP verified successfully")
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("Invalid or expired OTP")
                                .build()
                        );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                    );
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes
    ){
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/auth/forgot-password";
        }

        try {
            userServices.updatePassword(email,password);
            redirectAttributes.addFlashAttribute("success", "Password reset successful! Please login with your new password.");
            return "redirect:/signIn?passwordChange";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Password reset failed: " + e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }


}
