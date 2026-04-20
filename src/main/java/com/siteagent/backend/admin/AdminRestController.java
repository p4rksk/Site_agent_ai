package com.siteagent.backend.admin;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.siteagent.backend.admin.request.AdminSignupRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminRestController {

    
    private final AdminService adminService;

    @PostMapping("/signup")
    public ResponseEntity<?> singnup(@RequestBody AdminSignupRequest request) {
        adminService.signUp(request);
        

        return ResponseEntity.ok("회원가입이 완료 되었습니다.");
    }

    //아이디 중복체크
    @GetMapping("/check-id")
    public ResponseEntity<?> checkId(@RequestParam String loginId) {
        boolean isDuplicate = adminService.checkDuplicateAdminId(loginId);
        return ResponseEntity.ok(isDuplicate);
    }

    @GetMapping("/check-site-id")
    public ResponseEntity<?> checkSiteId(@RequestParam String loginId) {
        boolean isDuplicate = adminService.checkDuplicateAdminId(loginId);
        return ResponseEntity.ok(isDuplicate);
    }
    
    

}