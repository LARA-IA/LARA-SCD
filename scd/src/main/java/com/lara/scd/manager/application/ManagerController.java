package com.lara.scd.manager.application;

import com.lara.scd.manager.application.dto.ChangePasswordRequest;
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
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints de administração")
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

    @Operation(summary = "Dashboard de estatísticas", description = "Retorna estatísticas do sistema")
    @GetMapping("/dashboard")
    public ResponseEntity<com.lara.scd.manager.application.dto.DashboardResponseDto> getDashboard() {
        return ResponseEntity.ok(managerService.getDashboardStats());
    }

    @Operation(summary = "Alterar senha do Admin", description = "Altera a senha do administrador logado")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            managerService.changePassword(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Download backup (dataset)", description = "Gera e retorna arquivo ZIP com imagens confirmadas")
    @GetMapping("/backup")
    public ResponseEntity<org.springframework.core.io.Resource> getBackupZip() {
        org.springframework.core.io.Resource file = managerService.getBackupZip();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"scd_database.zip\"")
                .body(file);
    }
}

