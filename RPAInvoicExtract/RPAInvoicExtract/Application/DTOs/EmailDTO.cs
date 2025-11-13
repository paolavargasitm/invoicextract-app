using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.DTOs
{
    public class EmailDTO
    {
        [JsonProperty("username")]
        public string AccountEmail { get; set; }

        [JsonProperty("password")]
        public string Password { get; set; }

        [JsonProperty("key")]
        public string EncryptionKey { get; set; }
    }
}
