package co.edu.itm.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMappingRequest(
        @NotBlank String erpName,
        @NotBlank String sourceField,
        @NotBlank String targetField,
        String transformFn,
        @NotBlank String status
) {
}
