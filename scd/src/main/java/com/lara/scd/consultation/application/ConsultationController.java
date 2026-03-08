package com.lara.scd.consultation.application;

import com.lara.scd.consultation.application.dto.ConfirmDiagnosisRequest;
import com.lara.scd.consultation.application.dto.ConsultationRequest;
import com.lara.scd.consultation.application.dto.ConsultationResponse;
import com.lara.scd.consultation.domain.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medico/consultations")
@Tag(name = "Consultations", description = "Endpoints de consultas médicas")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @Operation(summary = "Criar consulta com imagens", description = "Cria consulta com paciente e múltiplas imagens. Cada imagem é enviada para o microsserviço de IA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consulta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConsultationResponse> createConsultation(@ModelAttribute ConsultationRequest request) {
        ConsultationResponse response = consultationService.createConsultation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Confirmar diagnóstico da consulta", description = "Confirma ou altera o diagnóstico da IA para a consulta inteira")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ConsultationResponse> confirmDiagnosis(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmDiagnosisRequest request) {
        return ResponseEntity.ok(consultationService.confirmConsultationDiagnosis(id, request));
    }

    @Operation(summary = "Confirmar diagnóstico de imagem individual", description = "Confirma ou altera o diagnóstico de uma imagem específica")
    @PutMapping("/images/{imageId}/confirm")
    public ResponseEntity<ConsultationResponse> confirmImageDiagnosis(
            @PathVariable UUID imageId,
            @Valid @RequestBody ConfirmDiagnosisRequest request) {
        return ResponseEntity.ok(consultationService.confirmImageDiagnosis(imageId, request));
    }

    @Operation(summary = "Listar consultas do médico", description = "Retorna todas as consultas do médico logado (filtros opcionais: nome, cpf)")
    @GetMapping
    public ResponseEntity<List<ConsultationResponse>> listConsultations(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf) {
        return ResponseEntity.ok(consultationService.listConsultations(nome, cpf));
    }

    @Operation(summary = "Obter consulta por ID", description = "Retorna detalhes completos de uma consulta")
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> getConsultation(@PathVariable UUID id) {
        return ResponseEntity.ok(consultationService.getConsultation(id));
    }
}
