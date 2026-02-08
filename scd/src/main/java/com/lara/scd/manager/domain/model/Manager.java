package com.lara.scd.manager.domain.model;

import com.lara.scd.user.domain.model.AccessLevel;
import com.lara.scd.user.domain.model.User;
import jakarta.persistence.Entity;

@Entity
public class Manager extends User {

    public Manager() {
        super();
    }

    public Manager(String nome, String cpf, String email, String password) {
        super(nome, cpf, email, password, AccessLevel.MANAGER, true);
    }
}