package co.edu.itm.adapters.out.jpa.mappings.mapper;

import co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity;
import co.edu.itm.domain.model.FieldMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MappingEntityMapper {
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "erpId", source = "erpId"),
            @Mapping(target = "sourceField", source = "sourceField"),
            @Mapping(target = "targetField", source = "targetField"),
            @Mapping(target = "transformFn", source = "transformFn"),
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "version", source = "version"),
            @Mapping(target = "createdAt", source = "createdAt"),
            @Mapping(target = "updatedAt", source = "updatedAt")
    })
    FieldMapping toDomain(FieldMappingEntity e);
}
