package com.lara.scd.user.application.dto;

import com.lara.scd.user.domain.model.AcessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class LoginResponseDto {
    private String token;
    private String type;
    private UUID id;
    private String nome;
    private String email;
    private AcessLevel accessLevel;

    public LoginResponseDto(String type, String token, UUID id, String nome, String email, AcessLevel accessLevel) {
        this.type = type;
        this.token = token;
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.accessLevel = accessLevel;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AcessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AcessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
}