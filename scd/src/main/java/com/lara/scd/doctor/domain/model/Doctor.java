package com.lara.scd.doctor.domain.model;

import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.user.domain.model.AcessLevel;
import com.lara.scd.user.domain.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Doctor extends User {

    private boolean activated;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Patient> patients = new ArrayList<>();

    private String CRM;

    public Doctor() {
        super();
    }

    public Doctor(String nome, String cpf, String email, String password, String CRM) {
        super(nome, cpf, email, password, AcessLevel.DOCTOR);
        this.CRM = CRM;
        this.activated = true;
    }

    public boolean isActivated() {
        return activated;
    }
    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    public List<Patient> getPatients() {
        return patients;
    }
    public void setPatients(List<Patient> patients) {
        this.patients = patients != null ? patients : new ArrayList<>();
    }
    public String getCRM() {
        return CRM;
    }
    public void setCRM(String CRM) {
        this.CRM = CRM;
    }
}
