package com.booking.service.api.config;

import com.booking.service.api.auth.JwtService;
import com.booking.service.api.member.MemberService;
import com.booking.service.domain.member.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 단순 JWT 인증 필터. Bearer 토큰을 파싱하여 SecurityContext에 인증 정보를 넣는다.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MemberService memberService;

    public JwtAuthFilter(JwtService jwtService, MemberService memberService) {
        this.jwtService = jwtService;
        this.memberService = memberService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring("Bearer ".length());
            if (jwtService.isValid(token)) {
                Long memberId = jwtService.parseMemberId(token);
                memberService.findById(memberId).ifPresent(member -> setAuthentication(member));
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(Member member) {
        var auth = new UsernamePasswordAuthenticationToken(
                member, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
