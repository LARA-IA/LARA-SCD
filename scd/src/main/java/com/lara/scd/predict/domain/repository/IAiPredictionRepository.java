package com.lara.scd.predict.domain.repository;

import com.lara.scd.predict.domain.model.AiPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IAiPredictionRepository extends JpaRepository<AiPrediction, UUID> {

    List<AiPrediction> findByPatientImageId(UUID patientImageId);

    List<AiPrediction> findByPatientImageIdOrderByCriadoEmDesc(UUID patientImageId);
}
