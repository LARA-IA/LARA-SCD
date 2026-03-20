package com.lara.scd.predict.application;

import com.lara.scd.patient.domain.model.Localizacao;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.patient.domain.repository.IPatientImageRepository;
import com.lara.scd.patient.domain.repository.IPatientRepository;
import com.lara.scd.predict.application.dto.AiPredictionResponse;
import com.lara.scd.predict.domain.service.PredictService;
import com.lara.scd.shared.service.FileStorageService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@RestController
@RequestMapping("/api/predict")
public class PredictController {

    private final PredictService predictService;
    private final FileStorageService fileStorageService;
    private final IPatientRepository patientRepository;
    private final IPatientImageRepository patientImageRepository;

    public PredictController(PredictService predictService, FileStorageService fileStorageService,
                             IPatientRepository patientRepository, IPatientImageRepository patientImageRepository) {
        this.predictService = predictService;
        this.fileStorageService = fileStorageService;
        this.patientRepository = patientRepository;
        this.patientImageRepository = patientImageRepository;
    }

    @PostMapping(value = "/classify/{patientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PatientImage> classifyImage(
            @PathVariable UUID patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("localizacao") String localizacao) {
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado"));

        int idade = 0;
        if (patient.getDataNascimento() != null) {
            idade = Period.between(patient.getDataNascimento(), LocalDate.now()).getYears();
        }

        String sexo = patient.getSexo() != null ? patient.getSexo() : "M";

        // Call AI Service synchronously
        AiPredictionResponse iaResponse;
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            iaResponse = predictService.predictImage(resource, idade, sexo, localizacao);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao classificar a imagem na IA", e);
        }

        // Save file to disk
        String storedFileName = fileStorageService.storeFile(file);

        // Map AI response
        String aiClass = null;
        Double aiConfidence = null;
        String multClass = null;
        Double multClassConfidence = null;

        if (iaResponse != null && iaResponse.getPredictions() != null && !iaResponse.getPredictions().isEmpty()) {
            AiPredictionResponse.Prediction pred = iaResponse.getPredictions().get(0);
            aiClass = pred.getClassValue();
            aiConfidence = pred.getProbabilidade();
            multClass = pred.getMultClassValue();
            multClassConfidence = pred.getMultClassConfidenceValue();
        }

        // Save PatientImage entity
        PatientImage imageEntity = new PatientImage();
        imageEntity.setPatient(patient);
        imageEntity.setFileName(file.getOriginalFilename());
        imageEntity.setFilePath(storedFileName);
        imageEntity.setFileSize(file.getSize());
        imageEntity.setContentType(file.getContentType());
        imageEntity.setLocalizacao(Localizacao.valueOf(localizacao));
        imageEntity.setAiDiagnosis(aiClass);
        imageEntity.setConfidence(aiConfidence);
        imageEntity.setMultClass(multClass);
        imageEntity.setMultClassConfidence(multClassConfidence);
        imageEntity.setConfirmed(false);

        patientImageRepository.save(imageEntity);

        return ResponseEntity.ok(imageEntity);
    }
}
