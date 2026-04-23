package com.siteagent.backend.admin;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String businessNumber; 

    @Column(nullable = false)
    private String phone;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Admin(String companyName, String loginId, String password, String businessNumber, String phone) {
        this.companyName = companyName;
        this.loginId = loginId;
        this.password = password;
        this.businessNumber = businessNumber;
        this.phone = phone;
    }

    public void updateProfile(String companyName, String password, String businessNumber, String phone) {
        this.companyName = companyName;
        this.password = password;
        this.businessNumber = businessNumber;
        this.phone = phone;
    }

    public enum AdminRole {
        SUPER_ADMIN,
        SITE_ADMIN
    }
}