package com.lara.scd.doctor.application.dto;

import java.util.UUID;

public record DoctorRegisterResponseDto(
        UUID id,
        String nome,
        String email,
        String CRM
) {}
