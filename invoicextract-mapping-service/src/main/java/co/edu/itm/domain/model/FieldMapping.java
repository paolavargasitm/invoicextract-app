package co.edu.itm.domain.model;
import lombok.*;
import java.time.Instant;
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class FieldMapping {
  private Long id;
  private Long erpId;
  private String sourceField;
  private String targetField;
  private String transformFn; // TRIM, UPPER, DATE_FMT:yyyy-MM-dd
  private String status; // ACTIVE | INACTIVE
  private Integer version;
  private Instant createdAt;
  private Instant updatedAt;
}
