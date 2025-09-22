package co.edu.itm.infra.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceLogger {
    private static final Logger log = LoggerFactory.getLogger(DataSourceLogger.class);
    private final DataSource dataSource;

    public DataSourceLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void logDatasource() {
        if (dataSource instanceof HikariDataSource hikari) {
            log.info("Primary DataSource URL: {} | user: {} | pool: {}", hikari.getJdbcUrl(), hikari.getUsername(), hikari.getPoolName());
        } else {
            log.info("Primary DataSource class: {}", dataSource.getClass().getName());
        }
    }
}
