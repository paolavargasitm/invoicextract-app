package co.edu.itm.infra.diagnostics;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DataSourceLoggerTest {

    @Test
    void logDatasource_withHikari_shouldNotThrow() {
        HikariDataSource hikari = new HikariDataSource();
        // No configuration needed for coverage; getters can return null
        DataSourceLogger logger = new DataSourceLogger(hikari);
        assertDoesNotThrow(logger::logDatasource);
    }

    @Test
    void logDatasource_withGenericDataSource_shouldNotThrow() {
        DataSource generic = new DataSource() {
            @Override public Connection getConnection() throws SQLException { throw new UnsupportedOperationException(); }
            @Override public Connection getConnection(String username, String password) throws SQLException { throw new UnsupportedOperationException(); }
            @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
            @Override public PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(PrintWriter out) { }
            @Override public void setLoginTimeout(int seconds) { }
            @Override public int getLoginTimeout() { return 0; }
            @Override public Logger getParentLogger() { return Logger.getGlobal(); }
        };
        DataSourceLogger logger = new DataSourceLogger(generic);
        assertDoesNotThrow(logger::logDatasource);
    }
}
