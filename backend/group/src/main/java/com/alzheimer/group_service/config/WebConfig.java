package com.alzheimer.group_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Créer le dossier uploads s'il n'existe pas
        File uploadsDir = new File("uploads/groups");
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }
        
        // Servir les images uploadées
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}