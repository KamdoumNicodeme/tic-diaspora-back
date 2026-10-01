package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.ApplicationRole;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private UUID memberId;
    private String email;
    private String passwordHash;
    private ApplicationRole role;
    @Builder.Default
    private boolean enabled = true;
    private Instant lastLoginAt;
    private Instant createdAt;
}
