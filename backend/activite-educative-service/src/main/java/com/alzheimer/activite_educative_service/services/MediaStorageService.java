package com.alzheimer.activite_educative_service.services;

import com.alzheimer.activite_educative_service.exceptions.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Stockage local des images (miniatures d’activité, images de questions).
 * Les URLs publiques sont du type {@code /uploads/activities/<fichier>}.
 */
@Service
public class MediaStorageService {

    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Value("${app.media.upload-dir:uploads}")
    private String uploadDir;

    public String storeActivityThumbnail(MultipartFile file) {
        return store(file, "activities");
    }

    public String storeQuestionImage(MultipartFile file) {
        return store(file, "questions");
    }

    private String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Fichier image vide");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessRuleException("Image trop volumineuse (max 5 Mo)");
        }
        String ext = resolveExtension(file);
        if (ext == null) {
            throw new BusinessRuleException("Type de fichier non autorisé (jpg, png, gif, webp)");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = resolveRoot().resolve(subDir);
        try {
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessRuleException("Impossible d’enregistrer l’image : " + e.getMessage());
        }
        return "/uploads/" + subDir + "/" + filename;
    }

    private String resolveExtension(MultipartFile file) {
        String ct = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";
        String fromMime = switch (ct) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> null;
        };
        if (fromMime != null) {
            return fromMime;
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        int dot = original.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        String ext = original.substring(dot).toLowerCase(Locale.ROOT);
        return ALLOWED_EXT.contains(ext) ? ext : null;
    }

    public Path resolveRoot() {
        Path p = Paths.get(uploadDir);
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(p).normalize();
        }
        return p;
    }
}
