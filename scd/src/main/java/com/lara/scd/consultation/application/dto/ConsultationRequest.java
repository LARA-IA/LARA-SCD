package com.lara.scd.consultation.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public class ConsultationRequest {

    @NotBlank
    private String nome;

    @NotBlank
    private String cpf;

    @NotNull
    private String sexo;

    private LocalDate dataNascimento;

    @NotEmpty
    private List<String> localizacoes;

    private List<MultipartFile> images;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public List<String> getLocalizacoes() { return localizacoes; }
    public void setLocalizacoes(List<String> localizacoes) { this.localizacoes = localizacoes; }
    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
}
