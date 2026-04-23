package com.siteagent.backend.site;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.siteagent.backend.exception.CustomException;
import com.siteagent.backend.site.request.SiteCreateRequest;
import com.siteagent.backend.site.response.SiteDetailResponse;
import com.siteagent.backend.site.response.SiteListResponse;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/admin/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;


    // 현장 등록
    @PostMapping
    public ResponseEntity<?> createSite(
            HttpServletRequest request, SiteCreateRequest requestDTO,
            @RequestParam("name") String name,
            @RequestParam("managerName") String managerName,
            @RequestParam("managerPhone") String managerPhone,
            @RequestParam("address") String address,
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam(value = "file", required = false) MultipartFile file) {
            
        Long adminId = (Long) request.getAttribute("adminId");
        String role = (String) request.getAttribute("role");
            
        if (!"SUPER_ADMIN".equals(role)) {
            throw new CustomException(403, "권한이 없습니다.");
        }

        siteService.siteCreate(requestDTO, adminId);
        return ResponseEntity.ok().build();
    }

    // 현장 목록 조회
    @GetMapping
    public ResponseEntity<List<SiteListResponse>> getSiteList(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        return ResponseEntity.ok(siteService.getSiteList(adminId));
    }

    //현장 상세보기 
    @GetMapping("/{siteId}")
    public ResponseEntity<?> getSite(@PathVariable("siteId") Long siteId,
                                      HttpServletRequest request) {
        SiteDetailResponse response = siteService.getSite(siteId);
        return ResponseEntity.ok(response);
    }
    
    // 현장 수정
    @PutMapping("/{siteId}")
    public ResponseEntity<?> updateSite(
            @PathVariable("siteId") Long siteId,
            @RequestParam("name") String name,
            @RequestParam("managerName") String managerName,
            @RequestParam("managerPhone") String managerPhone,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
    
        String role = (String) request.getAttribute("role");
    
        if (!"SUPER_ADMIN".equals(role) && !"SITE_ADMIN".equals(role)) {
            throw new CustomException(403, "권한이 없습니다.");
        }
    
        siteService.siteUpdate(siteId, name, managerName, managerPhone, file);
        return ResponseEntity.ok().build();
    }
    

}