package com.lara.scd.patient.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lara.scd.consultation.domain.model.Consultation;
import com.lara.scd.doctor.domain.model.Doctor;
import com.lara.scd.lesion.domain.model.Lesion;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String sexo;

    @Column(name = "termo_consentimento_ia")
    private Boolean termoConsentimentoIa;

    @Column(name = "data_consentimento")
    private LocalDateTime dataConsentimento;

    @Column(name = "data_revogacao_consentimento")
    private LocalDateTime dataRevogacaoConsentimento;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Consultation> consultations = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Lesion> lesoes = new ArrayList<>();

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

    public Patient() {}

    public Patient(String nome, String cpf, LocalDate dataNascimento, String sexo) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public Boolean getTermoConsentimentoIa() { return termoConsentimentoIa; }
    public void setTermoConsentimentoIa(Boolean termoConsentimentoIa) { this.termoConsentimentoIa = termoConsentimentoIa; }
    public LocalDateTime getDataConsentimento() { return dataConsentimento; }
    public void setDataConsentimento(LocalDateTime dataConsentimento) { this.dataConsentimento = dataConsentimento; }
    public LocalDateTime getDataRevogacaoConsentimento() { return dataRevogacaoConsentimento; }
    public void setDataRevogacaoConsentimento(LocalDateTime dataRevogacaoConsentimento) { this.dataRevogacaoConsentimento = dataRevogacaoConsentimento; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public List<Consultation> getConsultations() { return consultations; }
    public void setConsultations(List<Consultation> consultations) { this.consultations = consultations; }
    public List<Lesion> getLesoes() { return lesoes; }
    public void setLesoes(List<Lesion> lesoes) { this.lesoes = lesoes; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
