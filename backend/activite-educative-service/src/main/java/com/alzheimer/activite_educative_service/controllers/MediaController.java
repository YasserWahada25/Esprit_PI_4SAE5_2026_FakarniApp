package com.alzheimer.activite_educative_service.controllers;

import com.alzheimer.activite_educative_service.services.MediaStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/uploads")
public class MediaController {

    private final MediaStorageService mediaStorageService;

    public MediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @GetMapping("/activities/{filename:.+}")
    public ResponseEntity<Resource> serveActivityImage(@PathVariable String filename) {
        return serveFile("activities", filename);
    }

    @GetMapping("/questions/{filename:.+}")
    public ResponseEntity<Resource> serveQuestionImage(@PathVariable String filename) {
        return serveFile("questions", filename);
    }

    private ResponseEntity<Resource> serveFile(String subDir, String filename) {
        try {
            Path file = mediaStorageService.resolveRoot().resolve(subDir).resolve(filename);
            
            if (!Files.exists(file) || !Files.isReadable(file)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
