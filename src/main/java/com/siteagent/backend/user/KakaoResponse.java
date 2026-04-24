package com.siteagent.backend.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

public class KakaoResponse {

    // 카카오 토큰 응답 DTO
    @Getter
    @ToString
    public static class TokenDTO {
        @JsonProperty("access_token")
        private String accessToken;
    }

    // 카카오 유저 정보 응답 DTO
    @Getter
    @ToString
    public static class KakaoUserDTO {
        private Long id;  // 카카오 고유 PK

        @JsonProperty("kakao_account")
        private KakaoAccount kakaoAccount;

        @Getter
        public static class KakaoAccount {
            private Profile profile;

            @Getter
            public static class Profile {
                private String nickname;

                @JsonProperty("profile_image_url")
                private String profileImageUrl;
            }
        }
    }
} 
    

