package com.ticdiaspora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.ticdiaspora.application",
        "com.ticdiaspora.infrastructure",
        "com.ticdiaspora.bootstrap"
})
public class TicDiasporaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicDiasporaApplication.class, args);
    }
}
