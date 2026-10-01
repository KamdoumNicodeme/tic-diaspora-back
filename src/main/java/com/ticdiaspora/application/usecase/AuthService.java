package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.domain.model.User;
import com.ticdiaspora.application.port.out.UserRepositoryPort;
import com.ticdiaspora.domain.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import com.ticdiaspora.application.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@UseCase
public class AuthService {

    private final UserRepositoryPort users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long tokenValidityMinutes;

    public AuthService(
            UserRepositoryPort users,
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
        User user = users.findByEmailIgnoreCase(email)
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
        users.save(user);
        return new AuthToken(token, "Bearer", expiresAt, user.getMemberId(), user.getRole().name(), user.getEmail());
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("PASSWORD_CONFIRMATION_MISMATCH", "La confirmation du nouveau mot de passe ne correspond pas");
        }
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("BAD_CREDENTIALS", "Utilisateur introuvable"));
        if (!user.isEnabled() || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("BAD_CREDENTIALS", "Mot de passe actuel incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException("PASSWORD_UNCHANGED", "Le nouveau mot de passe doit être différent de l'ancien");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        users.save(user);
    }

    public record AuthToken(String accessToken, String tokenType, Instant expiresAt, java.util.UUID memberId, String role, String email) {
    }
}
