package com.lara.sdc.user.domain.model;

import jakarta.persistence.*;


import java.util.UUID;

@Entity
@Table(name = "users")
public abstract class User {

    public User(){}

    public User(String nome , String cpf, String email, String password ) {
        this.email = email;
        this.password = password;
        this.nome  = nome;
        this.cpf = cpf;
    }

    public UUID getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false,unique = true)
    private String cpf;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}