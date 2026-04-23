package com.siteagent.backend.site;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.siteagent.backend.admin.Admin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "site_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
    private List<SitePdf> sitePdfs = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private Double lat;
    private Double lng;
    private String managerName;
    private String managerPhone;
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Site(Admin admin, String name, String address, Double lat, Double lng,
                String managerName, String managerPhone) {
        this.admin = admin;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
    }

    public void update(String name, 
                       String managerName, String managerPhone) {
        this.name = name;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}