package co.edu.itm.domain.model;
import lombok.*;
import java.time.Instant;
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class Erp {
  private Long id;
  private String name;
  private String status; // ACTIVE | INACTIVE
  private Instant createdAt;
  private Instant updatedAt;
}
