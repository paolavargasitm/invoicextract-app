using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.DTOs
{
    public class InvoiceItemDTO
    {
        /// <summary>
        /// Internal or external product/service code.
        /// Example: "P001" or "SERV-1001".
        /// </summary>
        public string ItemCode { get; set; }

        /// <summary>
        /// Description of the product or service.
        /// Example: "HP Laptop 15"" or "Consulting Services".
        /// </summary>
        public string Description { get; set; }

        /// <summary>
        /// Quantity of items purchased.
        /// Example: 3
        /// </summary>
        public int Quantity { get; set; }

        /// <summary>
        /// Unit of measure of the item.
        /// Example: "UND", "KG", "HRS".
        /// </summary>
        public string Unit { get; set; }

        /// <summary>
        /// Price per unit without tax.
        /// Example: 2500.00
        /// </summary>
        public string UnitPrice { get; set; }

        /// <summary>
        /// Total value of the item before tax.
        /// Example: Quantity * UnitPrice.
        /// </summary>
        public string Subtotal { get; set; }

        /// <summary>
        /// Tax value applied to this item.
        /// Example: 475.00
        /// </summary>
        public string TaxAmount { get; set; }

        /// <summary>
        /// Total amount of the item including tax.
        /// Example: 2975.00
        /// </summary>
        public string Total { get; set; }
    }

}
