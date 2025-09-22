package co.edu.itm.adapters.in.rest.dto;

public record MappingResponse(
        Long id, Long erpId, String sourceField, String targetField,
        String transformFn, String status, Integer version, String createdAt, String updatedAt
) {
}
