package com.lara.scd.consultation.domain.service;

import com.lara.scd.config.security.SecurityContext;
import com.lara.scd.consultation.application.dto.ConfirmDiagnosisRequest;
import com.lara.scd.consultation.application.dto.ConsultationRequest;
import com.lara.scd.consultation.application.dto.ConsultationResponse;
import com.lara.scd.consultation.domain.model.Consultation;
import com.lara.scd.consultation.domain.repository.IConsultationRepository;
import com.lara.scd.doctor.domain.model.Doctor;
import com.lara.scd.doctor.domain.repository.IDoctorRepository;
import com.lara.scd.patient.domain.model.DoctorVerdict;
import com.lara.scd.patient.domain.model.Localizacao;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.patient.domain.repository.IPatientImageRepository;
import com.lara.scd.patient.domain.repository.IPatientRepository;
import com.lara.scd.predict.application.dto.AiPredictionResponse;
import com.lara.scd.predict.domain.service.PredictService;
import com.lara.scd.shared.service.FileStorageService;
import com.lara.scd.user.domain.model.AccessLevel;
import com.lara.scd.user.domain.model.User;
import com.lara.scd.user.domain.repository.IUserRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsultationService {

    private final IConsultationRepository consultationRepository;
    private final IPatientRepository patientRepository;
    private final IPatientImageRepository imageRepository;
    private final IDoctorRepository doctorRepository;
    private final IUserRepository userRepository;
    private final PredictService predictService;
    private final FileStorageService fileStorageService;
    private final SecurityContext securityContext;

    public ConsultationService(IConsultationRepository consultationRepository,
                               IPatientRepository patientRepository,
                               IPatientImageRepository imageRepository,
                               IDoctorRepository doctorRepository,
                               IUserRepository userRepository,
                               PredictService predictService,
                               FileStorageService fileStorageService,
                               SecurityContext securityContext) {
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.imageRepository = imageRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.predictService = predictService;
        this.fileStorageService = fileStorageService;
        this.securityContext = securityContext;
    }

    @Transactional
    public ConsultationResponse createConsultation(ConsultationRequest request) {
        // Get the authenticated doctor
        User currentUser = securityContext.getCurrentUser();
        Doctor doctor = doctorRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não é médico"));

        // Find or create patient by CPF
        Patient patient = patientRepository.findByCpf(request.getCpf())
                .orElse(null);

        if (patient == null) {
            patient = new Patient(request.getNome(), request.getCpf(), request.getDataNascimento(), request.getSexo());
            patient = patientRepository.save(patient);
        } else {
            patient.setNome(request.getNome());
            if (request.getDataNascimento() != null) {
                patient.setDataNascimento(request.getDataNascimento());
            }
            patient = patientRepository.save(patient);
        }

        // Create consultation
        Consultation consultation = new Consultation();
        consultation.setPatient(patient);
        consultation.setDoctor(doctor);
        consultation = consultationRepository.save(consultation);

        // Process each image
        List<MultipartFile> images = request.getImages();
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pelo menos uma imagem é obrigatória");
        }
        if (images.size() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo de 20 imagens por consulta");
        }

        List<String> localizacoesStr = request.getLocalizacoes();
        if (localizacoesStr == null || localizacoesStr.size() != images.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade de localizações deve ser igual à quantidade de imagens");
        }

        // Convert strings to Localizacao enum
        List<Localizacao> localizacoes = new java.util.ArrayList<>();
        for (String loc : localizacoesStr) {
            try {
                localizacoes.add(Localizacao.valueOf(loc));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Localização inválida: " + loc);
            }
        }

        int idade = 0;
        if (patient.getDataNascimento() != null) {
            idade = Period.between(patient.getDataNascimento(), LocalDate.now()).getYears();
        }
        String sexo = patient.getSexo() != null ? patient.getSexo() : "M";

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            Localizacao localizacao = localizacoes.get(i);

            // Call AI service
            AiPredictionResponse iaResponse = null;
            try {
                ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
                iaResponse = predictService.predictImage(resource, idade, sexo, localizacao.name());
            } catch (Exception e) {
                // Log the error so we can debug AI service connectivity
                System.err.println("Falha ao chamar serviço de IA para imagem " + file.getOriginalFilename() + ": " + e.getMessage());
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

            // Save PatientImage
            PatientImage imageEntity = new PatientImage();
            imageEntity.setPatient(patient);
            imageEntity.setConsultation(consultation);
            imageEntity.setFileName(file.getOriginalFilename());
            imageEntity.setFilePath(storedFileName);
            imageEntity.setFileSize(file.getSize());
            imageEntity.setContentType(file.getContentType());
            imageEntity.setLocalizacao(localizacao);
            imageEntity.setAiDiagnosis(aiClass);
            imageEntity.setConfidence(aiConfidence);
            imageEntity.setMultClass(multClass);
            imageEntity.setMultClassConfidence(multClassConfidence);
            imageEntity.setConfirmed(false);

            imageRepository.save(imageEntity);
            consultation.getImages().add(imageEntity);

            // Set consultation-level diagnosis from first image
            if (i == 0) {
                consultation.setAiDiagnosis(aiClass);
                consultation.setConfidence(aiConfidence);
                consultation.setMultClass(multClass);
                consultation.setMultClassConfidence(multClassConfidence);
            }
        }

        consultation = consultationRepository.save(consultation);
        return ConsultationResponse.from(consultation);
    }

    @Transactional
    public ConsultationResponse confirmConsultationDiagnosis(UUID consultationId, ConfirmDiagnosisRequest request) {
        User currentUser = securityContext.getCurrentUser();
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta não encontrada"));

        // Verify ownership or admin
        if (!consultation.getDoctor().getId().equals(currentUser.getId())
                && currentUser.getAccessLevel() != AccessLevel.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para confirmar esta consulta");
        }

        consultation.setFinalDiagnosis(request.getFinalDiagnosis().name());
        consultation.setConfirmed(true);
        consultation = consultationRepository.save(consultation);
        return ConsultationResponse.from(consultation);
    }

    @Transactional
    public ConsultationResponse confirmImageDiagnosis(UUID imageId, ConfirmDiagnosisRequest request) {
        User currentUser = securityContext.getCurrentUser();
        PatientImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada"));

        Consultation consultation = image.getConsultation();
        if (consultation == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem não está vinculada a uma consulta");
        }

        // Verify ownership or admin
        if (!consultation.getDoctor().getId().equals(currentUser.getId())
                && currentUser.getAccessLevel() != AccessLevel.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão");
        }

        image.setDoctorVerdict(request.getFinalDiagnosis());
        image.setConfirmed(true);
        imageRepository.save(image);

        return ConsultationResponse.from(consultationRepository.findById(consultation.getId()).orElseThrow());
    }

    public List<ConsultationResponse> listConsultations(String nome, String cpf) {
        User currentUser = securityContext.getCurrentUser();

        List<Consultation> consultations;
        if (nome != null || cpf != null) {
            consultations = consultationRepository.findByDoctorWithFilters(currentUser.getId(), nome, cpf);
        } else {
            consultations = consultationRepository.findByDoctorIdOrderByCreatedAtDesc(currentUser.getId());
        }

        return consultations.stream()
                .map(ConsultationResponse::from)
                .collect(Collectors.toList());
    }

    public ConsultationResponse getConsultation(UUID id) {
        User currentUser = securityContext.getCurrentUser();
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta não encontrada"));

        // Verify ownership or admin
        if (!consultation.getDoctor().getId().equals(currentUser.getId())
                && currentUser.getAccessLevel() != AccessLevel.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão");
        }

        return ConsultationResponse.from(consultation);
    }
}
