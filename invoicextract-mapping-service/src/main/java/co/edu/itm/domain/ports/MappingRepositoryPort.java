package co.edu.itm.domain.ports;

import co.edu.itm.domain.model.FieldMapping;

import java.util.List;

public interface MappingRepositoryPort {
    List<FieldMapping> findActiveByErpName(String erpName);

    boolean existsActiveByErpAndSource(Long erpId, String sourceField);

    void invalidateCacheForErp(String erpName);
}
