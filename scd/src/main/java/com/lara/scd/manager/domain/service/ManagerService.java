package com.lara.scd.manager.domain.service;

import com.lara.scd.doctor.domain.repository.IDoctorRepository;
import com.lara.scd.manager.application.dto.ManagerRegisterRequestDto;
import com.lara.scd.exception.UnicidadeVioladaException;
import com.lara.scd.manager.domain.model.Manager;
import com.lara.scd.manager.domain.repository.IManagerRepository;
import com.lara.scd.patient.domain.repository.IPatientImageRepository;
import com.lara.scd.patient.domain.repository.IPatientRepository;
import com.lara.scd.shared.service.FileStorageService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerService {

    private final IManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final IPatientRepository patientRepository;
    private final IDoctorRepository doctorRepository;
    private final IPatientImageRepository imageRepository;
    private final FileStorageService fileStorageService;


    public ManagerService(IManagerRepository managerRepository, PasswordEncoder passwordEncoder,
                          IPatientRepository patientRepository,
                          IDoctorRepository doctorRepository,
                          IPatientImageRepository imageRepository,
                          FileStorageService fileStorageService) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.imageRepository = imageRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public void registerManager(ManagerRegisterRequestDto dto) {
        if (managerRepository.existsByEmail(dto.email())) {
            throw new UnicidadeVioladaException("E-mail '" + dto.email() + "' já está cadastrado no sistema.");
        }

        Manager newManager = new Manager(
                dto.nome(),
                dto.cpf(),
                dto.email(),
                passwordEncoder.encode(dto.password())
        );
        managerRepository.save(newManager);
    }

    public com.lara.scd.manager.application.dto.DashboardResponseDto getDashboardStats() {
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalImages = imageRepository.count();

        return new com.lara.scd.manager.application.dto.DashboardResponseDto(totalPatients, totalDoctors, totalImages);
    }

    public org.springframework.core.io.Resource getBackupZip() {
        return fileStorageService.getBackupZip();
    }
}
