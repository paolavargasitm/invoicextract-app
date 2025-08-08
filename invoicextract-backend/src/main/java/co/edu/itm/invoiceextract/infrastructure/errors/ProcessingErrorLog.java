package co.edu.itm.invoiceextract.infrastructure.errors;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "processing_error_logs")
public class ProcessingErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "kafka_partition", nullable = false)
    private int kafkaPartition;

    @Column(name = "kafka_offset", nullable = false)
    private long kafkaOffset;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawMessage;

    @Column(nullable = false)
    private String errorType;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
