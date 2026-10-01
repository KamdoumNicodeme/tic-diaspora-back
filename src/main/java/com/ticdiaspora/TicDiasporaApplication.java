package com.ticdiaspora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.ticdiaspora.application",
        "com.ticdiaspora.infrastructure"
})
@EntityScan(basePackages = "com.ticdiaspora.infrastructure.adapter.out.persistence.entity")
@EnableJpaRepositories(basePackages = "com.ticdiaspora.infrastructure.adapter.out.persistence.repository")
@EnableScheduling
public class TicDiasporaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicDiasporaApplication.class, args);
    }
}
