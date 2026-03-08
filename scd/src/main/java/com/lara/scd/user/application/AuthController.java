package com.lara.scd.user.application;

import com.lara.scd.doctor.application.dto.DoctorRegisterRequestDto;
import com.lara.scd.doctor.domain.service.DoctorService;
import com.lara.scd.user.application.dto.LoginRequestDto;
import com.lara.scd.user.application.dto.LoginResponseDto;
import com.lara.scd.user.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticação")
public class AuthController {

    private final UserService userService;
    private final DoctorService doctorService;

    public AuthController(UserService userService, DoctorService doctorService) {
        this.userService = userService;
        this.doctorService = doctorService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Retorna JWT token válido por 24 horas")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            return ResponseEntity.ok(userService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @Operation(summary = "Registro de novo Médico", description = "Cadastro de novos médicos no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Médico cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou duplicados")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody DoctorRegisterRequestDto dto) {
        doctorService.registerDoctor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
