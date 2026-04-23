package com.siteagent.backend.site;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

import com.siteagent.backend.admin.AdminRepository;
import com.siteagent.backend.common.SupabaseStorageService;
import com.siteagent.backend.exception.CustomException;
import com.siteagent.backend.site.request.SiteCreateRequest;
import com.siteagent.backend.site.response.SiteDetailResponse;
import com.siteagent.backend.site.response.SiteListResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository siteRepository;
    private final AdminRepository adminRepository;
    private final SitePdfRepository sitePdfRepository;
    private final SupabaseStorageService supabaseStorageService;

    @Value("${fastapi.url}")
    private String fastApiUrl;

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

    // 현장 상세보기
    public SiteDetailResponse getSite(Long siteId) {
        Site site = siteRepository.findById(siteId).orElseThrow  
        (() -> new CustomException(404, "존재하지 않는 현장입니다."));
        
        return new SiteDetailResponse(siteId, site.getName(), site.getAddress(), site.getLat(),  site.getLng(),
        site.getManagerName(),
        site.getManagerPhone(),
        site.getSitePdfs().stream()
            .filter(pdf -> pdf.getIsActive())
            .map(pdf -> new SiteDetailResponse.PdfInfo(
                pdf.getId(),
                pdf.getFileName(),
                pdf.getFilePath()
            ))
            .toList()
        );
    }


    //현장 수정하기
    @Transactional
    public void siteUpdate(Long siteId, String name, String managerName,
                           String managerPhone, MultipartFile file) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new CustomException(404, "존재하지 않는 현장입니다."));

        site.update(name, managerName, managerPhone);

        if (file != null && !file.isEmpty()) {
            sitePdfRepository.deactivateAllBySiteId(siteId);

            String fileUrl = supabaseStorageService.uploadPdf(siteId, file);

            SitePdf sitePdf = SitePdf.builder()
                    .site(site)
                    .fileName(file.getOriginalFilename())
                    .filePath(fileUrl)
                    .isActive(true)
                    .build();
            sitePdfRepository.save(sitePdf);

            sendPdfUrlToFastApi(siteId, fileUrl);
        }
    }

    private void sendPdfUrlToFastApi(Long siteId, String fileUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"site_id\": " + siteId + ", \"pdf_url\": \"" + fileUrl + "\"}";
            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    fastApiUrl + "/upload-pdf",
                    requestEntity,
                    String.class
            );
        } catch (Exception e) {
            throw new CustomException(500, "FastAPI 전송 실패");
        }
    }
}