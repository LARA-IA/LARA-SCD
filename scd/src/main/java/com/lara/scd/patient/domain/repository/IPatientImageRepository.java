package com.lara.scd.patient.domain.repository;

import com.lara.scd.patient.domain.model.PatientImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPatientImageRepository extends JpaRepository<PatientImage, UUID> {

    List<PatientImage> findByPatientId(UUID patientId);
}
