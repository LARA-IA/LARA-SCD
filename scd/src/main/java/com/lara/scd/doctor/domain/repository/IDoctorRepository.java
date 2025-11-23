package com.lara.scd.doctor.domain.repository;

import com.lara.scd.doctor.domain.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IDoctorRepository extends JpaRepository<Doctor, UUID> {


    boolean existsByEmail(String email);


    Optional<Doctor> findByEmail(String email);
}