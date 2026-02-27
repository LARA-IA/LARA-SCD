package com.lara.scd.manager.application.dto;

public record DashboardResponseDto(
        long totalPatients,
        long totalDoctors,
        long totalImages
) {}
