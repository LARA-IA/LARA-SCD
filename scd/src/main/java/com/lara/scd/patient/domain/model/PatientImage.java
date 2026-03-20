package com.lara.scd.patient.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.lara.scd.consultation.domain.model.Consultation;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "patient_images")
public class PatientImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Localizacao localizacao;

    @Column(name = "ai_diagnosis")
    private String aiDiagnosis; // MALIGNO ou BENIGNO

    private Double confidence;

    @Column(name = "mult_class")
    private String multClass; // melanoma, nevo, etc

    @Column(name = "mult_class_confidence")
    private Double multClassConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "doctor_verdict")
    private DoctorVerdict doctorVerdict;

    private Boolean confirmed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    @JsonBackReference
    private Consultation consultation;

    // Construtores, Getters e Setters
    public PatientImage() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Localizacao getLocalizacao() { return localizacao; }
    public void setLocalizacao(Localizacao localizacao) { this.localizacao = localizacao; }
    public String getAiDiagnosis() { return aiDiagnosis; }
    public void setAiDiagnosis(String aiDiagnosis) { this.aiDiagnosis = aiDiagnosis; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getMultClass() { return multClass; }
    public void setMultClass(String multClass) { this.multClass = multClass; }
    public Double getMultClassConfidence() { return multClassConfidence; }
    public void setMultClassConfidence(Double multClassConfidence) { this.multClassConfidence = multClassConfidence; }
    public DoctorVerdict getDoctorVerdict() { return doctorVerdict; }
    public void setDoctorVerdict(DoctorVerdict doctorVerdict) { this.doctorVerdict = doctorVerdict; }
    public Boolean getConfirmed() { return confirmed; }
    public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Consultation getConsultation() { return consultation; }
    public void setConsultation(Consultation consultation) { this.consultation = consultation; }
}
