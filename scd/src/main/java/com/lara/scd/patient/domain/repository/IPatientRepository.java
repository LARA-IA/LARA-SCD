package com.lara.scd.patient.domain.repository;

import com.lara.scd.patient.domain.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface IPatientRepository extends JpaRepository<Patient, UUID> {
}
