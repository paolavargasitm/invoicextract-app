package co.edu.itm.adapters.in.rest;

import co.edu.itm.adapters.in.rest.dto.CreateMappingRequest;
import co.edu.itm.adapters.in.rest.dto.MappingResponse;
import co.edu.itm.adapters.out.jpa.mappings.entity.ErpEntity;
import co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.repo.FieldMappingJpaRepository;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MappingControllerTest {

    @Test
    void list_shouldResolveErpAndMapResponses() {
        ErpJpaRepository erpRepo = mock(ErpJpaRepository.class);
        FieldMappingJpaRepository fmRepo = mock(FieldMappingJpaRepository.class);
        MappingRepositoryPort mappingPort = mock(MappingRepositoryPort.class);
        when(erpRepo.findByNameIgnoreCase("SAP")).thenReturn(Optional.of(ErpEntity.builder().id(1L).name("SAP").status("ACTIVE").build()));
        Instant now = Instant.now();
        when(fmRepo.findByErpIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(
                FieldMappingEntity.builder().id(10L).erpId(1L).sourceField("a").targetField("b").transformFn("TRIM").status("ACTIVE").version(1).createdAt(now).updatedAt(now).build()
        ));

        MappingController controller = new MappingController(erpRepo, fmRepo, mappingPort);
        List<MappingResponse> out = controller.list("SAP", "ACTIVE");
        assertEquals(1, out.size());
        assertEquals("b", out.get(0).targetField());

        verify(erpRepo).findByNameIgnoreCase("SAP");
        verify(fmRepo).findByErpIdAndStatus(1L, "ACTIVE");
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        ErpJpaRepository erpRepo = mock(ErpJpaRepository.class);
        FieldMappingJpaRepository fmRepo = mock(FieldMappingJpaRepository.class);
        MappingRepositoryPort mappingPort = mock(MappingRepositoryPort.class);
        when(erpRepo.findByNameIgnoreCase("SAP")).thenReturn(Optional.of(ErpEntity.builder().id(1L).name("SAP").status("ACTIVE").build()));
        when(fmRepo.save(any(FieldMappingEntity.class))).thenAnswer(inv -> {
            FieldMappingEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        MappingController controller = new MappingController(erpRepo, fmRepo, mappingPort);
        ResponseEntity<MappingResponse> resp = controller.create(new CreateMappingRequest("SAP", "a", "b", "TRIM", null));
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(10L, resp.getBody().id());
        assertEquals("b", resp.getBody().targetField());

        verify(fmRepo).save(any(FieldMappingEntity.class));
    }

    @Test
    void changeStatus_shouldPersist() {
        ErpJpaRepository erpRepo = mock(ErpJpaRepository.class);
        FieldMappingJpaRepository fmRepo = mock(FieldMappingJpaRepository.class);
        MappingRepositoryPort mappingPort = mock(MappingRepositoryPort.class);
        FieldMappingEntity existing = FieldMappingEntity.builder().id(10L).erpId(1L).sourceField("a").targetField("b").transformFn("TRIM").status("ACTIVE").version(1).build();
        when(fmRepo.findById(10L)).thenReturn(Optional.of(existing));

        MappingController controller = new MappingController(erpRepo, fmRepo, mappingPort);
        ResponseEntity<Void> resp = controller.changeStatus(10L, "INACTIVE");
        assertEquals(204, resp.getStatusCode().value());
        assertEquals("INACTIVE", existing.getStatus());
        verify(fmRepo).save(existing);
    }
}
