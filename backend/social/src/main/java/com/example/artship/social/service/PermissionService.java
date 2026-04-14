package com.example.artship.social.service;

import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;
import com.example.artship.social.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class PermissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    public boolean canManageArt(User currentUser, Art art) {
        if (currentUser == null || art == null) {
            return false;
        }
        
        if (currentUser.getUserRole() == UserRole.ADMIN) {
            return true;
        }
        
        if (currentUser.getUserRole() == UserRole.MODERATOR) {
            return true;
        }
        
        boolean isAuthor = art.getAuthor().getId().equals(currentUser.getId());
        if (isAuthor) {
            logger.debug("Автор {} управляет своим артом {}", currentUser.getUsername(), art.getId());
        } else {
            logger.warn("Пользователь {} не является автором арта {}", currentUser.getUsername(), art.getId());
        }
        return isAuthor;
    }
    
    /**
     * Проверка прав на редактирование контента
     * (админ, модератор или автор)
     */
    public boolean canEditContent(User currentUser, Art art) {
        if (currentUser == null || art == null) return false;
        
        // Админ и модератор могут редактировать любые арты
        if (currentUser.getUserRole() == UserRole.ADMIN || 
            currentUser.getUserRole() == UserRole.MODERATOR) {
            logger.debug("{} {} имеет право редактировать арт {}", 
                currentUser.getUserRole(), currentUser.getUsername(), art.getId());
            return true;
        }
        
        return art.getAuthor().getId().equals(currentUser.getId());
    }
    
    /**
     * Проверка прав на просмотр арта
     */
    public boolean canViewArt(User currentUser, Art art) {
        if (art == null) return false;
        
        if (art.getIsPublicFlag() != null && art.getIsPublicFlag()) {
            logger.debug("Публичный арт {} доступен для просмотра", art.getId());
            return true;
        }
        
        if (currentUser == null) {
            logger.debug("Неавторизованный пользователь не может смотреть приватный арт {}", art.getId());
            return false;
        }
        
        boolean hasAccess = currentUser.getUserRole() == UserRole.ADMIN || 
                           currentUser.getUserRole() == UserRole.MODERATOR ||
                           art.getAuthor().getId().equals(currentUser.getId());
        
        if (hasAccess) {
            logger.debug("Пользователь {} имеет доступ к приватному арту {}", 
                currentUser.getUsername(), art.getId());
        } else {
            logger.warn("Пользователь {} не имеет доступа к приватному арту {}", 
                currentUser.getUsername(), art.getId());
        }
        
        return hasAccess;
    }
    
    
  
    public boolean canManageUsers(User currentUser) {
        if (currentUser == null) return false;
        return currentUser.getUserRole() == UserRole.ADMIN;
    }
    

    public boolean canManageRoles(User currentUser) {
        if (currentUser == null) return false;
        return currentUser.getUserRole() == UserRole.ADMIN;
    }
    

    public boolean canBanUser(User currentUser) {
        if (currentUser == null) return false;
        return currentUser.getUserRole() == UserRole.ADMIN || 
               currentUser.getUserRole() == UserRole.MODERATOR;
    }
    

    public boolean isAdmin(User user) {
        return user != null && user.getUserRole() == UserRole.ADMIN;
    }

    public boolean isModerator(User currentUser) {
        return currentUser != null && currentUser.getUserRole() == UserRole.MODERATOR;
    }
    

    public boolean isAuthor(User user, Art art) {
        return user != null && art != null && 
               art.getAuthor().getId().equals(user.getId());
    }
}