package com.example.analyzeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnalyzeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzeServiceApplication.class, args);
    }

}
