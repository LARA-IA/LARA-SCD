package com.lara.scd.user.domain.service;

import com.lara.scd.config.security.JwtUtil;
import com.lara.scd.user.application.dto.LoginRequestDto;
import com.lara.scd.user.application.dto.LoginResponseDto;
import com.lara.scd.user.domain.model.User;
import com.lara.scd.user.domain.repository.IUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IUserRepository userRepository;

    public UserService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, IUserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    public LoginResponseDto login(LoginRequestDto request) {

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

            return new LoginResponseDto(
                    "Bearer",
                    token,
                    user.getId(),
                    user.getNome(),
                    user.getEmail(),
                    user.getAccessLevel()
            );

        } catch (AuthenticationException e) {
            throw new RuntimeException("Email ou senha inválidos");
        }
    }
}
