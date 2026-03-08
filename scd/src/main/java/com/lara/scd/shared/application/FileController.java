package com.lara.scd.shared.application;

import com.lara.scd.shared.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "Endpoints para servir arquivos (imagens)")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Obter arquivo por nome", description = "Serve um arquivo (imagem) buscando pelo nome")
    @GetMapping("/by-name/{filename:.+}")
    public ResponseEntity<Resource> getFileByName(@PathVariable String filename) {
        Resource resource = fileStorageService.loadFileAsResource(filename);

        String contentType = determineContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Obter arquivo por path", description = "Serve um arquivo (imagem) pelo caminho")
    @GetMapping("/**")
    public ResponseEntity<Resource> getFileByPath(@RequestParam(value = "path", required = false) String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Extract just the filename from the given path
        String filename = filePath;
        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf('/') + 1);
        }
        if (filename.contains("\\")) {
            filename = filename.substring(filename.lastIndexOf('\\') + 1);
        }

        Resource resource = fileStorageService.loadFileAsResource(filename);
        String contentType = determineContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private String determineContentType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }
}
