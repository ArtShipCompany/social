package com.example.artship.social.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.artship.social.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    long countByUsernameContainingIgnoreCase(String username);

    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime dateTime);
}