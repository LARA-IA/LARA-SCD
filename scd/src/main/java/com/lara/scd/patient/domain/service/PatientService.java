package com.lara.scd.patient.domain.service;

import com.lara.scd.patient.application.dto.PatientRegisterRequestDto;
import com.lara.scd.patient.domain.model.DoctorVerdict;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.patient.domain.repository.IPatientImageRepository;
import com.lara.scd.patient.domain.repository.IPatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PatientService {

    private final IPatientRepository patientRepository;
    private final IPatientImageRepository imageRepository;

    public PatientService(IPatientRepository patientRepository, IPatientImageRepository imageRepository) {
        this.patientRepository = patientRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public void registerPatient(PatientRegisterRequestDto dto) {
        Patient newPatient = new Patient(dto.nome(), dto.cpf(), dto.dataNascimento(), dto.sexo());
        patientRepository.save(newPatient);
    }

    @Transactional
    public PatientImage confirmDiagnosis(UUID imageId, DoctorVerdict verdict) {
        PatientImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));
        
        image.setDoctorVerdict(verdict);
        image.setConfirmed(true);
        return imageRepository.save(image);
    }
}
