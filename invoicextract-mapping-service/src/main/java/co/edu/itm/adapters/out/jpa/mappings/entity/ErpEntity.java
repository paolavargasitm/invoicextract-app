package co.edu.itm.adapters.out.jpa.mappings.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="erps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true) private String name;
  @Column(nullable=false) private String status;
  @Column(name="created_at") private Instant createdAt;
  @Column(name="updated_at") private Instant updatedAt;
}
