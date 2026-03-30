package com.lara.scd.patient.application.dto;

import com.lara.scd.patient.domain.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PatientResponseDto(
        UUID id,
        String nome,
        String cpf,
        String sexo,
        LocalDate dataNascimento,
        Boolean termoConsentimentoIa,
        LocalDateTime dataConsentimento,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static PatientResponseDto from(Patient p) {
        return new PatientResponseDto(
                p.getId(),
                p.getNome(),
                p.getCpf(),
                p.getSexo(),
                p.getDataNascimento(),
                p.getTermoConsentimentoIa(),
                p.getDataConsentimento(),
                p.getCriadoEm(),
                p.getAtualizadoEm()
        );
    }
}
