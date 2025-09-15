package co.edu.itm.adapters.out.jpa.mappings.mapper;
import org.mapstruct.Mapper;
import co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity;
import co.edu.itm.domain.model.FieldMapping;
@Mapper(componentModel = "spring")
public interface MappingEntityMapper {
  FieldMapping toDomain(FieldMappingEntity e);
}
