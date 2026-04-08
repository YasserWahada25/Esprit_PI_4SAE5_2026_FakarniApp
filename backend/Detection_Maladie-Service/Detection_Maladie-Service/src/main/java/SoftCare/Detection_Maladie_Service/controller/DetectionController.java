package SoftCare.Detection_Maladie_Service.controller;

import SoftCare.Detection_Maladie_Service.dto.AnalyseIRMResponse;
import SoftCare.Detection_Maladie_Service.service.DetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;   // ← fixes "Cannot resolve symbol 'Files'"
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j                         // ← fixes "Cannot resolve symbol 'log'"
@RestController
@RequestMapping("/api/detection")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetectionController {

    private final DetectionService service;

    @Value("${app.upload-dir:uploads/mri}")
    private String uploadDir;

    /** POST /api/detection/analyser */
    @PostMapping("/analyser")
    public ResponseEntity<AnalyseIRMResponse> analyser(
            @RequestParam("image") MultipartFile image) {
        try {
            return ResponseEntity.ok(service.analyserIRM(image));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /** GET /api/detection/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<AnalyseIRMResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAnalyseById(id));
    }

    /** GET /api/detection/image/{filename} */
    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();

            // If exact file not found, search for timestamped version
            if (!filePath.toFile().exists()) {
                filePath = Files.list(uploadPath)
                        .filter(p -> p.getFileName().toString().endsWith("_" + filename)
                                || p.getFileName().toString().equals(filename))
                        .findFirst()
                        .orElse(filePath);
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("❌ Image not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            String contentType = filename.toLowerCase().endsWith(".png")
                    ? "image/png" : "image/jpeg";

            log.info("✅ Serving image: {}", filePath.getFileName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Error serving image {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}