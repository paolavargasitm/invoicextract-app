using Newtonsoft.Json;
using RPAInvoicExtract.Infrasetrucutre.DTOs;
using RPAInvoicExtract.Infrasetrucutre.Loggin;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.Service.Backend
{
    internal class BackendServiceClient
    {
        private readonly HttpClient _httpClient;
        private readonly IAppLogger _logger;
        private readonly string _apiUrl = ConfigurationManager.AppSettings["urlInvoice"];

        public BackendServiceClient(HttpClient httpClient, IAppLogger logger)
        {
            _httpClient = httpClient;
            _logger = logger;
        }

        /// <summary>
        /// Envía el InvoiceDTO al endpoint del servicio remoto.
        /// </summary>
        public async Task<bool> SendInvoiceAsync(InvoiceDTO invoice)
        {

           
            try
            {

                var tokenRequest = new HttpRequestMessage(HttpMethod.Post, ConfigurationManager.AppSettings["urlToken"])
                {
                    Content = new FormUrlEncodedContent(new Dictionary<string, string>
                {
                    { "grant_type", "client_credentials" },
                    { "client_id", ConfigurationManager.AppSettings["client_idServices"] },
                    { "client_secret", ConfigurationManager.AppSettings["client_secret"] }
                })
                };

                var tokenResponse = await _httpClient.SendAsync(tokenRequest);
                tokenResponse.EnsureSuccessStatusCode();
                var tokenJson = await tokenResponse.Content.ReadAsStringAsync();
                dynamic tokenData = JsonConvert.DeserializeObject(tokenJson);
                string token = tokenData.access_token ?? tokenData.token ?? tokenData["data"]?["token"];

                string jsonBody = JsonConvert.SerializeObject(invoice);
                var content = new StringContent(jsonBody, Encoding.UTF8, "application/json");

                _httpClient.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", token);

                var response = await _httpClient.PostAsync(_apiUrl, content);

                if (response.IsSuccessStatusCode)
                {
                    _logger.Info($"✅ Factura enviada correctamente al servicio: {invoice.DocumentNumber}");
                    return true;
                }
                else
                {
                    string error = await response.Content.ReadAsStringAsync();
                    _logger.Error($"❌ Error enviando factura: {response.StatusCode} → {error}");
                    return false;
                }
            }
            catch (Exception ex)
            {
                _logger.Error($"⚠️ Excepción al enviar la factura: {ex.Message}");
                return false;
            }
        }
    }
}
