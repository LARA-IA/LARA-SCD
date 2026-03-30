package com.lara.scd.consultation.application.dto;

import com.lara.scd.consultation.domain.model.Consultation;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.predict.domain.model.AiPrediction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConsultationResponse {

    private UUID id;
    private PatientInfo patient;
    private DoctorInfo doctor;
    private String finalDiagnosis;
    private Boolean confirmed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageInfo> images;

    public static ConsultationResponse from(Consultation c) {
        ConsultationResponse resp = new ConsultationResponse();
        resp.id = c.getId();
        resp.patient = new PatientInfo(
                c.getPatient().getId(),
                c.getPatient().getNome(),
                c.getPatient().getCpf(),
                c.getPatient().getSexo(),
                c.getPatient().getDataNascimento(),
                c.getPatient().getTermoConsentimentoIa()
        );
        resp.doctor = new DoctorInfo(c.getDoctor().getId(), c.getDoctor().getNome());
        resp.finalDiagnosis = c.getFinalDiagnosis();
        resp.confirmed = c.getConfirmed();
        resp.createdAt = c.getCreatedAt();
        resp.updatedAt = c.getUpdatedAt();
        resp.images = c.getImages().stream().map(ImageInfo::from).collect(Collectors.toList());
        return resp;
    }

    // Getters
    public UUID getId() { return id; }
    public PatientInfo getPatient() { return patient; }
    public DoctorInfo getDoctor() { return doctor; }
    public String getFinalDiagnosis() { return finalDiagnosis; }
    public Boolean getConfirmed() { return confirmed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ImageInfo> getImages() { return images; }

    public record PatientInfo(UUID id, String nome, String cpf, String sexo, LocalDate dataNascimento, Boolean termoConsentimentoIa) {}
    public record DoctorInfo(UUID id, String nome) {}

    public static class ImageInfo {
        private UUID id;
        private String filePath;
        private String fileName;
        private Long fileSize;
        private String contentType;
        private String localizacao;
        private String finalDiagnosis;
        private Boolean confirmed;
        private Boolean concordanciaIa;
        private String statusProcessamentoIa;
        private UUID lesionId;
        private List<AiPredictionInfo> predictions;

        public static ImageInfo from(PatientImage img) {
            ImageInfo info = new ImageInfo();
            info.id = img.getId();
            info.filePath = img.getFilePath();
            info.fileName = img.getFileName();
            info.fileSize = img.getFileSize();
            info.contentType = img.getContentType();
            info.localizacao = img.getLocalizacao() != null ? img.getLocalizacao().name() : null;
            info.finalDiagnosis = img.getDoctorVerdict() != null ? img.getDoctorVerdict().name() : null;
            info.confirmed = img.getConfirmed();
            info.concordanciaIa = img.getConcordanciaIa();
            info.statusProcessamentoIa = img.getStatusProcessamentoIa() != null ? img.getStatusProcessamentoIa().name() : null;
            info.lesionId = img.getLesion() != null ? img.getLesion().getId() : null;
            info.predictions = img.getPredictions() != null
                    ? img.getPredictions().stream().map(AiPredictionInfo::from).collect(Collectors.toList())
                    : List.of();
            return info;
        }

        // Getters
        public UUID getId() { return id; }
        public String getFilePath() { return filePath; }
        public String getFileName() { return fileName; }
        public Long getFileSize() { return fileSize; }
        public String getContentType() { return contentType; }
        public String getLocalizacao() { return localizacao; }
        public String getFinalDiagnosis() { return finalDiagnosis; }
        public Boolean getConfirmed() { return confirmed; }
        public Boolean getConcordanciaIa() { return concordanciaIa; }
        public String getStatusProcessamentoIa() { return statusProcessamentoIa; }
        public UUID getLesionId() { return lesionId; }
        public List<AiPredictionInfo> getPredictions() { return predictions; }
    }

    public static class AiPredictionInfo {
        private UUID id;
        private String versaoModelo;
        private String classeInferida;
        private Double confianca;
        private String multClasse;
        private Double confiancaMultClasse;
        private LocalDateTime criadoEm;

        public static AiPredictionInfo from(AiPrediction pred) {
            AiPredictionInfo info = new AiPredictionInfo();
            info.id = pred.getId();
            info.versaoModelo = pred.getVersaoModelo();
            info.classeInferida = pred.getClasseInferida();
            info.confianca = pred.getConfianca();
            info.multClasse = pred.getMultClasse();
            info.confiancaMultClasse = pred.getConfiancaMultClasse();
            info.criadoEm = pred.getCriadoEm();
            return info;
        }

        // Getters
        public UUID getId() { return id; }
        public String getVersaoModelo() { return versaoModelo; }
        public String getClasseInferida() { return classeInferida; }
        public Double getConfianca() { return confianca; }
        public String getMultClasse() { return multClasse; }
        public Double getConfiancaMultClasse() { return confiancaMultClasse; }
        public LocalDateTime getCriadoEm() { return criadoEm; }
    }
}
