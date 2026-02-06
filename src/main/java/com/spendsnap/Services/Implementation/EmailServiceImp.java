package com.spendsnap.Services.Implementation;

import com.spendsnap.Entities.EmailOtp;
import com.spendsnap.Repositories.EmailOtpRepository;
import com.spendsnap.Services.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EmailServiceImp implements EmailService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔐 OTP generator
    public static String OtpGenerate() {
        SecureRandom secureRandom = new SecureRandom();
        int otp = 1000 + secureRandom.nextInt(9000);
        return String.valueOf(otp);
    }

    // ================= SEND OTP =================
    @Transactional
    @Override
    public Boolean generateAndSendOTP(String userEmail, String purpose) {

        try {
            // 🔁 Remove old OTP
            emailOtpRepository.deleteByEmail(userEmail);

            // 🔐 Generate OTP
            String otp = OtpGenerate();

            // 🎯 Purpose text
            String actionText = switch (purpose.toUpperCase()) {
                case "SIGNUP" -> "complete your Sign Up";
                case "FORGOT_PASSWORD" -> "reset your password";
                case "LOGIN" -> "verify your login";
                default -> "verify your action";
            };

            // 💌 HTML EMAIL (UNCHANGED)
            String html = """
            <html>
            <body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td align="center">
                            <table width="480" style="background:#ffffff;padding:30px;border-radius:10px;">
                                
                                <tr>
                                    <td align="center">
                                        <h2 style="color:#222;">OTP Verification</h2>
                                    </td>
                                </tr>

                                <tr>
                                    <td style="color:#555;font-size:15px;">
                                        Hello 👋<br><br>
                                        Use the OTP below to <b>%s</b>.
                                    </td>
                                </tr>

                                <tr><td height="20"></td></tr>

                                <tr>
                                    <td align="center">
                                        <div style="
                                            font-size:32px;
                                            font-weight:bold;
                                            letter-spacing:6px;
                                            color:#2e6da4;
                                            background:#eef3ff;
                                            padding:15px;
                                            border-radius:8px;">
                                            %s
                                        </div>
                                    </td>
                                </tr>

                                <tr><td height="20"></td></tr>

                                <tr>
                                    <td style="font-size:13px;color:#888;">
                                        ⏳ This OTP is valid for <b>2 minutes</b>.<br>
                                        🔒 Do not share this OTP with anyone.
                                    </td>
                                </tr>

                                <tr><td height="25"></td></tr>

                                <tr>
                                    <td style="font-size:12px;color:#aaa;text-align:center;">
                                        © 2026 SpendSnap<br>
                                        Secure expense tracking made simple
                                    </td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(actionText, otp);

            // 🔐 Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            // 📦 Brevo Request Body (SAFE MAP)
            Map<String, Object> body = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("name", "SpendSnap");
            sender.put("email", "abhijha4324@gmail.com"); // MUST be verified in Brevo

            body.put("sender", sender);

            Map<String, String> toUser = new HashMap<>();
            toUser.put("email", userEmail);

            body.put("to", List.of(toUser));
            body.put("subject", "SpendSnap | OTP Verification");
            body.put("htmlContent", html);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(body, headers);

            // 📤 Send email via Brevo API
            restTemplate.postForEntity(
                    brevoApiUrl,
                    request,
                    String.class
            );

            // 💾 Save OTP (hashed)
            EmailOtp emailOtp = new EmailOtp();
            emailOtp.setEmail(userEmail);
            emailOtp.setOtp(passwordEncoder.encode(otp));
            emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(2));
            emailOtpRepository.save(emailOtp);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // ================= VERIFY OTP =================
    @Transactional
    @Override
    public Boolean verifyOTP(String userEmail, String otp) {

        Optional<EmailOtp> optionalOtp =
                emailOtpRepository.findByEmail(userEmail);

        if (optionalOtp.isEmpty()) {
            return false;
        }

        EmailOtp emailOtp = optionalOtp.get();

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailOtpRepository.delete(emailOtp);
            return false;
        }

        if (!passwordEncoder.matches(otp, emailOtp.getOtp())) {
            return false;
        }

        emailOtpRepository.delete(emailOtp);
        return true;
    }
}
