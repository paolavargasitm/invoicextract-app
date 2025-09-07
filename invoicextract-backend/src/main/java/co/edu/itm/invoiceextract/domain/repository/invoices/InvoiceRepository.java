package co.edu.itm.invoiceextract.domain.repository.invoices;

import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByDocumentNumber(String documentNumber);
    
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(@Param("id") Long id);
    
    List<Invoice> findBySenderTaxId(String senderTaxId);
    
    List<Invoice> findByReceiverTaxId(String receiverTaxId);
    
    List<Invoice> findByDocumentType(String documentType);
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    List<Invoice> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<Invoice> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Invoice> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT i FROM Invoice i ORDER BY i.createdDate DESC")
    List<Invoice> findTop10ByOrderByCreatedDateDesc();
    
    long countByDocumentType(String documentType);
    
    @Query("SELECT SUM(i.amount) FROM Invoice i")
    BigDecimal sumAllAmounts();
}
