package com.booking.service.api.member;

import com.booking.service.domain.member.Member;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * 인메모리 회원 서비스.
 */
@Service
public class MemberService {

    private final Map<Long, Member> members = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public Member register(String email, String nickname, String rawPassword) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        if (nickname == null || nickname.isBlank()) throw new IllegalArgumentException("nickname is required");
        if (rawPassword == null || rawPassword.isBlank()) throw new IllegalArgumentException("password is required");

        if (findByEmail(email).isPresent()) {
            throw new IllegalStateException("email already registered");
        }
        Long id = seq.getAndIncrement();
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        Member member = new Member(id, email, nickname, hash);
        members.put(id, member);
        return member;
    }

    public Optional<Member> findByEmail(String email) {
        return members.values().stream()
                .filter(m -> m.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(members.get(id));
    }

    public Member updateProfile(Long id, String nickname) {
        Member existing = members.get(id);
        if (existing == null) throw new IllegalArgumentException("member not found");
        String newNickname = nickname == null || nickname.isBlank() ? existing.getNickname() : nickname;
        Member updated = new Member(existing.getId(), existing.getEmail(), newNickname, existing.getPasswordHash());
        members.put(id, updated);
        return updated;
    }

    public boolean matchesPassword(Member member, String rawPassword) {
        return BCrypt.checkpw(rawPassword, member.getPasswordHash());
    }
}
