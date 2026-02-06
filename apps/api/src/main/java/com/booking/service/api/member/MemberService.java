package com.booking.service.api.member;

import com.booking.service.domain.member.Member;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인메모리 회원 서비스.
 */
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member register(String email, String nickname, String rawPassword) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        if (nickname == null || nickname.isBlank()) throw new IllegalArgumentException("nickname is required");
        if (rawPassword == null || rawPassword.isBlank()) throw new IllegalArgumentException("password is required");

        if (memberRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("email already registered");
        }
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        Member member = new Member(email, nickname, hash, Member.Role.USER);
        return memberRepository.save(member);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public Member updateProfile(Long id, String nickname) {
        Member existing = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("member not found"));
        if (nickname != null && !nickname.isBlank()) {
            existing.setNickname(nickname);
        }
        return memberRepository.save(existing);
    }

    public boolean matchesPassword(Member member, String rawPassword) {
        return BCrypt.checkpw(rawPassword, member.getPasswordHash());
    }

    @Transactional
    public Member changeRole(Long memberId, Member.Role role) {
        Member existing = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("member not found"));
        existing.setRole(role);
        return memberRepository.save(existing);
    }
}
