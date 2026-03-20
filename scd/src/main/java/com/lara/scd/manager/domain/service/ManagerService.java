package com.lara.scd.manager.domain.service;

import com.lara.scd.config.security.SecurityContext;
import com.lara.scd.doctor.domain.repository.IDoctorRepository;
import com.lara.scd.manager.application.dto.ChangePasswordRequest;
import com.lara.scd.manager.application.dto.ManagerRegisterRequestDto;
import com.lara.scd.exception.UnicidadeVioladaException;
import com.lara.scd.manager.domain.model.Manager;
import com.lara.scd.manager.domain.repository.IManagerRepository;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.patient.domain.repository.IPatientImageRepository;
import com.lara.scd.patient.domain.repository.IPatientRepository;
import com.lara.scd.shared.service.FileStorageService;
import com.lara.scd.user.domain.model.User;
import com.lara.scd.user.domain.repository.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ManagerService {

    private final IManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final IPatientRepository patientRepository;
    private final IDoctorRepository doctorRepository;
    private final IPatientImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final SecurityContext securityContext;
    private final IUserRepository userRepository;


    public ManagerService(IManagerRepository managerRepository, PasswordEncoder passwordEncoder,
                          IPatientRepository patientRepository,
                          IDoctorRepository doctorRepository,
                          IPatientImageRepository imageRepository,
                          FileStorageService fileStorageService,
                          SecurityContext securityContext,
                          IUserRepository userRepository) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.imageRepository = imageRepository;
        this.fileStorageService = fileStorageService;
        this.securityContext = securityContext;
        this.userRepository = userRepository;
    }

    @Transactional
    public void registerManager(ManagerRegisterRequestDto dto) {
        if (managerRepository.existsByEmail(dto.email())) {
            throw new UnicidadeVioladaException("E-mail '" + dto.email() + "' já está cadastrado no sistema.");
        }

        Manager newManager = new Manager(
                dto.nome(),
                dto.cpf(),
                dto.email(),
                passwordEncoder.encode(dto.password())
        );
        managerRepository.save(newManager);
    }

    public com.lara.scd.manager.application.dto.DashboardResponseDto getDashboardStats() {
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalImages = imageRepository.count();

        return new com.lara.scd.manager.application.dto.DashboardResponseDto(totalPatients, totalDoctors, totalImages);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = securityContext.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    public byte[] generateBackup() throws IOException {
        // Buscar apenas imagens confirmadas pelo médico
        List<PatientImage> confirmedImages = imageRepository.findByConfirmedTrue();

        if (confirmedImages.isEmpty()) {
            throw new RuntimeException("Nenhuma imagem com diagnóstico confirmado pelo médico encontrada");
        }

        File tempZip = File.createTempFile("scd_database_", ".zip");
        File tempCsv = File.createTempFile("scd_database_", ".csv");

        try (FileOutputStream fos = new FileOutputStream(tempZip);
             ZipOutputStream zos = new ZipOutputStream(fos);
             PrintWriter csvWriter = new PrintWriter(new FileOutputStream(tempCsv))) {

            // Cabeçalho do CSV
            csvWriter.println("Image ID,Patient ID,AI Diagnosis,AI Confidence,Doctor Final Diagnosis");

            Path storageLocation = fileStorageService.getStorageLocation();

            for (PatientImage image : confirmedImages) {
                // Resolver o caminho do arquivo
                Path imagePath = storageLocation.resolve(image.getFilePath()).normalize();
                if (!Files.exists(imagePath)) {
                    // Tentar caminho absoluto
                    imagePath = Paths.get(image.getFilePath());
                    if (!Files.exists(imagePath)) {
                        continue; // Pular se o arquivo não existir
                    }
                }

                UUID patientId = image.getPatient() != null ? image.getPatient().getId() : null;
                String aiDiagnosis = image.getAiDiagnosis();
                Double confidence = image.getConfidence();
                String doctorVerdict = image.getDoctorVerdict() != null ? image.getDoctorVerdict().name() : "";

                // Escrever linha no CSV
                csvWriter.printf("%s,%s,%s,%s,%s%n",
                        image.getId(),
                        patientId != null ? patientId : "",
                        aiDiagnosis != null ? aiDiagnosis : "",
                        confidence != null ? String.format("%.4f", confidence) : "",
                        doctorVerdict);

                // Obter extensão do arquivo original
                String originalFileName = image.getFileName();
                String extension = ".jpg";
                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                // Nome: {imageId}_{patientId}_{doctorVerdict}.{ext}
                String safeVerdict = sanitizeFileName(doctorVerdict.isEmpty() ? "Unknown" : doctorVerdict);
                String newFileName = String.format("%s_%s_%s%s",
                        image.getId().toString().substring(0, 8),
                        patientId != null ? patientId.toString().substring(0, 8) : "unknown",
                        safeVerdict,
                        extension);

                // Adicionar imagem ao ZIP dentro de "dataset/"
                addFileToZip(imagePath.toFile(), "dataset/" + newFileName, zos);
            }

            csvWriter.flush();

            // Adicionar CSV ao ZIP
            addFileToZip(tempCsv, "database.csv", zos);

            zos.finish();
        } finally {
            if (tempCsv.exists()) {
                tempCsv.delete();
            }
        }

        byte[] zipBytes = Files.readAllBytes(tempZip.toPath());
        tempZip.delete();

        return zipBytes;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void addFileToZip(File file, String zipEntryName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            zos.closeEntry();
        }
    }
}

