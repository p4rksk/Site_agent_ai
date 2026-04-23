package com.siteagent.backend.site_admin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SiteAdminRepository extends JpaRepository<SiteAdmin, Long> {
    Optional<SiteAdmin> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}