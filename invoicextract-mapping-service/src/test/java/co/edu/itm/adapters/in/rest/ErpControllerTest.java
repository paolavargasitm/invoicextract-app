package co.edu.itm.adapters.in.rest;

import co.edu.itm.adapters.in.rest.dto.CreateErpRequest;
import co.edu.itm.adapters.in.rest.dto.ErpResponse;
import co.edu.itm.adapters.out.jpa.mappings.entity.ErpEntity;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErpControllerTest {

    @Test
    void create_shouldSaveAndReturnResponse() {
        ErpJpaRepository repo = mock(ErpJpaRepository.class);
        ErpController controller = new ErpController(repo);
        when(repo.save(any(ErpEntity.class))).thenAnswer(inv -> {
            ErpEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        ResponseEntity<ErpResponse> resp = controller.create(new CreateErpRequest("SAP"));
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1L, resp.getBody().id());
        assertEquals("SAP", resp.getBody().name());
    }

    @Test
    void list_shouldMapEntitiesToResponses() {
        ErpJpaRepository repo = mock(ErpJpaRepository.class);
        Instant now = Instant.now();
        when(repo.findByStatusIgnoreCase("ACTIVE")).thenReturn(List.of(
                ErpEntity.builder().id(1L).name("SAP").status("ACTIVE").createdAt(now).updatedAt(now).build()
        ));
        ErpController controller = new ErpController(repo);

        List<ErpResponse> out = controller.list("ACTIVE");
        assertEquals(1, out.size());
        assertEquals("SAP", out.get(0).name());
    }

    @Test
    void changeStatus_shouldUpdateAndSave() {
        ErpJpaRepository repo = mock(ErpJpaRepository.class);
        ErpEntity e = ErpEntity.builder().id(1L).name("SAP").status("ACTIVE").build();
        when(repo.findById(1L)).thenReturn(Optional.of(e));
        ErpController controller = new ErpController(repo);

        ResponseEntity<Void> resp = controller.changeStatus(1L, "INACTIVE");
        assertEquals(204, resp.getStatusCode().value());
        assertEquals("INACTIVE", e.getStatus());
        verify(repo).save(e);
    }
}
