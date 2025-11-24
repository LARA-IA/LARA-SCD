package com.lara.scd.patient.domain.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "patient_images")
public class PatientImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String base64Data;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PredictionClass predictionClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorVerdict doctorVerdict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    // Getters e Setters
}
