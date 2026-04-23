package com.siteagent.backend.site;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SiteRepository extends JpaRepository<Site, Long> {
   
    @Query(value = """
        SELECT
            s.id            AS id,
            s.name          AS name,
            s.address       AS address,
            s.lat           AS lat,
            s.lng           AS lng,
            s.manager_name  AS managerName,
            s.manager_phone AS managerPhone
        FROM site_tb s
        WHERE s.admin_id = :adminId
          AND s.deleted_at IS NULL
        ORDER BY s.id DESC
        """, nativeQuery = true)
    List<SiteListProjection> findSitesByAdminId(@Param("adminId") Long adminId);

    
}