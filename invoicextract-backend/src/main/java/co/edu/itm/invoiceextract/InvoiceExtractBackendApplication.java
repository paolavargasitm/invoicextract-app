package co.edu.itm.invoiceextract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)
public class InvoiceExtractBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceExtractBackendApplication.class, args);
    }
}
