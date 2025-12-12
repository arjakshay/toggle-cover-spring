package com.togglecover.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    public String uploadFile(MultipartFile file, String directory) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, directory);
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        // Return relative path (in production, return CDN/cloud storage URL)
        return "/uploads/" + directory + "/" + uniqueFilename;
    }
}