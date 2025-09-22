package co.edu.itm.infra.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class InvoicesDbConfig {

    @Bean
    @ConfigurationProperties(prefix = "invoicesdb")
    public InvoicesDbProperties invoicesDbProperties() {
        return new InvoicesDbProperties();
    }

    @Bean(name = "invoicesDataSource")
    public DataSource invoicesDataSource(@Qualifier("invoicesDbProperties") InvoicesDbProperties p) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(p.getUrl());
        ds.setUsername(p.getUsername());
        ds.setPassword(p.getPassword());
        if (p.getDriverClassName() != null && !p.getDriverClassName().isBlank()) {
            ds.setDriverClassName(p.getDriverClassName());
        }
        ds.setReadOnly(true);
        ds.setPoolName("invoices-readonly-pool");
        return ds;
    }

    @Bean(name = "invoicesJdbcTemplate")
    public JdbcTemplate invoicesJdbcTemplate(@Qualifier("invoicesDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}

