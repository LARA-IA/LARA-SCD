package com.lara.scd.lesion.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lara.scd.patient.domain.model.Localizacao;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.model.PatientImage;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lesions")
public class Lesion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "localizacao_anatomica", nullable = false)
    private Localizacao localizacaoAnatomica;

    private String descricao;

    @OneToMany(mappedBy = "lesion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PatientImage> registros = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }

    public Lesion() {}

    public Lesion(Patient patient, Localizacao localizacaoAnatomica, String descricao) {
        this.patient = patient;
        this.localizacaoAnatomica = localizacaoAnatomica;
        this.descricao = descricao;
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Localizacao getLocalizacaoAnatomica() { return localizacaoAnatomica; }
    public void setLocalizacaoAnatomica(Localizacao localizacaoAnatomica) { this.localizacaoAnatomica = localizacaoAnatomica; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public List<PatientImage> getRegistros() { return registros; }
    public void setRegistros(List<PatientImage> registros) { this.registros = registros; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
