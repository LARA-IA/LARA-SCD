package com.lara.scd.patient.application;

import com.lara.scd.patient.application.dto.PatientImageResponseDto;
import com.lara.scd.patient.application.dto.PatientRegisterRequestDto;
import com.lara.scd.patient.application.dto.PatientResponseDto;
import com.lara.scd.patient.domain.model.DoctorVerdict;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.patient.domain.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
@Tag(name = "Patient", description = "Endpoints para pacientes")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(summary = "Cadastrar novo paciente", description = "Cria um novo paciente na plataforma SCD (separado da consulta).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paciente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<PatientResponseDto> createPatient(@Validated @RequestBody PatientRegisterRequestDto dto) {
        Patient patient = patientService.registerPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(PatientResponseDto.from(patient));
    }

    @Operation(summary = "Buscar paciente por CPF", description = "Retorna o paciente com o CPF informado")
    @GetMapping("/search")
    public ResponseEntity<PatientResponseDto> searchByCpf(@RequestParam String cpf) {
        return patientService.findByCpf(cpf)
                .map(p -> ResponseEntity.ok(PatientResponseDto.from(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar paciente por ID", description = "Retorna o paciente com o ID informado")
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDto> getPatient(@PathVariable UUID id) {
        return ResponseEntity.ok(PatientResponseDto.from(patientService.findById(id)));
    }

    @Operation(summary = "Listar pacientes do médico", description = "Retorna todos os pacientes vinculados ao médico logado")
    @GetMapping
    public ResponseEntity<List<PatientResponseDto>> listPatients() {
        List<PatientResponseDto> dtos = patientService.listByDoctorId().stream()
                .map(PatientResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/images/{imageId}/confirm")
    public ResponseEntity<PatientImageResponseDto> confirmDiagnosis(
            @PathVariable UUID imageId,
            @RequestBody DoctorVerdict verdict) {
        PatientImage image = patientService.confirmDiagnosis(imageId, verdict);
        return ResponseEntity.ok(PatientImageResponseDto.from(image));
    }
}
