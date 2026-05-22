package com.example.artship.social.config;

import com.example.artship.social.model.User;
import com.example.artship.social.model.UserRole;
import com.example.artship.social.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.email:admin@artship.com}")
    private String adminEmail;
    
    @Value("${app.admin.username:admin}")
    private String adminUsername;
    
    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;
    
    @Value("${app.moderator.email:moderator@artship.com}")
    private String moderatorEmail;
    
    @Value("${app.moderator.username:moderator}")
    private String moderatorUsername;
    
    @Value("${app.moderator.password:Moderator123!}")
    private String moderatorPassword;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== ПРОВЕРКА ИНИЦИАЛИЗАЦИИ ДАННЫХ ===");
        
        createAdminIfNotExists();
        
        createModeratorIfNotExists();
        
        logger.info("=== ИНИЦИАЛИЗАЦИЯ ДАННЫХ ЗАВЕРШЕНА ===");
    }
    
    private void createAdminIfNotExists() {
        if (!userRepository.existsByUsername(adminUsername)) {
            logger.info("Создание администратора: {}", adminUsername);
            
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setDisplayName("Administrator");
            admin.setUserRole(UserRole.ADMIN);
            admin.setEmailVerified(true);
            admin.setIsPublic(false);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(admin);
            logger.info("Администратор создан: username={}, email={}, password={}", 
                       adminUsername, adminEmail, adminPassword);
        } else {
            logger.info("Администратор уже существует: {}", adminUsername);
        }
    }
    
    private void createModeratorIfNotExists() {
        if (!userRepository.existsByUsername(moderatorUsername)) {
            logger.info("Создание модератора: {}", moderatorUsername);
            
            User moderator = new User();
            moderator.setUsername(moderatorUsername);
            moderator.setEmail(moderatorEmail);
            moderator.setPasswordHash(passwordEncoder.encode(moderatorPassword));
            moderator.setDisplayName("Moderator");
            moderator.setUserRole(UserRole.MODERATOR);
            moderator.setEmailVerified(true);
            moderator.setIsPublic(false);
            moderator.setCreatedAt(LocalDateTime.now());
            moderator.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(moderator);
            logger.info("Модератор создан: username={}, email={}, password={}", 
                       moderatorUsername, moderatorEmail, moderatorPassword);
        } else {
            logger.info("Модератор уже существует: {}", moderatorUsername);
        }
    }
}