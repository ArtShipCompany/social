package com.example.artship.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание тега")
public class TagCreateRequest {
    
    @Schema(
        description = "Название тега",
        example = "живопись",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 50
    )
    @NotBlank(message = "Название тега не может быть пустым")
    @Size(min = 2, max = 50, message = "Название тега должно быть от 2 до 50 символов")
    private String name;
    

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}