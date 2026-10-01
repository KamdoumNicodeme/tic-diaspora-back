package com.ticdiaspora.infrastructure.config;

import com.ticdiaspora.domain.service.AbsenceRules;
import com.ticdiaspora.domain.service.AttendanceRules;
import com.ticdiaspora.domain.service.ChairpersonRotationPolicy;
import com.ticdiaspora.domain.service.PenaltyRules;
import com.ticdiaspora.domain.service.TontineRules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    AbsenceRules absenceRules() {
        return new AbsenceRules();
    }

    @Bean
    AttendanceRules attendanceRules() {
        return new AttendanceRules();
    }

    @Bean
    ChairpersonRotationPolicy chairpersonRotationPolicy() {
        return new ChairpersonRotationPolicy();
    }

    @Bean
    PenaltyRules penaltyRules() {
        return new PenaltyRules();
    }

    @Bean
    TontineRules tontineRules() {
        return new TontineRules();
    }
}
