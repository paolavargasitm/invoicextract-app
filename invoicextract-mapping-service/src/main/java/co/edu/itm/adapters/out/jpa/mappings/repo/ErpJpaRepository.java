package co.edu.itm.adapters.out.jpa.mappings.repo;

import co.edu.itm.adapters.out.jpa.mappings.entity.ErpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ErpJpaRepository extends JpaRepository<ErpEntity, Long> {
    Optional<ErpEntity> findByNameIgnoreCase(String name);
    List<ErpEntity> findByStatus(String status);
    List<ErpEntity> findByStatusIgnoreCase(String status);
}
