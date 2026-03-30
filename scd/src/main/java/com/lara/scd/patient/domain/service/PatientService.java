package com.lara.scd.patient.domain.service;

import com.lara.scd.config.security.SecurityContext;
import com.lara.scd.doctor.domain.model.Doctor;
import com.lara.scd.doctor.domain.repository.IDoctorRepository;
import com.lara.scd.patient.application.dto.PatientRegisterRequestDto;
import com.lara.scd.patient.domain.model.DoctorVerdict;
import com.lara.scd.patient.domain.model.Patient;
import com.lara.scd.patient.domain.model.PatientImage;
import com.lara.scd.patient.domain.repository.IPatientImageRepository;
import com.lara.scd.patient.domain.repository.IPatientRepository;
import com.lara.scd.user.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PatientService {

    private final IPatientRepository patientRepository;
    private final IPatientImageRepository imageRepository;
    private final IDoctorRepository doctorRepository;
    private final SecurityContext securityContext;

    public PatientService(IPatientRepository patientRepository,
                          IPatientImageRepository imageRepository,
                          IDoctorRepository doctorRepository,
                          SecurityContext securityContext) {
        this.patientRepository = patientRepository;
        this.imageRepository = imageRepository;
        this.doctorRepository = doctorRepository;
        this.securityContext = securityContext;
    }

    @Transactional
    public Patient registerPatient(PatientRegisterRequestDto dto) {
        // Check if patient with same CPF already exists
        Optional<Patient> existing = patientRepository.findByCpf(dto.cpf());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Paciente com CPF '" + dto.cpf() + "' já está cadastrado.");
        }

        Patient newPatient = new Patient(dto.nome(), dto.cpf(), dto.dataNascimento(), dto.sexo());

        // LGPD consent
        if (dto.termoConsentimentoIa() != null && dto.termoConsentimentoIa()) {
            newPatient.setTermoConsentimentoIa(true);
            newPatient.setDataConsentimento(LocalDateTime.now());
        } else {
            newPatient.setTermoConsentimentoIa(false);
        }

        // Link to current doctor
        User currentUser = securityContext.getCurrentUser();
        Doctor doctor = doctorRepository.findById(currentUser.getId()).orElse(null);
        if (doctor != null) {
            newPatient.setDoctor(doctor);
        }

        return patientRepository.save(newPatient);
    }

    public Optional<Patient> findByCpf(String cpf) {
        return patientRepository.findByCpf(cpf);
    }

    public Patient findById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado"));
    }

    public List<Patient> listByDoctorId() {
        User currentUser = securityContext.getCurrentUser();
        return patientRepository.findByDoctorId(currentUser.getId());
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
