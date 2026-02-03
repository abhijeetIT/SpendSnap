package com.spendsnap.Services.Implementation;

import com.spendsnap.Entities.EmailOtp;
import com.spendsnap.Repositories.EmailOtpRepository;
import com.spendsnap.Services.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailServiceImp implements EmailService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    @Autowired
    private JavaMailSender javaMailSender;

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
            // 🔁 Remove old OTP if exists
            emailOtpRepository.deleteByEmail(userEmail);

            // 🔐 Generate OTP
            String otp = OtpGenerate();

            // 📨 Prepare mail
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(userEmail);
            helper.setSubject("SpendSnap | OTP Verification");

            // 🎯 Dynamic message based on purpose
            String actionText = switch (purpose.toUpperCase()) {
                case "SIGNUP" -> "complete your Sign Up";
                case "FORGOT_PASSWORD" -> "reset your password";
                case "LOGIN" -> "verify your login";
                default -> "verify your action";
            };

            // 💌 Enhanced HTML mail
            String html = """
            <html>
            <body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td align="center">
                            <table width="480" style="background:#ffffff;padding:30px;border-radius:10px;">
                                
                                <tr>
                                    <td align="center">
                                        <img src="cid:spendsnapLogo" width="80"/>
                                    </td>
                                </tr>

                                <tr><td height="20"></td></tr>

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

            helper.setText(html, true);

            // 🖼 Logo
            ClassPathResource logo =
                    new ClassPathResource("static/images/spendsnap-logo.png");
            helper.addInline("spendsnapLogo", logo);

            // 📤 Send email
            javaMailSender.send(message);

            // 💾 Save OTP (hashed)
            EmailOtp emailOtp = new EmailOtp();
            emailOtp.setEmail(userEmail);
            emailOtp.setOtp(passwordEncoder.encode(otp));
            emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(2));

            emailOtpRepository.save(emailOtp);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // ================= VERIFY OTP =================
    @Transactional
    @Override
    public Boolean verifyOTP(String userEmail, String otp) {

        Optional<EmailOtp> optionalOtp =
                emailOtpRepository.findByEmail(userEmail);

        // ❌ Not found
        if (optionalOtp.isEmpty()) {
            return false;
        }

        EmailOtp emailOtp = optionalOtp.get();

        // ❌ Expired
        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailOtpRepository.findByEmail(userEmail)
                    .ifPresent(emailOtpRepository::delete);
            return false;
        }

        // ❌ Mismatch
        if (!passwordEncoder.matches(otp,emailOtp.getOtp())) { //hashed otp
            return false;
        }

        // ✅ Success → remove OTP
        emailOtpRepository.delete(emailOtp);
        return true;
    }
}
