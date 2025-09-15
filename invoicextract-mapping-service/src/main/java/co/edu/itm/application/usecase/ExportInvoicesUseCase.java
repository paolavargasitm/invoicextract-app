package co.edu.itm.application.usecase;
import co.edu.itm.domain.ports.InvoiceRepositoryPort;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import co.edu.itm.domain.service.DynamicMappingService;
import co.edu.itm.domain.model.Invoice;
import co.edu.itm.domain.model.FieldMapping;
import java.util.*;
import java.util.stream.Collectors;

public class ExportInvoicesUseCase {
  private final InvoiceRepositoryPort invoiceRepo;
  private final MappingRepositoryPort mappingRepo;
  private final DynamicMappingService mapper;
  public ExportInvoicesUseCase(InvoiceRepositoryPort i, MappingRepositoryPort m, DynamicMappingService d){
    this.invoiceRepo=i; this.mappingRepo=m; this.mapper=d;
  }
  public List<Map<String,Object>> exportMapped(String erpName){
    List<Invoice> invoices = invoiceRepo.findApproved();
    List<FieldMapping> rules = mappingRepo.findActiveByErpName(erpName);
    return invoices.stream().map(inv -> {
      Map<String,Object> src = new HashMap<>();
      src.put("invoiceId", inv.getInvoiceId());
      src.put("customerId", inv.getCustomerId());
      src.put("total", inv.getTotal());
      src.put("issue_date", inv.getIssueDate());
      src.put("currency", inv.getCurrency());
      return mapper.apply(rules, src);
    }).collect(Collectors.toList());
  }
}
