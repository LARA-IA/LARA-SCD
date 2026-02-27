package com.lara.scd.shared.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível armazenar o arquivo " + fileName + ".", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Arquivo não encontrado " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Arquivo não encontrado " + fileName, ex);
        }
    }

    public Path getStorageLocation() {
        return this.fileStorageLocation;
    }

    public Resource getBackupZip() {
        try {
            Path zipPath = this.fileStorageLocation.resolve("backup_" + System.currentTimeMillis() + ".zip");
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zipPath))) {
                Files.walk(this.fileStorageLocation).filter(path -> !Files.isDirectory(path) && !path.toString().endsWith(".zip"))
                        .forEach(path -> {
                            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(this.fileStorageLocation.relativize(path).toString());
                            try {
                                zos.putNextEntry(zipEntry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (Exception e) {
                                System.err.println("Erro ao zippar arquivo: " + path);
                            }
                        });
            }
            return new UrlResource(zipPath.toUri());
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar arquivo de backup", e);
        }
    }
}
