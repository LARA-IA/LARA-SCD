package com.lara.scd.predict.domain.model;

import com.lara.scd.patient.domain.model.PatientImage;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_predictions")
public class AiPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_image_id", nullable = false)
    private PatientImage patientImage;

    @Column(name = "versao_modelo", nullable = false)
    private String versaoModelo;

    @Column(name = "classe_inferida")
    private String classeInferida; // MALIGNO / BENIGNO

    private Double confianca;

    @Column(name = "mult_classe")
    private String multClasse; // mel, bcc, nv, etc.

    @Column(name = "confianca_mult_classe")
    private Double confiancaMultClasse;

    @Column(name = "probabilidades_json", columnDefinition = "TEXT")
    private String probabilidadesJson; // Vetor softmax completo em JSON (RNF04)

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }

    public AiPrediction() {}

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PatientImage getPatientImage() { return patientImage; }
    public void setPatientImage(PatientImage patientImage) { this.patientImage = patientImage; }
    public String getVersaoModelo() { return versaoModelo; }
    public void setVersaoModelo(String versaoModelo) { this.versaoModelo = versaoModelo; }
    public String getClasseInferida() { return classeInferida; }
    public void setClasseInferida(String classeInferida) { this.classeInferida = classeInferida; }
    public Double getConfianca() { return confianca; }
    public void setConfianca(Double confianca) { this.confianca = confianca; }
    public String getMultClasse() { return multClasse; }
    public void setMultClasse(String multClasse) { this.multClasse = multClasse; }
    public Double getConfiancaMultClasse() { return confiancaMultClasse; }
    public void setConfiancaMultClasse(Double confiancaMultClasse) { this.confiancaMultClasse = confiancaMultClasse; }
    public String getProbabilidadesJson() { return probabilidadesJson; }
    public void setProbabilidadesJson(String probabilidadesJson) { this.probabilidadesJson = probabilidadesJson; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
