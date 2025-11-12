using System.Diagnostics;


namespace RPAInvoicExtract.Infrasetrucutre.Loggin
{
    public class ActivityLogger : IAppLogger
    {
        private readonly string _source;
        private readonly string _log;

        public  ActivityLogger(string source = "RPAInvoicExtract", string log = "Application")
        {
            _source = source;
            _log = log;

            // Asegurar que exista el Source (requiere permisos de admin la primera vez)
            if (!EventLog.SourceExists(_source))
            {
                EventLog.CreateEventSource(new EventSourceCreationData(_source, _log));
            }
        }

        public void Info(string message) => EventLog.WriteEntry(_source, message, EventLogEntryType.Information);
        public void Warn(string message) => EventLog.WriteEntry(_source, message, EventLogEntryType.Warning);
        public void Error(string message) => EventLog.WriteEntry(_source, message, EventLogEntryType.Error);
    }
}
