package com.ticdiaspora.config;

import com.ticdiaspora.auth.infrastructure.UserEntity;
import com.ticdiaspora.auth.infrastructure.UserJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberEntity;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.shared.domain.enums.ApplicationRole;
import com.ticdiaspora.shared.domain.enums.ContributionType;
import com.ticdiaspora.shared.domain.enums.MemberStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataBootstrap {

    @Bean
    ApplicationRunner seedDemoAccounts(
            MemberJpaRepository members,
            UserJpaRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.enabled}") boolean enabled,
            @Value("${app.bootstrap.president-email}") String presidentEmail,
            @Value("${app.bootstrap.president-password}") String presidentPassword,
            @Value("${app.bootstrap.member-email}") String memberEmail,
            @Value("${app.bootstrap.member-password}") String memberPassword
    ) {
        return args -> {
            if (!enabled) {
                return;
            }
            createAccountIfMissing(members, users, passwordEncoder, presidentEmail, presidentPassword,
                    "Président", "TIC Diaspora", ApplicationRole.PRESIDENT, ContributionType.XAF_100000);
            createAccountIfMissing(members, users, passwordEncoder, memberEmail, memberPassword,
                    "Membre", "Démo", ApplicationRole.MEMBER, ContributionType.XAF_50000);
        };
    }

    private void createAccountIfMissing(
            MemberJpaRepository members,
            UserJpaRepository users,
            PasswordEncoder passwordEncoder,
            String email,
            String password,
            String firstName,
            String lastName,
            ApplicationRole role,
            ContributionType contributionType
    ) {
        if (members.existsByEmailIgnoreCase(email)) {
            return;
        }
        MemberEntity member = new MemberEntity();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setPhone("+35200000000");
        member.setCountry("Luxembourg");
        member.setCity("Luxembourg");
        member.setFullAddress("Adresse à compléter");
        member.setJoinedAt(LocalDate.now());
        member.setStatus(MemberStatus.ACTIVE);
        member.setRole(role);
        member.setContributionType(contributionType);
        member = members.save(member);

        UserEntity user = new UserEntity();
        user.setMemberId(member.getId());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        users.save(user);
    }
}
