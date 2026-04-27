package com.siteagent.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.siteagent.backend.common.security.JwtTokenProvider;
import com.siteagent.backend.site.SiteService;
import com.siteagent.backend.site.response.SiteListResponse;
import com.siteagent.backend.user.response.UserLoginResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SiteService siteService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/kakao-login")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String token = userService.kakaoLogin(code);
        Long userId = jwtTokenProvider.getId(token);

        System.out.println("🔍 token: " + token);
        System.out.println("🔍 userId: " + userId);

        UserLoginResponse userLoginResponse = new UserLoginResponse(token,"user", userId);
        System.out.println("response" + userLoginResponse);
        return ResponseEntity.ok(userLoginResponse);
    }
    

    @GetMapping("/sites")
    public ResponseEntity<List<SiteListResponse>> getPublicSiteList() {
    return ResponseEntity.ok(siteService.getPublicSiteList());
}
}