package com.lara.scd.patient.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lara.scd.consultation.domain.model.Consultation;
import com.lara.scd.lesion.domain.model.Lesion;
import com.lara.scd.predict.domain.model.AiPrediction;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "doctor_verdict")
    private DoctorVerdict doctorVerdict;

    private Boolean confirmed = false;

    @Column(name = "concordancia_ia")
    private Boolean concordanciaIa;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_processamento_ia", nullable = false)
    private AiProcessingStatus statusProcessamentoIa = AiProcessingStatus.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    @JsonBackReference
    private Consultation consultation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesion_id")
    private Lesion lesion;

    @OneToMany(mappedBy = "patientImage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AiPrediction> predictions = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // Constructors
    public PatientImage() {}

    // Getters & Setters
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
    public DoctorVerdict getDoctorVerdict() { return doctorVerdict; }
    public void setDoctorVerdict(DoctorVerdict doctorVerdict) { this.doctorVerdict = doctorVerdict; }
    public Boolean getConfirmed() { return confirmed; }
    public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }
    public Boolean getConcordanciaIa() { return concordanciaIa; }
    public void setConcordanciaIa(Boolean concordanciaIa) { this.concordanciaIa = concordanciaIa; }
    public AiProcessingStatus getStatusProcessamentoIa() { return statusProcessamentoIa; }
    public void setStatusProcessamentoIa(AiProcessingStatus statusProcessamentoIa) { this.statusProcessamentoIa = statusProcessamentoIa; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Consultation getConsultation() { return consultation; }
    public void setConsultation(Consultation consultation) { this.consultation = consultation; }
    public Lesion getLesion() { return lesion; }
    public void setLesion(Lesion lesion) { this.lesion = lesion; }
    public List<AiPrediction> getPredictions() { return predictions; }
    public void setPredictions(List<AiPrediction> predictions) { this.predictions = predictions; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
