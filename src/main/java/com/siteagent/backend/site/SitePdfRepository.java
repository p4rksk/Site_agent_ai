package com.siteagent.backend.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SitePdfRepository extends JpaRepository<SitePdf, Long> {

    @Modifying
    @Query("UPDATE SitePdf p SET p.isActive = false WHERE p.site.id = :siteId")
    void deactivateAllBySiteId(@Param("siteId") Long siteId);
}