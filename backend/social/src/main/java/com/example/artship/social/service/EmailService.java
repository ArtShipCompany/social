package com.example.artship.social.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${app.email.from:noreply@artship.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;
    
    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;
    
    public void sendVerificationEmail(String to, String username, String token) {
        String verificationUrl = "http://localhost:5173/verify-email?token=" + token;
        
        if (!emailEnabled || mailSender == null) {
            log.info("=== EMAIL WOULD BE SENT (email sending disabled or mailSender is null) ===");
            log.info("To: {}", to);
            log.info("Subject: Подтверждение регистрации - ArtShip");
            log.info("Verification link: {}", verificationUrl);
            log.info("================================================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Подтверждение регистрации - ArtShip");
            message.setText(String.format(
                "Здравствуйте, %s!\n\n" +
                "Спасибо за регистрацию в ArtShip!\n\n" +
                "Для подтверждения вашего email адреса, пожалуйста, перейдите по ссылке:\n%s\n\n" +
                "Ссылка действительна в течение 24 часов.\n\n" +
                "Если вы не регистрировались на нашем сайте, просто проигнорируйте это письмо.\n\n" +
                "С уважением,\nКоманда ArtShip",
                username, verificationUrl
            ));
            
            mailSender.send(message);
            log.info("✅ Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send verification email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }
    
    public void sendPasswordResetEmail(String to, String username, String token) {
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        
        if (!emailEnabled || mailSender == null) {
            log.info("=== EMAIL WOULD BE SENT (email sending disabled or mailSender is null) ===");
            log.info("To: {}", to);
            log.info("Subject: Сброс пароля - ArtShip");
            log.info("Reset link: {}", resetUrl);
            log.info("================================================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Сброс пароля - ArtShip");
            message.setText(String.format(
                "Здравствуйте, %s!\n\n" +
                "Вы запросили сброс пароля для вашей учетной записи ArtShip.\n\n" +
                "Для сброса пароля, пожалуйста, перейдите по ссылке:\n%s\n\n" +
                "Ссылка действительна в течение 1 часа.\n\n" +
                "Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо.\n\n" +
                "С уважением,\nКоманда ArtShip",
                username, resetUrl
            ));
            
            mailSender.send(message);
            log.info("✅ Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }
}