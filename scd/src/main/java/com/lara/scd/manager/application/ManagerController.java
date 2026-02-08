package com.lara.scd.manager.application;

import com.lara.scd.manager.application.dto.ManagerRegisterRequestDto;
import com.lara.scd.manager.domain.service.ManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@Tag(name = "Manager", description = "Endpoints para gerentes")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @Operation(
            summary = "Registro de novo Manager",
            description = "Cria um novo Manager na plataforma SCD."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Manager criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (falha na validação ou campos obrigatórios)"),
            @ApiResponse(responseCode = "409", description = "Conflito: Usuário com este e-mail ou CPF já existe")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> createManager(@Validated @RequestBody ManagerRegisterRequestDto dto) {
        managerService.registerManager(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
