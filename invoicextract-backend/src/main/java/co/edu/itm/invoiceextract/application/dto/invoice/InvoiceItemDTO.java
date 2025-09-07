package co.edu.itm.invoiceextract.application.dto.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "Item line for Invoice payload")
public class InvoiceItemDTO {

    @JsonProperty("ItemCode")
    @Schema(example = "P001")
    private String itemCode;

    @JsonProperty("Description")
    @Schema(example = "HP Laptop 15\"")
    private String description;

    @JsonProperty("Quantity")
    private Integer quantity;

    @JsonProperty("Unit")
    private String unit;

    @JsonProperty("UnitPrice")
    private BigDecimal unitPrice;

    @JsonProperty("Subtotal")
    private BigDecimal subtotal;

    @JsonProperty("TaxAmount")
    private BigDecimal taxAmount;

    @JsonProperty("Total")
    private BigDecimal total;
}
