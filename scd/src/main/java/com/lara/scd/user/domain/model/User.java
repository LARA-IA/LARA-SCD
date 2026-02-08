package com.lara.scd.user.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public abstract class User {

    public User(){}

    public User(String nome, String cpf, String email, String password, AccessLevel accessLevel, boolean activated) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.cpf = cpf;
        this.accessLevel = accessLevel;
        this.activated = activated;
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

    @Column(nullable = false)
    private boolean activated;

    @Enumerated(EnumType.STRING)
    private AccessLevel accessLevel;

    public boolean isActivated() {
        return activated;
    }
    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}