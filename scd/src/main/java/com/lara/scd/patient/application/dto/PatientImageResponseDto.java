package com.lara.scd.patient.application.dto;

import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.predict.domain.model.AiPrediction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record PatientImageResponseDto(
        UUID id,
        String filePath,
        String fileName,
        Long fileSize,
        String contentType,
        String localizacao,
        String doctorVerdict,
        Boolean confirmed,
        Boolean concordanciaIa,
        String statusProcessamentoIa,
        UUID lesionId,
        List<AiPredictionDto> predictions,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static PatientImageResponseDto from(PatientImage img) {
        return new PatientImageResponseDto(
                img.getId(),
                img.getFilePath(),
                img.getFileName(),
                img.getFileSize(),
                img.getContentType(),
                img.getLocalizacao() != null ? img.getLocalizacao().name() : null,
                img.getDoctorVerdict() != null ? img.getDoctorVerdict().name() : null,
                img.getConfirmed(),
                img.getConcordanciaIa(),
                img.getStatusProcessamentoIa() != null ? img.getStatusProcessamentoIa().name() : null,
                img.getLesion() != null ? img.getLesion().getId() : null,
                img.getPredictions() != null
                        ? img.getPredictions().stream().map(AiPredictionDto::from).collect(Collectors.toList())
                        : List.of(),
                img.getCriadoEm(),
                img.getAtualizadoEm()
        );
    }

    public record AiPredictionDto(
            UUID id,
            String versaoModelo,
            String classeInferida,
            Double confianca,
            String multClasse,
            Double confiancaMultClasse,
            LocalDateTime criadoEm
    ) {
        public static AiPredictionDto from(AiPrediction pred) {
            return new AiPredictionDto(
                    pred.getId(),
                    pred.getVersaoModelo(),
                    pred.getClasseInferida(),
                    pred.getConfianca(),
                    pred.getMultClasse(),
                    pred.getConfiancaMultClasse(),
                    pred.getCriadoEm()
            );
        }
    }
}
