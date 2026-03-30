package com.lara.scd.predict.application;

import com.lara.scd.patient.application.dto.PatientImageResponseDto;
import com.lara.scd.patient.domain.model.AiProcessingStatus;
import com.lara.scd.patient.domain.model.Localizacao;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.patient.domain.repository.PatientImageRepository;
import com.lara.scd.patient.domain.repository.PatientRepository;
import com.lara.scd.predict.application.dto.AiPredictionResponse;
import com.lara.scd.predict.domain.model.AiPrediction;
import com.lara.scd.predict.domain.repository.AiPredictionRepository;
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

    private static final String AI_MODEL_VERSION = "YOLOv8_simulated_v1.0";

    private final PredictService predictService;
    private final FileStorageService fileStorageService;
    private final PatientRepository patientRepository;
    private final PatientImageRepository patientImageRepository;
    private final AiPredictionRepository aiPredictionRepository;

    public PredictController(PredictService predictService, FileStorageService fileStorageService,
                             PatientRepository patientRepository, PatientImageRepository patientImageRepository,
                             AiPredictionRepository aiPredictionRepository) {
        this.predictService = predictService;
        this.fileStorageService = fileStorageService;
        this.patientRepository = patientRepository;
        this.patientImageRepository = patientImageRepository;
        this.aiPredictionRepository = aiPredictionRepository;
    }

    @PostMapping(value = "/classify/{patientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PatientImageResponseDto> classifyImage(
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

        // Save PatientImage entity
        PatientImage imageEntity = new PatientImage();
        imageEntity.setPatient(patient);
        imageEntity.setFileName(file.getOriginalFilename());
        imageEntity.setFilePath(storedFileName);
        imageEntity.setFileSize(file.getSize());
        imageEntity.setContentType(file.getContentType());
        imageEntity.setLocalizacao(Localizacao.valueOf(localizacao));
        imageEntity.setConfirmed(false);
        imageEntity.setStatusProcessamentoIa(AiProcessingStatus.PENDENTE);

        imageEntity = patientImageRepository.save(imageEntity);

        // Create AiPrediction record from AI response
        if (iaResponse != null && iaResponse.getPredictions() != null && !iaResponse.getPredictions().isEmpty()) {
            AiPredictionResponse.Prediction pred = iaResponse.getPredictions().get(0);

            String modelVersion = iaResponse.getModelVersion() != null
                    ? iaResponse.getModelVersion()
                    : AI_MODEL_VERSION;

            AiPrediction aiPrediction = new AiPrediction();
            aiPrediction.setPatientImage(imageEntity);
            aiPrediction.setVersaoModelo(modelVersion);
            aiPrediction.setClasseInferida(pred.getClassValue());
            aiPrediction.setConfianca(pred.getProbabilidade());
            aiPrediction.setMultClasse(pred.getMultClassValue());
            aiPrediction.setConfiancaMultClasse(pred.getMultClassConfidenceValue());
            aiPredictionRepository.save(aiPrediction);

            imageEntity.setStatusProcessamentoIa(AiProcessingStatus.CONCLUIDO);
        } else {
            imageEntity.setStatusProcessamentoIa(AiProcessingStatus.FALHA);
        }

        imageEntity = patientImageRepository.save(imageEntity);

        return ResponseEntity.ok(PatientImageResponseDto.from(imageEntity));
    }
}
