package com.example.servicerating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ServiceRatingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRatingApplication.class, args);
    }

}
