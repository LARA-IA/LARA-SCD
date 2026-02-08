package com.lara.scd.patient.application.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record PatientRegisterRequestDto(
        @NotBlank
        String nome,

        @NotBlank
        @CPF
        String cpf
) {}
