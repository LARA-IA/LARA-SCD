package com.lara.scd.lesion.domain.repository;

import com.lara.scd.lesion.domain.model.Lesion;
import com.lara.scd.patient.domain.model.Localizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ILesionRepository extends JpaRepository<Lesion, UUID> {

    List<Lesion> findByPatientId(UUID patientId);

    Optional<Lesion> findByPatientIdAndLocalizacaoAnatomica(UUID patientId, Localizacao localizacao);
}
