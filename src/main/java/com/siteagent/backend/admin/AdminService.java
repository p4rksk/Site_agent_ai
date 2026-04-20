package com.siteagent.backend.admin;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siteagent.backend.admin.request.AdminSignupRequest;
import com.siteagent.backend.exception.CustomException;
import com.siteagent.backend.site_admin.SiteAdmin;
import com.siteagent.backend.site_admin.SiteAdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {
    
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SiteAdminRepository siteAdminRepository;

    // 회원가입
    @Transactional
    public void signUp(AdminSignupRequest request ){
        
        if (adminRepository.existsByloginId(request.adminId())) {
            throw new CustomException(409, "이미 사용중인 아이디입니다.");
        }
        if (siteAdminRepository.existsByLoginId(request.siteAdminId())) {
            throw new CustomException(409, "이미 사용중인 현장관리자 아이디입니다.");
        }
    
        
        Admin admin = adminRepository.save(
            Admin.builder()
                .companyName(request.companyName())
                .businessNumber(request.businessNumber())
                .phone(request.phone())
                .loginId(request.adminId())
                .password(passwordEncoder.encode(request.adminPassword()))
                .build()
        );
    
        
       SiteAdmin siteAdmin =  siteAdminRepository.save(
            SiteAdmin.builder()
                .admin(admin)
                .loginId(request.siteAdminId())
                .password(passwordEncoder.encode(request.siteAdminPassword()))
                .build()
        );
    } ;

    //Admin 아이디 중복체크
    public boolean checkDuplicateAdminId(String loginId) {
        return adminRepository.existsByloginId(loginId);
    }


    public boolean checkDuplicateSiteAdminId(String loginId) {
        return siteAdminRepository.existsByLoginId(loginId);
    }
}