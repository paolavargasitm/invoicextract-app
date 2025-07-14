package co.edu.itm.invoiceextract.application.controller;


import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import co.edu.itm.invoiceextract.domain.entity.Invoice;
import co.edu.itm.invoiceextract.application.service.InvoiceService;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {
    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Invoice> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Invoice> getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Invoice create(@RequestBody Invoice invoice) {
        return service.save(invoice);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
