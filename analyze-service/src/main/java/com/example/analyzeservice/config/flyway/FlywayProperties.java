package com.example.analyzeservice.config.flyway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "flyway.clickhouse")
@Getter
@Setter
public class FlywayProperties {

    private boolean validateOnMigrate = true;
    private boolean cleanDisabled = false;

}
