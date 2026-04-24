package com.siteagent.backend.user;

import com.siteagent.backend.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    public String kakaoLogin(String code) {
        // 1. 인가코드 → 카카오 액세스 토큰
        KakaoResponse.TokenDTO tokenDTO = getKakaoToken(code);
        System.out.println("카카오 토큰: " + tokenDTO.toString());

        // 2. 액세스 토큰 → 유저 정보
        KakaoResponse.KakaoUserDTO kakaoUser = getKakaoUserInfo(tokenDTO.getAccessToken());
        System.out.println("카카오 유저: " + kakaoUser.toString());

        // 3. DB 저장 or 조회 → JWT 발급
        User user = saveOrFind(kakaoUser);
        return jwtTokenProvider.createToken(user.getId(), user.getKakaoId(), "USER");
    }

    private KakaoResponse.TokenDTO getKakaoToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        System.out.println("code: " + code);
        System.out.println("clientId: " + clientId);
        System.out.println("redirectUri: " + redirectUri);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret); 
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<KakaoResponse.TokenDTO> response = restTemplate.exchange(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            request,
            KakaoResponse.TokenDTO.class
        );

        return response.getBody();
    }

    private KakaoResponse.KakaoUserDTO getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoResponse.KakaoUserDTO> response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            request,
            KakaoResponse.KakaoUserDTO.class
        );

        return response.getBody();
    }

    private User saveOrFind(KakaoResponse.KakaoUserDTO kakaoUser) {
        String kakaoId = String.valueOf(kakaoUser.getId());
        String username = kakaoUser.getKakaoAccount().getProfile().getNickname();
        String profileImage = kakaoUser.getKakaoAccount().getProfile().getProfileImageUrl();

        return userRepository.findByKakaoId(kakaoId)
            .orElseGet(() -> {
                System.out.println("신규 유저 → 강제 회원가입");
                return userRepository.save(
                    User.builder()
                        .kakaoId(kakaoId)
                        .username(username)
                        .profileImage(profileImage)
                        .build()
                );
            });
    }
}