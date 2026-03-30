package com.lara.scd.consultation.domain.service;

import com.lara.scd.config.security.SecurityContext;
import com.lara.scd.consultation.application.dto.ConfirmDiagnosisRequest;
import com.lara.scd.consultation.application.dto.ConsultationRequest;
import com.lara.scd.consultation.application.dto.ConsultationResponse;
import com.lara.scd.consultation.domain.model.Consultation;
import com.lara.scd.consultation.domain.repository.IConsultationRepository;
import com.lara.scd.doctor.domain.model.Doctor;
import com.lara.scd.doctor.domain.repository.IDoctorRepository;
import com.lara.scd.lesion.domain.model.Lesion;
import com.lara.scd.lesion.domain.repository.ILesionRepository;
import com.lara.scd.patient.domain.model.AiProcessingStatus;
import com.lara.scd.patient.domain.model.Localizacao;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.patient.domain.repository.IPatientImageRepository;
import com.lara.scd.patient.domain.repository.IPatientRepository;
import com.lara.scd.predict.application.dto.AiPredictionResponse;
import com.lara.scd.predict.domain.model.AiPrediction;
import com.lara.scd.predict.domain.repository.IAiPredictionRepository;
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

    private static final String AI_MODEL_VERSION = "YOLOv8_simulated_v1.0";

    private final IConsultationRepository consultationRepository;
    private final IPatientRepository patientRepository;
    private final IPatientImageRepository imageRepository;
    private final IDoctorRepository doctorRepository;
    private final IUserRepository userRepository;
    private final PredictService predictService;
    private final FileStorageService fileStorageService;
    private final SecurityContext securityContext;
    private final ILesionRepository lesionRepository;
    private final IAiPredictionRepository aiPredictionRepository;

    public ConsultationService(IConsultationRepository consultationRepository,
                               IPatientRepository patientRepository,
                               IPatientImageRepository imageRepository,
                               IDoctorRepository doctorRepository,
                               IUserRepository userRepository,
                               PredictService predictService,
                               FileStorageService fileStorageService,
                               SecurityContext securityContext,
                               ILesionRepository lesionRepository,
                               IAiPredictionRepository aiPredictionRepository) {
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.imageRepository = imageRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.predictService = predictService;
        this.fileStorageService = fileStorageService;
        this.securityContext = securityContext;
        this.lesionRepository = lesionRepository;
        this.aiPredictionRepository = aiPredictionRepository;
    }

    @Transactional
    public ConsultationResponse createConsultation(ConsultationRequest request) {
        // Get the authenticated doctor
        User currentUser = securityContext.getCurrentUser();
        Doctor doctor = doctorRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não é médico"));

        // Find patient by ID — patient must be registered beforehand
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Paciente não encontrado. Cadastre o paciente antes de criar uma consulta."));

        // Link patient to doctor if not already linked
        if (patient.getDoctor() == null) {
            patient.setDoctor(doctor);
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

            // Find or create Lesion for this patient + location
            Lesion lesion = lesionRepository.findByPatientIdAndLocalizacaoAnatomica(patient.getId(), localizacao)
                    .orElse(null);
            if (lesion == null) {
                lesion = new Lesion(patient, localizacao, null);
                lesion = lesionRepository.save(lesion);
            }

            // Save file to disk
            String storedFileName = fileStorageService.storeFile(file);

            // Save PatientImage with PENDENTE status
            PatientImage imageEntity = new PatientImage();
            imageEntity.setPatient(patient);
            imageEntity.setConsultation(consultation);
            imageEntity.setLesion(lesion);
            imageEntity.setFileName(file.getOriginalFilename());
            imageEntity.setFilePath(storedFileName);
            imageEntity.setFileSize(file.getSize());
            imageEntity.setContentType(file.getContentType());
            imageEntity.setLocalizacao(localizacao);
            imageEntity.setConfirmed(false);
            imageEntity.setStatusProcessamentoIa(AiProcessingStatus.PENDENTE);

            imageEntity = imageRepository.save(imageEntity);

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
                System.err.println("Falha ao chamar serviço de IA para imagem " + file.getOriginalFilename() + ": " + e.getMessage());
            }

            // Create AiPrediction record
            if (iaResponse != null && iaResponse.getPredictions() != null && !iaResponse.getPredictions().isEmpty()) {
                AiPredictionResponse.Prediction pred = iaResponse.getPredictions().get(0);

                // Use model_version from AI response if available, otherwise use default
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

            imageRepository.save(imageEntity);
            consultation.getImages().add(imageEntity);
        }

        consultation = consultationRepository.save(consultation);
        return ConsultationResponse.from(consultation);
    }

    @Transactional
    public ConsultationResponse confirmConsultationDiagnosis(UUID consultationId, ConfirmDiagnosisRequest request) {
        User currentUser = securityContext.getCurrentUser();
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta não encontrada"));

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

        if (!consultation.getDoctor().getId().equals(currentUser.getId())
                && currentUser.getAccessLevel() != AccessLevel.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão");
        }

        image.setDoctorVerdict(request.getFinalDiagnosis());
        image.setConfirmed(true);

        // Calculate concordanciaIa
        List<AiPrediction> predictions = aiPredictionRepository.findByPatientImageIdOrderByCriadoEmDesc(imageId);
        if (!predictions.isEmpty()) {
            AiPrediction latestPrediction = predictions.get(0);
            String aiClass = latestPrediction.getClasseInferida();
            String doctorVerdictStr = request.getFinalDiagnosis().name();

            boolean aiMaligno = "MALIGNO".equalsIgnoreCase(aiClass) || "maligno".equalsIgnoreCase(aiClass);
            boolean doctorMaligno = isMalignantVerdict(doctorVerdictStr);
            image.setConcordanciaIa(aiMaligno == doctorMaligno);
        }

        imageRepository.save(image);
        return ConsultationResponse.from(consultationRepository.findById(consultation.getId()).orElseThrow());
    }

    private boolean isMalignantVerdict(String verdict) {
        return verdict != null && (
                verdict.equals("MELANOMA") ||
                verdict.equals("CARCINOMA_BASOCELULAR") ||
                verdict.equals("CARCINOMA_ESPINOCELULAR") ||
                verdict.equals("QUERATOSE_ACTINICA")
        );
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

        if (!consultation.getDoctor().getId().equals(currentUser.getId())
                && currentUser.getAccessLevel() != AccessLevel.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão");
        }

        return ConsultationResponse.from(consultation);
    }
}
