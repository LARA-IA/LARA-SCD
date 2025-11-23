package com.lara.scd.doctor.application;

import com.lara.scd.doctor.application.dto.DoctorRegisterRequestDto;
import com.lara.scd.doctor.domain.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@Tag(name = "Doctor", description = "Endpoints para médicos")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @Operation(
            summary = "Registro de novo Medicos",
            description = "Cria um novo Medicos na plataforma SCD."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Medicos criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação ou campos obrigatórios)"),
            @ApiResponse(responseCode = "409", description = "Conflito: Usuário com este e-mail ou CPF já existe")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> createAdmin(@Validated @RequestBody DoctorRegisterRequestDto dto) {
        doctorService.registerDoctor(dto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
