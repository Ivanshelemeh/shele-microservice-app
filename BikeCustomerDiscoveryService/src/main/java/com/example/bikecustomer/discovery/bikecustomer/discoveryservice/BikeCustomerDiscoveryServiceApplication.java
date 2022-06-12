package com.example.bikecustomer.discovery.bikecustomer.discoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author shele
 * This is  a simple eurekaServer
 */
@SpringBootApplication
@EnableEurekaServer
public class BikeCustomerDiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BikeCustomerDiscoveryServiceApplication.class, args);
    }

}
