package com.alzheimer.activite_educative_service.config;

import com.alzheimer.activite_educative_service.services.MediaStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sert les fichiers uploadés sous {@code /uploads/**} (voir {@link MediaStorageService}).
 */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final MediaStorageService mediaStorageService;

    public UploadResourceConfig(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = mediaStorageService.resolveRoot().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
