package com.siteagent.backend.site;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_pdf_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SitePdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Boolean isActive;

    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public SitePdf(Site site, String fileName, String filePath) {
        this.site = site;
        this.fileName = fileName;
        this.filePath = filePath;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }
}