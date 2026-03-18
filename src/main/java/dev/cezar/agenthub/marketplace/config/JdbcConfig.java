package dev.cezar.agenthub.marketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Provides a JDBC {@link DataSource} bean for use by Flyway and
 * {@code FlywaySchemasMigrationApplier}.
 *
 * <p>Spring Boot's {@code DataSourceAutoConfiguration} is skipped when an R2DBC
 * {@code ConnectionFactory} bean is present (conditional on its absence). In reactive
 * services that need both R2DBC (for the application) and JDBC (for Flyway), the
 * DataSource must be defined explicitly.
 *
 * @since 1.0.0
 */
@Configuration
public class JdbcConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}
