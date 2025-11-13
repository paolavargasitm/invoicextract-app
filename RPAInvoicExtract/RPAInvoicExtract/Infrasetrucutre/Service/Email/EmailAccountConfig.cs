using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.Service.Email
{
    public class EmailAccountConfig
    {
        public string Email { get; set; }
        public string Password { get; set; }
        public string Provider { get; set; } = "outlook";
        public bool Enabled { get; set; } = true;
    }
}
