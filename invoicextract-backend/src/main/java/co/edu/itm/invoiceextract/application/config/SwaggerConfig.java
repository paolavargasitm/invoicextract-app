package co.edu.itm.invoiceextract.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Invoice Extract Backend API")
                        .description("Complete CRUD API for Invoice Management System. " +
                                "This API provides endpoints for creating, reading, updating, and deleting invoices, " +
                                "along with advanced search and filtering capabilities.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Invoice Extract Team")
                                .email("support@invoiceextract.com")
                                .url("https://github.com/invoiceextract/backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.invoiceextract.com")
                                .description("Production Server")
                ));
    }
}
