package co.edu.itm.domain.model;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class Invoice {
  private String invoiceId;
  private String customerId;
  private BigDecimal total;
  private LocalDate issueDate;
  private String currency;
  private String status; // APPROVED, etc.
}
