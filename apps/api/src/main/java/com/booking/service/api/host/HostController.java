package com.booking.service.api.host;

import com.booking.service.api.common.ApiResponse;
import com.booking.service.api.member.MemberService;
import com.booking.service.domain.host.HostProfile;
import com.booking.service.domain.member.Member;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 호스트 전환/프로필 API.
 */
@RestController
@RequestMapping("/hosts")
public class HostController {

    private final HostProfileService hostProfileService;
    private final MemberService memberService;

    public HostController(HostProfileService hostProfileService, MemberService memberService) {
        this.hostProfileService = hostProfileService;
        this.memberService = memberService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<HostProfileResponse>> apply(@Valid @RequestBody ApplyRequest request,
                                                                  Authentication authentication) {
        Member me = requireMember(authentication);
        // 간단하게 즉시 승인 처리. 추후 심사 프로세스가 필요하면 Status.PENDING으로 저장하고 승인 API 추가.
        HostProfile profile = hostProfileService.createOrUpdate(me.getId(), request.businessNumber(), HostProfile.Status.APPROVED);
        Member updated = memberService.changeRole(me.getId(), Member.Role.HOST);
        HostProfileResponse response = HostProfileResponse.from(profile, updated.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ApiResponse<HostProfileResponse> myProfile(Authentication authentication) {
        Member me = requireMember(authentication);
        HostProfile profile = hostProfileService.findByMemberId(me.getId())
                .orElseThrow(() -> new IllegalArgumentException("host profile not found"));
        return ApiResponse.ok(HostProfileResponse.from(profile, me.getRole()));
    }

    private Member requireMember(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("unauthenticated");
        }
        return (Member) authentication.getPrincipal();
    }

    public record ApplyRequest(@NotBlank String businessNumber) {
    }

    public record HostProfileResponse(Long memberId, String businessNumber, String status, String role) {
        public static HostProfileResponse from(HostProfile profile, Member.Role role) {
            return new HostProfileResponse(profile.getMemberId(), profile.getBusinessNumber(), profile.getStatus().name(), role.name());
        }
    }
}
