package com.lara.scd.patient.domain.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private boolean activated;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<PatientImage> images = new ArrayList<>();

    // Getters e Setters
}
