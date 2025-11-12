using Newtonsoft.Json;
using RPAInvoicExtract.Infrasetrucutre.DTOs;
using System.Configuration;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;


namespace RPAInvoicExtract.Infrasetrucutre.Service.Email
{
    public class EmailServiceClient : IEmailServiceClient
    {
        private readonly HttpClient _httpClient;

        public EmailServiceClient() : this(new HttpClient()) { }


        public EmailServiceClient(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }


        public async Task<List<EmailDTO>> GetEmailsAsync()
        {
            // App.config keys requeridas:
            // <add key="ApiBaseUrl" value="http://localhost:8080/invoicextract" />
            // <add key="urlToken" value="http://localhost:8080/invoicextract/oauth/token" /> (o el que aplique)
            // <add key="client_idServices" value="..." />
            // <add key="client_secretServices" value="..." />

            string urlEmails = ConfigurationManager.AppSettings["urlEmails"];
            string tokenUrl = ConfigurationManager.AppSettings["urlToken"];
            string clientId = ConfigurationManager.AppSettings["client_idServices"];
            string clientSecret = ConfigurationManager.AppSettings["client_secret"];

            // 1) Obtener token (client_credentials)
            var tokenRequest = new HttpRequestMessage(HttpMethod.Post, tokenUrl)
            {
                Content = new FormUrlEncodedContent(new Dictionary<string, string>
                {
                    { "grant_type", "client_credentials" },
                    { "client_id", clientId },
                    { "client_secret", clientSecret }
                })
            };

            var tokenResponse = await _httpClient.SendAsync(tokenRequest);
            tokenResponse.EnsureSuccessStatusCode();
            var tokenJson = await tokenResponse.Content.ReadAsStringAsync();
            dynamic tokenData = JsonConvert.DeserializeObject(tokenJson);
            string accessToken = tokenData.access_token ?? tokenData.token ?? tokenData["data"]?["token"];

            // 2) Consumir endpoint protegido
            var request = new HttpRequestMessage(HttpMethod.Get, urlEmails);
            request.Headers.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", accessToken);

            var response = await _httpClient.SendAsync(request);
            if (response.StatusCode == System.Net.HttpStatusCode.NotFound)
            {
                // No hay configuración activa
                return new List<EmailDTO>();
            }
            response.EnsureSuccessStatusCode();

            var raw = await response.Content.ReadAsStringAsync();
            dynamic data = JsonConvert.DeserializeObject(raw);

            string encPassword = (string)data.password; // Base64 AES/ECB
            string encKey = (string)data.key;           // 32 ASCII chars (UTF-8 -> 32 bytes)
            string user = (string)data.username;

            // Desencripta con helper legacy (AES/ECB/PKCS7)
            string plainPassword = RPAInvoicExtract.Infrasetrucutre.Utils.EncryptionHelper
                .DecryptBase64(encPassword, encKey);

            var list = new List<EmailDTO>
            {
                new EmailDTO
                {
                    AccountEmail = user,
                    Password = plainPassword,
                    EncryptionKey = encKey
                }
            };

            return list;
        }

        
    }
}
