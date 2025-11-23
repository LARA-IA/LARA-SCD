package com.lara.scd.doctor.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record DoctorRegisterRequestDto(
        @NotBlank
        String nome,

        @NotBlank
        @CPF
        String cpf,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String CRM
) {}
