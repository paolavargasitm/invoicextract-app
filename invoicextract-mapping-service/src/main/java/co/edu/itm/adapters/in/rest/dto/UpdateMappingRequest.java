package co.edu.itm.adapters.in.rest.dto;

public record UpdateMappingRequest(
        String sourceField,
        String targetField,
        String transformFn,
        String status
) {
}
