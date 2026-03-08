package com.lara.scd.consultation.application.dto;

import com.lara.scd.patient.domain.model.DoctorVerdict;
import jakarta.validation.constraints.NotNull;

public class ConfirmDiagnosisRequest {

    @NotNull
    private DoctorVerdict finalDiagnosis;

    public DoctorVerdict getFinalDiagnosis() { return finalDiagnosis; }
    public void setFinalDiagnosis(DoctorVerdict finalDiagnosis) { this.finalDiagnosis = finalDiagnosis; }
}
