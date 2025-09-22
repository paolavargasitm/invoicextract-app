package co.edu.itm.infra.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class InvoicesDbConfigTest {

    @Test
    void createsDataSourceAndJdbcTemplate() {
        InvoicesDbConfig cfg = new InvoicesDbConfig();
        InvoicesDbProperties props = new InvoicesDbProperties();
        props.setUrl("jdbc:mysql://localhost:3306/dummy");
        props.setUsername("user");
        props.setPassword("pass");
        props.setDriverClassName("com.mysql.cj.jdbc.Driver");

        DataSource ds = cfg.invoicesDataSource(props);
        assertNotNull(ds);

        JdbcTemplate template = cfg.invoicesJdbcTemplate(ds);
        assertNotNull(template);
    }
}
