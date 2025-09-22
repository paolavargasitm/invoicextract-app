package co.edu.itm.infra.config;

import co.edu.itm.application.usecase.ExportInvoicesUseCase;
import co.edu.itm.domain.ports.InvoiceRepositoryPort;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import co.edu.itm.domain.service.DynamicMappingService;
import co.edu.itm.domain.service.TransformRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UseCaseConfigTest {

    @Test
    void createsExportInvoicesUseCaseBean() {
        UseCaseConfig cfg = new UseCaseConfig();
        InvoiceRepositoryPort invoiceRepo = mock(InvoiceRepositoryPort.class);
        MappingRepositoryPort mappingRepo = mock(MappingRepositoryPort.class);
        DynamicMappingService mappingService = new DynamicMappingService(new TransformRegistry());

        ExportInvoicesUseCase bean = cfg.exportInvoicesUseCase(invoiceRepo, mappingRepo, mappingService);
        assertNotNull(bean);
    }
}
