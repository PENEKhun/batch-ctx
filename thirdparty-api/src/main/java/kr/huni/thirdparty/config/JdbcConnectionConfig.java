package kr.huni.thirdparty.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JdbcConnectionConfig {

    @Bean
    @Primary
    public JdbcConnectionDetails jdbcConnectionDetails(DataSourceProperties properties) {
        return new JdbcConnectionDetails() {
            @Override
            public String getJdbcUrl() {
                return properties.determineUrl();
            }

            @Override
            public String getUsername() {
                return properties.determineUsername();
            }

            @Override
            public String getPassword() {
                return properties.determinePassword();
            }
        };
    }
}
