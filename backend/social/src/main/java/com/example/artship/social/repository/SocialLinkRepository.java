package com.example.artship.social.repository;

import com.example.artship.social.model.SocialLink;
import com.example.artship.social.model.SocialPlatform;
import com.example.artship.social.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    
    List<SocialLink> findByUserOrderByDisplayOrderAsc(User user);
    
    List<SocialLink> findByUserAndIsVisibleTrueOrderByDisplayOrderAsc(User user);
    
    Optional<SocialLink> findByUserAndPlatform(User user, SocialPlatform platform);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM SocialLink s WHERE s.user = :user")
    void deleteAllByUser(@Param("user") User user);
    
    boolean existsByUserAndPlatform(User user, SocialPlatform platform);
    
    @Modifying
    @Transactional
    @Query("UPDATE SocialLink s SET s.displayOrder = :order WHERE s.id = :id")
    void updateDisplayOrder(@Param("id") Long id, @Param("order") Integer order);
}