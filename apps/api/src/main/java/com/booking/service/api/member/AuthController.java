package com.booking.service.api.member;

import com.booking.service.api.auth.JwtService;
import com.booking.service.api.auth.RefreshTokenStore;
import com.booking.service.api.common.ApiResponse;
import com.booking.service.api.member.dto.LoginRequest;
import com.booking.service.api.member.dto.MemberResponse;
import com.booking.service.api.member.dto.RefreshRequest;
import com.booking.service.api.member.dto.SignupRequest;
import com.booking.service.api.member.dto.TokenResponse;
import com.booking.service.api.member.dto.UpdateMemberRequest;
import com.booking.service.domain.member.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 가입/로그인/프로필 조회/수정 API (Spring Security 미사용, 인메모리 저장).
 */
@RestController
@RequestMapping
public class AuthController {

    private final MemberService memberService;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthController(MemberService memberService, JwtService jwtService, RefreshTokenStore refreshTokenStore) {
        this.memberService = memberService;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(@RequestBody SignupRequest request) {
        Member member = memberService.register(request.email(), request.nickname(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(MemberResponse.from(member)));
    }

    @PostMapping("/auth/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        Member member = memberService.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));
        if (!memberService.matchesPassword(member, request.password())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        String access = jwtService.generateAccessToken(member.getId());
        String refresh = jwtService.generateRefreshToken(member.getId());
        refreshTokenStore.save(refresh, member.getId(), jwtService.getExpiration(refresh));
        return ApiResponse.ok(new TokenResponse(access, refresh));
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken is required");
        }
        if (!jwtService.isValid(refreshToken)) {
            throw new IllegalArgumentException("invalid refresh token");
        }
        RefreshTokenStore.TokenMeta meta = refreshTokenStore.find(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("refresh token not recognized"));

        Long tokenSubject = jwtService.parseMemberId(refreshToken);
        if (!meta.memberId().equals(tokenSubject)) {
            throw new IllegalArgumentException("refresh token owner mismatch");
        }
        String newAccess = jwtService.generateAccessToken(meta.memberId());
        String newRefresh = jwtService.generateRefreshToken(meta.memberId());
        refreshTokenStore.replace(refreshToken, newRefresh, meta.memberId(), jwtService.getExpiration(newRefresh));
        return ApiResponse.ok(new TokenResponse(newAccess, newRefresh));
    }

    @GetMapping("/members/me")
    public ApiResponse<MemberResponse> me(@RequestHeader("Authorization") String authorization) {
        Long memberId = resolveMemberId(authorization);
        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));
        return ApiResponse.ok(MemberResponse.from(member));
    }

    @PutMapping("/members/me")
    public ApiResponse<MemberResponse> update(@RequestHeader("Authorization") String authorization,
                                              @RequestBody UpdateMemberRequest request) {
        Long memberId = resolveMemberId(authorization);
        Member updated = memberService.updateProfile(memberId, request.nickname());
        return ApiResponse.ok(MemberResponse.from(updated));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    private Long resolveMemberId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header missing or invalid");
        }
        String token = authorizationHeader.substring("Bearer ".length());
        if (!jwtService.isValid(token)) {
            throw new IllegalArgumentException("invalid access token");
        }
        return jwtService.parseMemberId(token);
    }
}
