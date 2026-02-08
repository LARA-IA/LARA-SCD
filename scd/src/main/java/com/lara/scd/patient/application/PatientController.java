package com.lara.scd.patient.application;

import com.lara.scd.patient.application.dto.PatientRegisterRequestDto;
import com.lara.scd.patient.domain.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
@Tag(name = "Patient", description = "Endpoints para pacientes")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(
            summary = "Registro de novo Paciente",
            description = "Cria um novo Paciente na plataforma SCD."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paciente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> createPatient(@Validated @RequestBody PatientRegisterRequestDto dto) {
        patientService.registerPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
