package com.lara.scd.consultation.application.dto;

import com.lara.scd.consultation.domain.model.Consultation;
import com.lara.scd.patient.domain.model.PatientImage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConsultationResponse {

    private UUID id;
    private PatientInfo patient;
    private DoctorInfo doctor;
    private String aiDiagnosis;
    private Double confidence;
    private String multClass;
    private Double multClassConfidence;
    private String finalDiagnosis;
    private Boolean confirmed;
    private LocalDateTime createdAt;
    private List<ImageInfo> images;

    public static ConsultationResponse from(Consultation c) {
        ConsultationResponse resp = new ConsultationResponse();
        resp.id = c.getId();
        resp.patient = new PatientInfo(c.getPatient().getId(), c.getPatient().getNome(), c.getPatient().getCpf(), c.getPatient().getSexo());
        resp.doctor = new DoctorInfo(c.getDoctor().getId(), c.getDoctor().getNome());
        resp.aiDiagnosis = c.getAiDiagnosis();
        resp.confidence = c.getConfidence();
        resp.multClass = c.getMultClass();
        resp.multClassConfidence = c.getMultClassConfidence();
        resp.finalDiagnosis = c.getFinalDiagnosis();
        resp.confirmed = c.getConfirmed();
        resp.createdAt = c.getCreatedAt();
        resp.images = c.getImages().stream().map(ImageInfo::from).collect(Collectors.toList());
        return resp;
    }

    // Getters
    public UUID getId() { return id; }
    public PatientInfo getPatient() { return patient; }
    public DoctorInfo getDoctor() { return doctor; }
    public String getAiDiagnosis() { return aiDiagnosis; }
    public Double getConfidence() { return confidence; }
    public String getMultClass() { return multClass; }
    public Double getMultClassConfidence() { return multClassConfidence; }
    public String getFinalDiagnosis() { return finalDiagnosis; }
    public Boolean getConfirmed() { return confirmed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<ImageInfo> getImages() { return images; }

    public record PatientInfo(UUID id, String nome, String cpf, String sexo) {}
    public record DoctorInfo(UUID id, String nome) {}

    public static class ImageInfo {
        private UUID id;
        private String filePath;
        private String fileName;
        private Long fileSize;
        private String contentType;
        private String aiDiagnosis;
        private Double confidence;
        private String multClass;
        private Double multClassConfidence;
        private String finalDiagnosis;
        private Boolean confirmed;

        public static ImageInfo from(PatientImage img) {
            ImageInfo info = new ImageInfo();
            info.id = img.getId();
            info.filePath = img.getFilePath();
            info.fileName = img.getFileName();
            info.fileSize = img.getFileSize();
            info.contentType = img.getContentType();
            info.aiDiagnosis = img.getAiDiagnosis();
            info.confidence = img.getConfidence();
            info.multClass = img.getMultClass();
            info.multClassConfidence = img.getMultClassConfidence();
            info.finalDiagnosis = img.getDoctorVerdict() != null ? img.getDoctorVerdict().name() : null;
            info.confirmed = img.getConfirmed();
            return info;
        }

        // Getters
        public UUID getId() { return id; }
        public String getFilePath() { return filePath; }
        public String getFileName() { return fileName; }
        public Long getFileSize() { return fileSize; }
        public String getContentType() { return contentType; }
        public String getAiDiagnosis() { return aiDiagnosis; }
        public Double getConfidence() { return confidence; }
        public String getMultClass() { return multClass; }
        public Double getMultClassConfidence() { return multClassConfidence; }
        public String getFinalDiagnosis() { return finalDiagnosis; }
        public Boolean getConfirmed() { return confirmed; }
    }
}
