package com.example.artship.social.security;

import com.example.artship.social.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    
    private final User user;
    private final List<GrantedAuthority> authorities;
    
    public CustomUserDetails(User user) {
        this.user = user;
        

        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
    }
    
    public Long getId() {
        return user.getId();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // Возвращаем список authorities
    }
    
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}