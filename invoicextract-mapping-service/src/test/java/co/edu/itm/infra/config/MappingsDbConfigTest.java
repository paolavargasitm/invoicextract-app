package co.edu.itm.infra.config;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class MappingsDbConfigTest {

    @Test
    void createsPrimaryMappingsDataSourceBean() {
        MappingsDbConfig cfg = new MappingsDbConfig();
        DataSource ds = cfg.mappingsDataSource();
        assertNotNull(ds);
    }
}
