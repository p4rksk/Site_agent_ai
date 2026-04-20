package com.siteagent.backend.admin;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByloginId(String loginId);

    boolean existsByloginId(String loginId);
}