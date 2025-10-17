package com.dzarembo.bybitservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BybitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BybitServiceApplication.class, args);
    }

}
