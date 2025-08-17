package co.edu.itm.invoiceextract.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    List<Invoice> findByType(InvoiceType type);
    
    @Query("SELECT i FROM Invoice i JOIN i.metadata m WHERE m.customerEmail = :email")
    List<Invoice> findByMetadataCustomerEmail(@Param("email") String email);
    
    @Query("SELECT i FROM Invoice i JOIN i.metadata m WHERE m.customerEmail LIKE %:email%")
    List<Invoice> findByMetadataCustomerEmailContainingIgnoreCase(@Param("email") String email);
    
    @Query("SELECT i FROM Invoice i JOIN i.metadata m WHERE m.customerEmail = :email AND i.status = :status")
    List<Invoice> findByMetadataCustomerEmailAndStatus(@Param("email") String email, @Param("status") InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i JOIN i.metadata m WHERE m.customerEmail = :email AND i.type = :type")
    List<Invoice> findByMetadataCustomerEmailAndType(@Param("email") String email, @Param("type") InvoiceType type);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.type = :type")
    List<Invoice> findByStatusAndType(@Param("status") InvoiceStatus status, @Param("type") InvoiceType type);
    
    @Query("SELECT i FROM Invoice i JOIN i.metadata m WHERE m.issueDate >= :startDate AND m.issueDate <= :endDate")
    List<Invoice> findByMetadataIssueDateBetween(@Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.createdDate >= :startDate AND i.createdDate <= :endDate")
    List<Invoice> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") InvoiceStatus status);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.type = :type")
    long countByType(@Param("type") InvoiceType type);
    
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.metadata WHERE i.id = :id")
    Optional<Invoice> findByIdWithMetadata(@Param("id") Long id);
    
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.metadata")
    List<Invoice> findAllWithMetadata();
}
