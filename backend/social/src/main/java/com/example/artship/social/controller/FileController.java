package com.example.artship.social.controller;

import com.example.artship.social.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    private final FileStorageService fileStorageService;
    private final String UPLOAD_DIR = "uploads/images/";
    
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    // Загрузка файла и получение ссылки
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Валидация формата
            if (!isValidImageFormat(file.getContentType())) {
                return ResponseEntity.badRequest().body("Invalid image format");
            }
            
            String fileUrl = fileStorageService.uploadFile(file);
            return ResponseEntity.ok(fileUrl);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed");
        }
    }
    
    // Получение файла по имени
    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            byte[] imageBytes = Files.readAllBytes(filePath);
            
            String contentType = determineContentType(fileName);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
                    
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private boolean isValidImageFormat(String contentType) {
        if (contentType == null) return false;
        
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }
    
    private String determineContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".png")) return "image/png";
        if (fileName.toLowerCase().endsWith(".gif")) return "image/gif";
        if (fileName.toLowerCase().endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}