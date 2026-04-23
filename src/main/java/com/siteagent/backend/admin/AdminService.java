package com.siteagent.backend.admin;


import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siteagent.backend.admin.request.AdminLoginRequest;
import com.siteagent.backend.admin.request.AdminSignupRequest;
import com.siteagent.backend.admin.response.LoginResponse;
import com.siteagent.backend.common.security.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;

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


    // 로그인
    public LoginResponse login(AdminLoginRequest request) {
        Optional<Admin> admin = adminRepository.findByloginId(request.loginId());
        System.out.println(1);
        if (admin.isPresent()) {
            if (!passwordEncoder.matches(request.password(), admin.get().getPassword())) {
                throw new CustomException(401, "비밀번호가 틀렸습니다.");
            }
            String tocken =  jwtTokenProvider.createToken(admin.get().getId(), request.loginId(), "SUPER_ADMIN");
            return new LoginResponse(tocken, "SUPER_ADMIN", admin.get().getId(), admin.get().getCompanyName());
            
        }

        Optional<SiteAdmin> siteAdmin = siteAdminRepository.findByLoginId(request.loginId());
        if(siteAdmin.isPresent()) {
            if (!passwordEncoder.matches(request.password(), siteAdmin.get().getPassword())) {
                throw new CustomException(401, "비밀번호가 틀렸습니다.");
            }
            String tocken =  jwtTokenProvider.createToken(siteAdmin.get().getId(), request.loginId(), "SITE_ADMIN");
            return new LoginResponse(tocken, "SITE_ADMIN", siteAdmin.get().getId(), null);
        }

        throw new CustomException(404, "존재하지 않는 아이디 입니다.");
    }
}