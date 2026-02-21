package com.booking.service.api.config;

import com.booking.service.api.auth.JwtService;
import com.booking.service.api.auth.TokenService;
import com.booking.service.api.member.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;
    private final MemberService memberService;
    private final TokenService tokenService;

    public SecurityConfig(JwtService jwtService, MemberService memberService, TokenService tokenService) {
        this.jwtService = jwtService;
        this.memberService = memberService;
        this.tokenService = tokenService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/refresh",
                                "/hello",
                                "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/accommodations", "/accommodations/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/hosts/apply").hasRole("USER")
                        .requestMatchers("/reservations").hasRole("ADMIN") // 전체 예약 목록
                        .requestMatchers("/reservations/**").hasAnyRole("USER", "ADMIN") // 생성/내역/취소/삭제
                        .requestMatchers(HttpMethod.POST, "/accommodations/**").hasRole("HOST")
                        .requestMatchers(HttpMethod.DELETE, "/accommodations/**").hasRole("HOST")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthFilter(jwtService, memberService, tokenService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
