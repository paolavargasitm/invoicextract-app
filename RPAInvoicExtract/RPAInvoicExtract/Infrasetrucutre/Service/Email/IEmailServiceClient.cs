using RPAInvoicExtract.Infrasetrucutre.DTOs;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.Service.Email
{
    public interface IEmailServiceClient
    {
        Task<List<EmailDTO>> GetEmailsAsync();
    }
}
