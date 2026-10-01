package com.ticdiaspora.auth.infrastructure;

import com.ticdiaspora.shared.domain.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AuthService {

    private final UserJpaRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long tokenValidityMinutes;

    public AuthService(
            UserJpaRepository users,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.security.issuer}") String issuer,
            @Value("${app.security.token-validity-minutes}") long tokenValidityMinutes
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.tokenValidityMinutes = tokenValidityMinutes;
    }

    @Transactional
    public AuthToken login(String email, String password) {
        UserEntity user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("BAD_CREDENTIALS", "Identifiants invalides"));
        if (!user.isEnabled() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException("BAD_CREDENTIALS", "Identifiants invalides");
        }
        Instant now = Instant.now();
        Instant expiresAt = now.plus(tokenValidityMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("memberId", user.getMemberId().toString())
                .claim("roles", List.of(user.getRole().name()))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        user.setLastLoginAt(now);
        return new AuthToken(token, "Bearer", expiresAt, user.getMemberId(), user.getRole().name(), user.getEmail());
    }

    public record AuthToken(String accessToken, String tokenType, Instant expiresAt, java.util.UUID memberId, String role, String email) {
    }
}
