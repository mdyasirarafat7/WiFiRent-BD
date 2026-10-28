package com.wifirent.bd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WifirentBdApplication {

    public static void main(String[] args) {
        SpringApplication.run(WifirentBdApplication.class, args);
    }
}
