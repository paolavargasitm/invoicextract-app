package co.edu.itm.invoiceextract.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import co.edu.itm.invoiceextract.domain.entity.Invoice;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByEmail(String email);
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    List<Invoice> findByType(InvoiceType type);
    
    List<Invoice> findByEmailContainingIgnoreCase(String email);
    
    @Query("SELECT i FROM Invoice i WHERE i.email = :email AND i.status = :status")
    List<Invoice> findByEmailAndStatus(@Param("email") String email, @Param("status") InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.email = :email AND i.type = :type")
    List<Invoice> findByEmailAndType(@Param("email") String email, @Param("type") InvoiceType type);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.type = :type")
    List<Invoice> findByStatusAndType(@Param("status") InvoiceStatus status, @Param("type") InvoiceType type);
    
    @Query("SELECT i FROM Invoice i WHERE i.date >= :startDate AND i.date <= :endDate")
    List<Invoice> findByDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.createdAt >= :startDate AND i.createdAt <= :endDate")
    List<Invoice> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") InvoiceStatus status);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.type = :type")
    long countByType(@Param("type") InvoiceType type);
    
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.metadata WHERE i.id = :id")
    Optional<Invoice> findByIdWithMetadata(@Param("id") Long id);
    
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.metadata")
    List<Invoice> findAllWithMetadata();
}
