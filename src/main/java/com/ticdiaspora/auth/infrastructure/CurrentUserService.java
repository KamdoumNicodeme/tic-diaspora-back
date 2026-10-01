package com.ticdiaspora.auth.infrastructure;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CurrentUserService {

    public Optional<UUID> memberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        String memberId = jwt.getClaimAsString("memberId");
        return memberId == null ? Optional.empty() : Optional.of(UUID.fromString(memberId));
    }

    public UUID memberIdOrSystem() {
        return memberId().orElse(null);
    }
}
