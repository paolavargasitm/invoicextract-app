using MailKit;
using MailKit.Net.Imap;
using MailKit.Search;
using MailKit.Security;
using MimeKit;
using Newtonsoft.Json;
using RPAInvoicExtract.Infrasetrucutre.DTOs;
using RPAInvoicExtract.Infrasetrucutre.Loggin;
using RPAInvoicExtract.Infrasetrucutre.Service.Backend;
using RPAInvoicExtract.Infrasetrucutre.Service.Document;
using RPAInvoicExtract.Infrasetrucutre.Service.Storage;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.Service.Email
{
    public class ImapClientService
    {
        private readonly IAppLogger _logger;
        private DocumentManagement documentManagement;

        private static readonly SemaphoreSlim _imapLock = new SemaphoreSlim(1, 1);

        public async Task ProcessMailboxAsync(EmailAccountConfig account)
        {
            await _imapLock.WaitAsync();
            try
            {
                await ProcessMailboxInternalAsync(account);
            }
            finally
            {
                _imapLock.Release();
            }
        }

        public ImapClientService(IAppLogger logger)
        {
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
            documentManagement = new DocumentManagement();
        }

        private async Task ProcessMailboxInternalAsync(EmailAccountConfig account)
        {
            string hostImap;
            int portImap;

            if (account.Provider.Equals("gmail", StringComparison.OrdinalIgnoreCase))
            {
                hostImap = "imap.gmail.com";
                portImap = 993;
            }
            else if (account.Provider.Equals("outlook", StringComparison.OrdinalIgnoreCase))
            {
                hostImap = "outlook.office365.com";
                portImap = 993;
            }
            else
            {
                throw new ArgumentException($"Proveedor no soportado: {account.Provider}");
            }

            int retryCount = 0;
            const int maxRetries = 3;

            while (retryCount < maxRetries)
            {
                using (var client = new ImapClient())
                {
                    client.ServerCertificateValidationCallback = (s, c, h, e) => true;
                    client.Timeout = 15000;

                    using (var cts = new CancellationTokenSource(TimeSpan.FromSeconds(20)))
                    {
                        try
                        {
                            _logger.Info($"🔌 Conectando a {hostImap}:{portImap} ({account.Email})...");
                            await client.ConnectAsync(hostImap, portImap, SecureSocketOptions.SslOnConnect, cts.Token);

                            _logger.Info($"🔑 Autenticando {account.Email}...");
                            await client.AuthenticateAsync(account.Email, account.Password, cts.Token);

                            var inbox = client.Inbox;
                            await inbox.OpenAsync(FolderAccess.ReadWrite, cts.Token);
                            _logger.Info($"📥 Procesando cuenta: {account.Email} ({account.Provider})");

                            var folderProcessed = EnsureFolder(client, inbox, "CorreosProcesados");
                            var folderError = EnsureFolder(client, inbox, "CorreosProcesadosError");
                            var folderNoAttach = EnsureFolder(client, inbox, "CorreosProcesadosSinAdjunto");

                            var unread = inbox.Search(SearchQuery.NotSeen, cts.Token);

                            if (!unread.Any())
                            {
                                _logger.Info($"✅ Sin correos pendientes en {account.Email}");
                            }
                            else
                            {
                                foreach (var uid in unread)
                                {
                                    try
                                    {
                                        var message = inbox.GetMessage(uid, cts.Token);
                                        await ProcessMessageAsync(message, uid, inbox, folderProcessed, folderNoAttach);
                                    }
                                    catch (Exception ex)
                                    {
                                        _logger.Error($"{account.Email} - Error procesando mensaje UID {uid}: {ex.Message}");
                                        try
                                        {
                                            inbox.MoveTo(uid, folderError, cts.Token);
                                        }
                                        catch (Exception moveEx)
                                        {
                                            _logger.Warn($"{account.Email} - No se pudo mover mensaje con error: {moveEx.Message}");
                                        }
                                    }
                                }
                            }

                            _logger.Info($"🟢 Finalizó el procesamiento de {account.Email}");
                            return;
                        }
                        catch (AuthenticationException ex)
                        {
                            _logger.Error($"🔒 Error de autenticación en {account.Email}: {ex.Message}");
                            _logger.Warn("Posible bloqueo temporal. Esperando 5 minutos antes de reintentar...");
                            retryCount++;
                            await Task.Delay(TimeSpan.FromMinutes(5));
                        }
                        catch (OperationCanceledException)
                        {
                            _logger.Warn($"⏱ Timeout en conexión o autenticación de {account.Email}. Reintentando en 2 minutos...");
                            retryCount++;
                            await Task.Delay(TimeSpan.FromMinutes(2));
                        }
                        catch (ServiceNotConnectedException ex)
                        {
                            _logger.Warn($"⚠️ Error IMAP {account.Email}: {ex.Message}. Esperando 1 minuto...");
                            retryCount++;
                            await Task.Delay(TimeSpan.FromMinutes(1));
                        }
                        catch (Exception ex)
                        {
                            _logger.Error($"{account.Email} - Error inesperado: {JsonConvert.SerializeObject(ex)}");
                            retryCount++;
                            await Task.Delay(TimeSpan.FromMinutes(1));
                        }
                        finally
                        {
                            try
                            {
                                if (client.IsConnected)
                                {
                                    await client.DisconnectAsync(true);
                                    _logger.Info($"🔚 Sesión IMAP cerrada correctamente para {account.Email}");
                                }
                            }
                            catch (Exception ex)
                            {
                                _logger.Warn($"⚠️ Error cerrando conexión IMAP: {ex.Message}");
                            }
                        }
                    }
                }
            }

            _logger.Error($"🚫 Fallaron los {maxRetries} intentos de conexión para {account.Email}. Se detiene el procesamiento temporalmente.");
        }



        private IMailFolder EnsureFolder(ImapClient client, IMailFolder inbox, string name)
        {
            try
            {
                return inbox.GetSubfolder(name);
            }
            catch
            {
                var folder = inbox.Create(name, true);
                _logger.Error($"📁 Carpeta '{name}' creada.");
                return folder;
            }
        }

        private Task ProcessMessageAsync(MimeMessage message, UniqueId uid, IMailFolder inbox, IMailFolder processed, IMailFolder noAttach)
        {
            if (!message.Attachments.Any())
            {
                inbox.AddFlags(uid, MessageFlags.Seen, false);
                inbox.MoveTo(uid, noAttach);
                return Task.CompletedTask;
            }

            // Aquí puedes seguir delegando a tu DocumentManager
            var path = documentManagement.CreateDownloadDrectory();
            foreach (var attachment in message.Attachments)
                documentManagement.DowloadAttachment(attachment, path);

            string pathXML = documentManagement.GetPathXML(path);
            List<string> filesUpload = documentManagement.GetFilesUpload(path);

            if (!string.IsNullOrEmpty(pathXML))
            {
                _logger.Info($"📄 Procesando documento electrónico en '{pathXML}'...");
                InvoiceDTO electronicDocument = documentManagement.GetElectronicDocument(pathXML);

                _logger.Info($"📄 Documento cargado para receptor NIT: '{electronicDocument.ReceiverTaxId}'.");
                var s3Service = new S3Services(_logger);
                foreach (var file in filesUpload)
                {
                    if (Path.GetExtension(file).Equals(".xml", StringComparison.OrdinalIgnoreCase))
                    {
                        string s3KeyXML = $"invoices/{electronicDocument.ReceiverTaxId}/{Path.GetFileName(file)}";
                        var urlXML = s3Service.UploadFileAsync(file, s3KeyXML).GetAwaiter().GetResult();
                        electronicDocument.InvoicePathXML = urlXML;
                    }
                    else if (Path.GetExtension(file).Equals(".pdf", StringComparison.OrdinalIgnoreCase))
                    {
                        string s3KeyPDF = $"invoices/{electronicDocument.ReceiverTaxId}/{Path.GetFileName(file)}";
                        var urlPDF = s3Service.UploadFileAsync(file, s3KeyPDF).GetAwaiter().GetResult();
                        electronicDocument.InvoicePathPDF = urlPDF;
                    }
                }

                if (electronicDocument.DocumentType == "01"
                || electronicDocument.DocumentType == "02"
                || electronicDocument.DocumentType == "03"
                || electronicDocument.DocumentType == "04")
                {
                    electronicDocument.DocumentType = "FACTURA";
                }
                else if (electronicDocument.DocumentType == "91")
                {
                    electronicDocument.DocumentType = "NOTA CREDITO";
                }
                else if (electronicDocument.DocumentType == "92")
                {
                    electronicDocument.DocumentType = "NOTA DEBITO";
                }

                

                var backendService = new BackendServiceClient(new System.Net.Http.HttpClient(), _logger);

                var sendResult = backendService.SendInvoiceAsync(electronicDocument).GetAwaiter().GetResult();

                if (sendResult)
                {
                    _logger.Info($"📄 Documento enviado correctamente al servicio para NIT: '{electronicDocument.ReceiverTaxId}'.");
                }
                else
                {
                    _logger.Error($"📄 Error enviando documento al servicio para NIT: '{electronicDocument.ReceiverTaxId}'.");
                }
                //documentManagement.DeleteDirectory(path);
            }


            _logger.Info($"📄 Procesado correo '{message.Subject}' con {message.Attachments.Count()} adjuntos.");

            inbox.AddFlags(uid, MessageFlags.Seen, true);
            inbox.MoveTo(uid, processed);
            return Task.CompletedTask;
        }
    }
}
