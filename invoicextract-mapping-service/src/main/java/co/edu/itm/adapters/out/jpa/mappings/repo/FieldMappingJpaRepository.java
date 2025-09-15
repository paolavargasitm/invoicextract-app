package co.edu.itm.adapters.out.jpa.mappings.repo;
import co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FieldMappingJpaRepository extends JpaRepository<FieldMappingEntity, Long> {
  List<FieldMappingEntity> findByErpIdAndStatus(Long erpId, String status);
  boolean existsByErpIdAndSourceFieldIgnoreCase(Long erpId, String sourceField);
}
