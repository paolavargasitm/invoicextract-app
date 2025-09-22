package co.edu.itm.infra.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoicesDbPropertiesTest {

    @Test
    void gettersAndSettersWork() {
        InvoicesDbProperties p = new InvoicesDbProperties();
        p.setUrl("jdbc:mysql://localhost:3306/test");
        p.setUsername("user");
        p.setPassword("pass");
        p.setDriverClassName("com.mysql.cj.jdbc.Driver");

        assertEquals("jdbc:mysql://localhost:3306/test", p.getUrl());
        assertEquals("user", p.getUsername());
        assertEquals("pass", p.getPassword());
        assertEquals("com.mysql.cj.jdbc.Driver", p.getDriverClassName());
    }
}
