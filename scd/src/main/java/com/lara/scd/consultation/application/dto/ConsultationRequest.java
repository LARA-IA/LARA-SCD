package com.lara.scd.consultation.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public class ConsultationRequest {

    @NotNull
    private UUID patientId;

    @NotEmpty
    private List<String> localizacoes;

    private List<MultipartFile> images;

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public List<String> getLocalizacoes() { return localizacoes; }
    public void setLocalizacoes(List<String> localizacoes) { this.localizacoes = localizacoes; }
    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
}
