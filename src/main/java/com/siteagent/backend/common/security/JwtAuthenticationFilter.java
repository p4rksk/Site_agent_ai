package com.siteagent.backend.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.filter.OncePerRequestFilter;

import com.siteagent.backend.exception.CustomException;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {



        //  브라우저가 서버에게 요청 보내도 되는지 확인할 때 OPTIONS를 보냄 이게 CORS에 걸리다보니까 통과 시켜주기
        if (request.getMethod().equals("OPTIONS")) { 
            filterChain.doFilter(request, response);
            return;
        }

        // 임시 예외처리
        String uri = request.getRequestURI();
        if (uri.equals("/user/kakao-login")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = resolveToken(request);
        
        if (token == null) {
            throw new CustomException(401, "토큰이 없습니다.");
        }

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long adminId = jwtTokenProvider.getAdminId(token);
            String role = jwtTokenProvider.getRole(token);
            request.setAttribute("adminId", adminId);
            request.setAttribute("role", role);
        }

        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 토큰 꺼내기
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }

    
}