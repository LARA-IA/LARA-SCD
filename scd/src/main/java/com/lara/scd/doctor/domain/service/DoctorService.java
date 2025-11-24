package com.lara.scd.doctor.domain.service;

import com.lara.scd.doctor.application.dto.DoctorRegisterRequestDto;
import com.lara.scd.exception.UnicidadeVioladaException;

import com.lara.scd.doctor.domain.model.Doctor;
import com.lara.scd.doctor.domain.repository.IDoctorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class DoctorService {

    private final IDoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(IDoctorRepository doctorRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerDoctor(DoctorRegisterRequestDto dto) {
        if (doctorRepository.existsByEmail(dto.email())) {
            throw new UnicidadeVioladaException("E-mail '" + dto.email() + "' já está cadastrado no sistema.");
        }


        Doctor newDoctor = new Doctor(
                dto.nome(),
                dto.cpf(),
                dto.email(),
                passwordEncoder.encode(dto.password()),
                dto.CRM()
        );
        doctorRepository.save(newDoctor);
    }
}