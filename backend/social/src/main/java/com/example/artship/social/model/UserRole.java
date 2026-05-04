package com.example.artship.social.model;

public enum UserRole {
    USER("USER", "Обычный пользователь"),
    MODERATOR("MODERATOR", "Модератор"),
    ADMIN("ADMIN", "Администратор");

    private String code;
    private String description;

    UserRole(String code, String description){
        this.code = code;
        this.description = description;
    }

    public String getCode(){
        return code;
    }
    public String getDescription(){
        return description;
    }

     public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}
