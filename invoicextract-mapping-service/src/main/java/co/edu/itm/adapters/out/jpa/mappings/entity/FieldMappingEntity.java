package co.edu.itm.adapters.out.jpa.mappings.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "field_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "erp_id", nullable = false)
    private Long erpId;
    @Column(name = "source_field", nullable = false)
    private String sourceField;
    @Column(name = "target_field", nullable = false)
    private String targetField;
    @Column(name = "transform_fn")
    private String transformFn;
    @Column(nullable = false)
    private String status;
    @Column
    private Integer version;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
