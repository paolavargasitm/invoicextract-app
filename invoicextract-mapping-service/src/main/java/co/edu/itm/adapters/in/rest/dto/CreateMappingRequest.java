package co.edu.itm.adapters.in.rest.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record CreateMappingRequest(
  @NotBlank String erpName,
  @NotBlank String sourceField,
  @NotBlank String targetField,
  String transformFn,
  @NotBlank String status
){}
