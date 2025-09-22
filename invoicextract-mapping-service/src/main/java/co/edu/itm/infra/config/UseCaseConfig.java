package co.edu.itm.infra.config;

import co.edu.itm.application.usecase.ExportInvoicesUseCase;
import co.edu.itm.domain.ports.InvoiceRepositoryPort;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import co.edu.itm.domain.service.DynamicMappingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public ExportInvoicesUseCase exportInvoicesUseCase(InvoiceRepositoryPort i, MappingRepositoryPort m, DynamicMappingService d) {
        return new ExportInvoicesUseCase(i, m, d);
    }
}
