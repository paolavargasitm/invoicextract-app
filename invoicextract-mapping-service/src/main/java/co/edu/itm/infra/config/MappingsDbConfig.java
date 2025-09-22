package co.edu.itm.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class MappingsDbConfig {

    // Primary DataSource for JPA (mappings schema)
    @Bean(name = "mappingsDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mappingsDataSource() {
        return DataSourceBuilder.create().build();
    }
}
