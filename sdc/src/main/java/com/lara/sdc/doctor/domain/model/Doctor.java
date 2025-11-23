package com.lara.sdc.doctor.domain.model;

import com.lara.sdc.user.domain.model.User;
import jakarta.persistence.Entity;

@Entity
public class Doctor extends User {

    public Doctor() {
        super();
    }

    public Doctor(String nome, String cpf, String email, String password) {
        super(nome, cpf, email, password);
    }
}