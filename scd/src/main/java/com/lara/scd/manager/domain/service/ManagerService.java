package com.lara.scd.manager.domain.service;

import com.lara.scd.manager.application.dto.ManagerRegisterRequestDto;
import com.lara.scd.exception.UnicidadeVioladaException;
import com.lara.scd.manager.domain.model.Manager;
import com.lara.scd.manager.domain.repository.IManagerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerService {

    private final IManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerService(IManagerRepository managerRepository, PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
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
}
