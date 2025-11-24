package com.lara.scd.user.application;

import com.lara.scd.user.application.dto.LoginRequestDto;
import com.lara.scd.user.application.dto.LoginResponseDto;
import com.lara.scd.user.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Authentication", description = "Endpoints de autenticação")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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
}