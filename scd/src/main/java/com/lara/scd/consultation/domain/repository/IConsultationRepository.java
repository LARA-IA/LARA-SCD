package com.lara.scd.consultation.domain.repository;

import com.lara.scd.consultation.domain.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IConsultationRepository extends JpaRepository<Consultation, UUID> {

    List<Consultation> findByDoctorIdOrderByCreatedAtDesc(UUID doctorId);

    @Query("SELECT c FROM Consultation c WHERE c.doctor.id = :doctorId " +
           "AND (:nome IS NULL OR LOWER(c.patient.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:cpf IS NULL OR c.patient.cpf = :cpf) " +
           "ORDER BY c.createdAt DESC")
    List<Consultation> findByDoctorWithFilters(@Param("doctorId") UUID doctorId,
                                               @Param("nome") String nome,
                                               @Param("cpf") String cpf);
}
