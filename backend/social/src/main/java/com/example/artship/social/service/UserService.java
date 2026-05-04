package com.example.artship.social.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.social.dto.RoleStatistics;
import com.example.artship.social.model.User;
import com.example.artship.social.model.UserRole;
import com.example.artship.social.repository.UserRepository;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserRepository userRepository;
    
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }
    
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersByRole(UserRole role, Pageable pageable){
        return userRepository.findByUserRole(role, pageable);
    }

    @Transactional(readOnly = true)
    public User changeUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setUserRole(newRole);
        User updatedUser = userRepository.save(user);
        return updatedUser;
    }
    @Transactional(readOnly = true)
    public User changeUserRoleByUsername(String username, UserRole newRole) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        user.setUserRole(newRole);
        User updatedUser = userRepository.save(user);
        return updatedUser;
    }

    @Transactional
    public int bulkChangeUserRole(List<Long> userIds, UserRole newRole) {        
        int updatedCount = 0;
        for (Long userId : userIds) {
            try {
                changeUserRole(userId, newRole);
                updatedCount++;
            } catch (Exception e) {
                logger.error("Failed to update user ID: {}", userId, e);
            }
        }
        
        logger.info("Successfully updated {} users", updatedCount);
        return updatedCount;
    }

    @Transactional(readOnly = true)
    public RoleStatistics getRoleStatistics() {
        logger.info("Getting role statistics");
        
        RoleStatistics stats = new RoleStatistics();
        stats.setTotalUsers(userRepository.count());
        stats.setAdminCount(userRepository.countByUserRole(UserRole.ADMIN));
        stats.setModeratorCount(userRepository.countByUserRole(UserRole.MODERATOR));
        stats.setUserCount(userRepository.countByUserRole(UserRole.USER));
        
        return stats;
    }

    @Transactional(readOnly = true)
    public Page<User> findPublicUsers(Pageable pageable) {
        logger.info("Finding public users with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByIsPublicTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        logger.info("Getting all users with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> searchByUsername(String search, Pageable pageable) {
        logger.info("Searching users by username: {}", search);
        return userRepository.findByUsernameContainingIgnoreCase(search, pageable);
    }

    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, UserRole role) {
        return userRepository.findById(userId)
                .map(user -> user.getUserRole() == role)
                .orElse(false);
    }

}