package com.lara.sdc.user.application;

import com.lara.sdc.config.security.JwtUtil;
import com.lara.sdc.user.application.dto.LoginRequestDto;
import com.lara.sdc.user.application.dto.LoginResponseDto;
import com.lara.sdc.user.domain.model.User;
import com.lara.sdc.user.domain.repository.IUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Authentication", description = "Endpoints de autenticação")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, IUserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Retorna JWT token válido por 24 horas")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            String token = jwtUtil.generateToken(request.getEmail(), user);
            return ResponseEntity.ok(new LoginResponseDto(token, "Bearer"));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Email ou senha inválidos");
        }
    }
}