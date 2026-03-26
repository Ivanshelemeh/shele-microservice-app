package com.example.analyzeservice.config.flyway;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    private final FlywayProperties flywayProperties;

    @Value("${spring.datasource.clickhouse.url}")
    private String clickUrl;
    @Value("${spring.datasource.clickhouse.username}")
    private String clickUser;
    @Value("${spring.datasource.clickhouse.password}")
    private String clickPassword;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return Flyway.configure()
                .dataSource(clickUrl,clickUser,clickPassword)
                .locations("classpath:db/migration/clickhouse")
                .table("flyway_schema_history")
                .executeInTransaction(false)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                // Валидация: не падать если миграция изменилась
                // (для dev-окружения, на prod — убрать)
                .validateOnMigrate(flywayProperties.isValidateOnMigrate())
                .cleanDisabled(flywayProperties.isCleanDisabled())
                // Кодировка
                .encoding("UTF-8")
                .load();

    }


}
