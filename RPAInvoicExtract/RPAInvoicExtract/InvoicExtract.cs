using RPAInvoicExtract.Infrasetrucutre.Service.Email;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Linq;
using System.ServiceProcess;
using System.Text;
using System.Threading.Tasks;
using RPAInvoicExtract.Application.UseCases;
using RPAInvoicExtract.Infrasetrucutre.Loggin;
using RPAInvoicExtract.Infrasetrucutre.Utils;
using System.Net.Http;

namespace RPAInvoicExtract
{
    partial class InvoicExtract : ServiceBase
    {
        bool flag = false;

        public InvoicExtract()
        {
            InitializeComponent();
        }

        protected override void OnStart(string[] args)
        {
            // TODO: agregar código aquí para iniciar el servicio.
            stLapso.Start();
        }

        protected override void OnStop()
        {
            // TODO: agregar código aquí para realizar cualquier anulación necesaria para detener el servicio.
            stLapso.Stop();
        }

        private void stLapso_Elased(object sender, System.Timers.ElapsedEventArgs e)
        {
            if (flag) return;

            flag = true;

            Task.Run(async () =>
            {
                try
                {
                    EventLog.WriteEntry("Iniciando procesamiento de correos electrónicos...", EventLogEntryType.Information);

                    var logger = new ActivityLogger();                 // tu logger
                    var emailServiceClient = new EmailServiceClient(); // obtiene token y lista de correos
                    var imapClientService = new ImapClientService(logger);   // clase que maneja IMAP
                    

                    logger.Info("Iniciando procesamiento de correos electrónicos...");

                    var processEmailsUseCase = new ProcessEmailsUseCase(emailServiceClient, imapClientService, logger);

                    await processEmailsUseCase.ExecuteAsyncEmails();

                    logger.Info("Procesamiento de correos completado exitosamente.");
                }
                catch (Exception ex)
                {
                    EventLog.WriteEntry($"Error al procesar correos: {ex.Message}", EventLogEntryType.Error);
                }
                finally
                {
                    flag = false;
                }
            });


        }
    }
}
