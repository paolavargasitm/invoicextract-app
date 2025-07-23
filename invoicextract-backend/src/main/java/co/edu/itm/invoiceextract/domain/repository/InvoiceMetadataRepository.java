package co.edu.itm.invoiceextract.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.itm.invoiceextract.domain.entity.InvoiceMetadata;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;

public interface InvoiceMetadataRepository extends JpaRepository<InvoiceMetadata, Long> {
    
    Optional<InvoiceMetadata> findByInvoiceId(Long invoiceId);
    
    Optional<InvoiceMetadata> findByInvoiceNumber(String invoiceNumber);
    
    List<InvoiceMetadata> findByCustomerNameContainingIgnoreCase(String customerName);
    
    List<InvoiceMetadata> findBySupplierNameContainingIgnoreCase(String supplierName);
    
    List<InvoiceMetadata> findByProcessingStatus(String processingStatus);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
    
    @Query("SELECT m FROM InvoiceMetadata m WHERE m.customerName LIKE %:customerName% AND m.supplierName LIKE %:supplierName%")
    List<InvoiceMetadata> findByCustomerNameAndSupplierName(@Param("customerName") String customerName, @Param("supplierName") String supplierName);
    
    @Query("SELECT m FROM InvoiceMetadata m WHERE m.totalAmount >= :minAmount AND m.totalAmount <= :maxAmount")
    List<InvoiceMetadata> findByTotalAmountBetween(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT m FROM InvoiceMetadata m WHERE m.issueDate >= :startDate AND m.issueDate <= :endDate")
    List<InvoiceMetadata> findByIssueDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT m FROM InvoiceMetadata m WHERE m.dueDate >= :startDate AND m.dueDate <= :endDate")
    List<InvoiceMetadata> findByDueDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT m FROM InvoiceMetadata m WHERE m.currency = :currency")
    List<InvoiceMetadata> findByCurrency(@Param("currency") String currency);
    
    @Query("SELECT m FROM InvoiceMetadata m WHERE m.confidenceScore >= :minScore")
    List<InvoiceMetadata> findByConfidenceScoreGreaterThanEqual(@Param("minScore") BigDecimal minScore);

    @Query("SELECT SUM(m.totalAmount) FROM InvoiceMetadata m")
    Optional<BigDecimal> findTotalAmount();
}
