package com.siteagent.backend.site;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siteagent.backend.admin.AdminRepository;
import com.siteagent.backend.exception.CustomException;
import com.siteagent.backend.site.request.SiteCreateRequest;
import com.siteagent.backend.site.response.SiteListResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository siteRepository;
    private final AdminRepository adminRepository;

    //현장 등록
    @Transactional
    public void siteCreate (SiteCreateRequest requestDTO, Long loginId){
        com.siteagent.backend.admin.Admin admin = adminRepository.findById(loginId).orElseThrow
        (() -> new CustomException(401, "인증되지 않은 사용자 입니다."));
        
        Site sit =  Site.builder()
            .admin(admin)
            .name(requestDTO.name())
            .address(requestDTO.address())
            .lat(requestDTO.lat())
            .lng(requestDTO.lng())
            .managerName(requestDTO.managerName())
            .managerPhone(requestDTO.managerPhone())
            .build();
            
        siteRepository.save(sit);     
    }

    // 현장 목록조회 
    public List<SiteListResponse> getSiteList(Long adminId) {
        return siteRepository.findSitesByAdminId(adminId)
                .stream()
                .map(SiteListResponse::from)
                .collect(Collectors.toList());
    }

}