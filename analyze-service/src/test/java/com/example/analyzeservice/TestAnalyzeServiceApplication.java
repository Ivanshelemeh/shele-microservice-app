package com.example.analyzeservice;

import org.springframework.boot.SpringApplication;

public class TestAnalyzeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(AnalyzeServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
