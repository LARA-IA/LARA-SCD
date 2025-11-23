package com.lara.sdc;

import com.lara.sdc.doctor.domain.model.Doctor;
import com.lara.sdc.manager.domain.model.Manager;
import com.lara.sdc.user.domain.repository.IUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SdcApplication {

	public static void main(String[] args) {
		SpringApplication.run(SdcApplication.class, args);
	}
    @Bean
    @Profile({"dev","prod"})
    public CommandLineRunner devProfile(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verificar se os usuários já existem
            if (userRepository.findByEmail("doctor@example.com").isEmpty()) {
                Doctor doctor = new Doctor("Dr. João Silva", "12345678901", "doctor@example.com", passwordEncoder.encode("password123"));
                userRepository.save(doctor);
                System.out.println("✅ Usuário Doctor criado: doctor@example.com");
            }

            if (userRepository.findByEmail("manager@example.com").isEmpty()) {
                Manager manager = new Manager("Gerente Maria", "98765432101", "manager@example.com", passwordEncoder.encode("password123"));
                userRepository.save(manager);
                System.out.println("✅ Usuário Manager criado: manager@example.com");
            }
        };
    }


}
