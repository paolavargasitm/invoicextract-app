package co.edu.itm.invoiceextract.application.dto.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item line for Invoice V2 payload")
public class InvoiceItemV2DTO {

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

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
