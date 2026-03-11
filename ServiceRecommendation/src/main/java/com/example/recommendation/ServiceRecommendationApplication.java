package com.example.recommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ServiceRecommendationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRecommendationApplication.class, args);
    }

}
