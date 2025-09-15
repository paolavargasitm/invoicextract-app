package co.edu.itm.adapters.in.rest.dto;
import jakarta.validation.constraints.NotBlank;
public record CreateErpRequest(@NotBlank String name) {}
