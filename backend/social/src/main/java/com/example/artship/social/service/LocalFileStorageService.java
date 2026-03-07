package com.example.artship.social.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService {
    
    private final Path uploadDir;
    
    public LocalFileStorageService() {
        // Сохраняем в uploads/images/
        this.uploadDir = Paths.get("uploads/images").toAbsolutePath().normalize();
        
        System.out.println("=== FILE STORAGE SERVICE INIT ===");
        System.out.println("Upload directory: " + this.uploadDir);
        System.out.println("Directory exists: " + Files.exists(this.uploadDir));
        
        try {
            Files.createDirectories(this.uploadDir);
            System.out.println("Directory created/verified");
            
            // Показываем файлы в директории для отладки
            if (Files.exists(this.uploadDir)) {
                System.out.println("Files in directory:");
                Files.list(this.uploadDir)
                     .limit(10)
                     .forEach(path -> System.out.println("  - " + path.getFileName()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для загрузки файлов", e);
        }
    }
    
    public String uploadFile(MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String fileName = UUID.randomUUID() + fileExtension;
            
            Path targetLocation = this.uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("=== FILE UPLOADED ===");
            System.out.println("Original filename: " + originalFileName);
            System.out.println("Saved to: " + targetLocation);
            System.out.println("File size: " + Files.size(targetLocation) + " bytes");
            System.out.println("File exists: " + Files.exists(targetLocation));

            return "/uploads/images/" + fileName;
            
        } catch (IOException ex) {
            throw new RuntimeException("Не удалось сохранить файл: " + ex.getMessage(), ex);
        }
    }
    
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName == null || fileName.isEmpty()) {
                System.err.println("Cannot extract filename from URL: " + fileUrl);
                return;
            }
            
            Path filePath = this.uploadDir.resolve(fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("File deleted: " + filePath);
            } else {
                System.out.println("File not found for deletion: " + filePath);
            }
            
        } catch (IOException e) {
            System.err.println("Не удалось удалить файл: " + e.getMessage());
        }
    }
    
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        
        String fileName = extractFileNameFromUrl(fileUrl);
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        Path filePath = this.uploadDir.resolve(fileName);
        return Files.exists(filePath);
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg"; 
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "";
        }

        
        String[] parts = fileUrl.split("/");
        return parts[parts.length - 1];
    }
}