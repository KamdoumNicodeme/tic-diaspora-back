package com.ticdiaspora.infrastructure.config;

import com.ticdiaspora.application.port.in.TontineCycleCommand;
import com.ticdiaspora.application.port.out.TontineCycleRepositoryPort;
import com.ticdiaspora.application.usecase.MeetingService;
import com.ticdiaspora.application.usecase.TontineCycleService;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.MemberEntity;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.MemberJpaRepository;
import com.ticdiaspora.domain.model.enums.ApplicationRole;
import com.ticdiaspora.domain.model.enums.ContributionType;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class DataBootstrap {

    @Bean
    ApplicationRunner seedDemoAccounts(
            MemberJpaRepository members,
            UserJpaRepository users,
            MeetingService meetingService,
            TontineCycleService tontineCycleService,
            TontineCycleRepositoryPort tontineCycles,
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
            seedMonthlyBeneficiaries(members, users, passwordEncoder, memberPassword);
            seedCurrentYearMeetingsAndCycles(members, meetingService, tontineCycleService, tontineCycles);
        };
    }

    private void seedMonthlyBeneficiaries(
            MemberJpaRepository members,
            UserJpaRepository users,
            PasswordEncoder passwordEncoder,
            String password
    ) {
        String[][] monthlyMembers = {
                {"01", "Janvier", "Beneficiaire", "XAF_100000"},
                {"02", "Fevrier", "Beneficiaire A", "XAF_50000"},
                {"03", "Mars", "Beneficiaire", "XAF_100000"},
                {"04", "Avril", "Beneficiaire A", "XAF_50000"},
                {"05", "Mai", "Beneficiaire", "XAF_100000"},
                {"06", "Juin", "Beneficiaire A", "XAF_50000"},
                {"07", "Juillet", "Beneficiaire", "XAF_100000"},
                {"08", "Aout", "Beneficiaire A", "XAF_50000"},
                {"09", "Septembre", "Beneficiaire", "XAF_100000"},
                {"10", "Octobre", "Beneficiaire A", "XAF_50000"},
                {"11", "Novembre", "Beneficiaire", "XAF_100000"},
                {"12", "Decembre", "Beneficiaire A", "XAF_50000"}
        };
        for (String[] monthlyMember : monthlyMembers) {
            createAccountIfMissing(
                    members,
                    users,
                    passwordEncoder,
                    "membre%s@ticdiaspora.org".formatted(monthlyMember[0]),
                    password,
                    monthlyMember[1],
                    monthlyMember[2],
                    ApplicationRole.MEMBER,
                    ContributionType.valueOf(monthlyMember[3])
            );
            if (ContributionType.valueOf(monthlyMember[3]) == ContributionType.XAF_50000) {
                createAccountIfMissing(
                        members,
                        users,
                        passwordEncoder,
                        "membre%sb@ticdiaspora.org".formatted(monthlyMember[0]),
                        password,
                        monthlyMember[1],
                        "Beneficiaire B",
                        ApplicationRole.MEMBER,
                        ContributionType.XAF_50000
                );
            }
        }
    }

    private void seedCurrentYearMeetingsAndCycles(
            MemberJpaRepository members,
            MeetingService meetingService,
            TontineCycleService tontineCycleService,
            TontineCycleRepositoryPort tontineCycles
    ) {
        int year = LocalDate.now().getYear();
        meetingService.generateYear(year, LocalTime.of(15, 0), LocalTime.of(17, 0), "https://meet.google.com/tic-diaspora-demo");
        for (int month = 1; month <= 12; month++) {
            if (tontineCycles.findByMonthAndYear(month, year).isPresent()) {
                continue;
            }
            MemberEntity beneficiary = members.findByEmailIgnoreCase("membre%02d@ticdiaspora.org".formatted(month))
                    .orElseThrow();
            MemberEntity secondaryBeneficiary = beneficiary.getContributionType() == ContributionType.XAF_50000
                    ? members.findByEmailIgnoreCase("membre%02db@ticdiaspora.org".formatted(month)).orElseThrow()
                    : null;
            tontineCycleService.create(new TontineCycleCommand(
                    month,
                    year,
                    beneficiary.getId(),
                    secondaryBeneficiary == null ? null : secondaryBeneficiary.getId(),
                    "Cycle de démonstration affecté au bénéficiaire du mois %02d/%d".formatted(month, year)
            ));
        }
        meetingService.completePastMeetings(LocalDate.now(), LocalTime.now());
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
