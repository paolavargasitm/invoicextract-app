using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.DTOs
{
    public class InvoiceDTO
    {
        public string DocumentType { get; set; }
        public string DocumentNumber { get; set; }
        public string ReceiverTaxId { get; set; }
        public string ReceiverTaxIdWithoutCheckDigit { get; set; }
        public string ReceiverBusinessName { get; set; }
        public string SenderTaxId { get; set; }
        public string SenderTaxIdWithoutCheckDigit { get; set; }
        public string SenderBusinessName { get; set; }
        public string RelatedDocumentNumber { get; set; }
        public string Amount { get; set; }
        public DateTime IssueDate { get; set; }
        public DateTime DueDate { get; set; }
        public string InvoicePathPDF { get; set; }
        public string InvoicePathXML { get; set; }
        public List <InvoiceItemDTO> InvoiceItems { get; set; }
    }
}
