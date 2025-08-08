package co.edu.itm.invoiceextract.infrastructure.errors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingErrorLogRepository extends JpaRepository<ProcessingErrorLog, Long> {
}
