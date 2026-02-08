package com.lara.scd.patient.domain.service;

import com.lara.scd.patient.application.dto.PatientRegisterRequestDto;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.repository.IPatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final IPatientRepository patientRepository;

    public PatientService(IPatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public void registerPatient(PatientRegisterRequestDto dto) {
        Patient newPatient = new Patient(dto.nome(), dto.cpf());
        patientRepository.save(newPatient);
    }
}
