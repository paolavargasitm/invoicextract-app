package co.edu.itm.invoiceextract.domain.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private String customerName;
    private BigDecimal amount;
    private LocalDate issueDate;
    private String pdfUrl;
    private String status;

    // Getters and setters omitted for brevity
}
