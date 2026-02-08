package com.lara.scd.manager.domain.repository;

import com.lara.scd.manager.domain.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface IManagerRepository extends JpaRepository<Manager, UUID> {
    boolean existsByEmail(String email);
}
