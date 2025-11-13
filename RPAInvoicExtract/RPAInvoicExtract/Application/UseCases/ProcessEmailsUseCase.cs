using RPAInvoicExtract.Infrasetrucutre.Loggin;
using RPAInvoicExtract.Infrasetrucutre.Service.Email;
using RPAInvoicExtract.Infrasetrucutre.Utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Web.Configuration;

namespace RPAInvoicExtract.Application.UseCases
{
    internal class ProcessEmailsUseCase
    {
        private readonly IEmailServiceClient _emailServiceClient;
        private readonly ImapClientService _imapClientService;
        private readonly IAppLogger _logger;

        public ProcessEmailsUseCase(
            IEmailServiceClient emailServiceClient,
            ImapClientService imapClientService,
            IAppLogger logger)
        {
            _emailServiceClient = emailServiceClient ?? throw new ArgumentNullException(nameof(emailServiceClient));
            _imapClientService = imapClientService ?? throw new ArgumentNullException(nameof(imapClientService));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public async Task ExecuteAsyncEmails()
        {
            try
            {
                var emails = await _emailServiceClient.GetEmailsAsync();

                if (emails == null || !emails.Any())
                {
                    _logger.Error("No se encontraron correos.");
                    return;
                }

                foreach (var account in emails)
                {
                    _logger.Info($"📥 Procesando cuenta: {account.AccountEmail}");

                    // 🔐 Desencriptar la contraseña
                    var provider = account.AccountEmail.Contains("gmail") ? "gmail" : "outlook";

                    await _imapClientService.ProcessMailboxAsync(new EmailAccountConfig
                    {
                        Email = account.AccountEmail,
                        Password = account.Password,
                        Provider = provider
                    });
                }

                _logger.Info("✅ Procesamiento de correos completado.");
            }
            catch (Exception ex)
            {
                _logger.Error($"❌ Error al procesar los correos: {ex.Message}");
            }
        }
    }
}
