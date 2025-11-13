using Aspose.Zip;
using MimeKit;
using RPAInvoicExtract.Infrasetrucutre.DTOs;
using RPAInvoicExtract.Infrasetrucutre.Loggin;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web.Configuration;
using System.Xml;

namespace RPAInvoicExtract.Infrasetrucutre.Service.Document
{
    
    public class DocumentManagement
    {
        private readonly IAppLogger _logger;
        public string CreateDownloadDrectory()
        {
            string pathDowload = WebConfigurationManager.AppSettings["pathDowload"];
            //DeleteDirectory(pathDowload);
            if (!Directory.Exists(pathDowload))
                Directory.CreateDirectory(pathDowload);

            string pathDowloadDirectory = pathDowload + DateTime.Now.ToString("hhmmssffffff") + "\\";

            if (!Directory.Exists(pathDowloadDirectory))
                Directory.CreateDirectory(pathDowloadDirectory);

            return pathDowloadDirectory;
        }

        public void DeleteDirectory(string pathDelete)
        {
            if (Directory.Exists(pathDelete))
            {
                List<string> strDirectories = Directory.GetDirectories(pathDelete, "*", SearchOption.AllDirectories).ToList();
                if (strDirectories.Count > 0)
                {
                    foreach (string strDirectory in strDirectories)
                    {
                        if (Directory.Exists(strDirectory))
                        {
                            DateTime fechaCreacion = Directory.GetCreationTime(strDirectory);
                            DateTime ahora = DateTime.Now;
                            TimeSpan tiempoTranscurrido = ahora - fechaCreacion;
                            if (tiempoTranscurrido.TotalMinutes > 5)
                            {
                                DeleteFileAndDirectory(strDirectory);
                            }
                        }
                    }
                }
            }
        }
        public static void DeleteFileAndDirectory(string pathDelete)
        {
            int cont = 0;
            while (true)
            {
                try
                {
                    if (Directory.Exists(pathDelete))
                    {
                        List<string> strDirectories = Directory.GetDirectories(pathDelete, "*", SearchOption.AllDirectories).ToList();

                        if (strDirectories.Count > 0)
                        {
                            foreach (string directorio in strDirectories)
                            {
                                if (Directory.Exists(directorio))
                                {
                                    DeleteFileAndDirectory(directorio);
                                }
                            }
                        }

                        List<string> strFiles = Directory.GetFiles(pathDelete, "*", SearchOption.AllDirectories).ToList();
                        if (strFiles.Count > 0)
                        {
                            foreach (string fichero in strFiles)
                            {
                                if (File.Exists(fichero))
                                {
                                    File.Delete(fichero);
                                }
                            }
                        }
                        List<string> strDirectorie = Directory.GetDirectories(pathDelete, "*", SearchOption.AllDirectories).ToList();

                        if (strDirectories.Count > 0)
                        {
                            foreach (string directorio in strDirectories)
                            {
                                if (Directory.Exists(directorio))
                                {
                                    Directory.Delete(directorio);
                                }
                            }
                        }

                        if (Directory.Exists(pathDelete))
                        {
                            Directory.Delete(pathDelete, true);
                        }


                    }
                    break;
                }
                catch (IOException)
                {
                    if (cont < 3)
                    {
                        cont++;
                        // Si el archivo no está disponible, espera un momento antes de intentarlo de nuevo
                        System.Threading.Thread.Sleep(1000);
                    }
                    else
                    {
                        break;
                    }

                }

            }

        }
        public void DowloadAttachment(MimeKit.MimeEntity attachment, string pathDowloadDocument)
        {
            try
            {
                string pathExtration = "";
                if (attachment.ContentType.MimeType == "application/zip"
                    || attachment.ContentType.MimeType == "application/x-zip-compressed"
                    || (attachment.ContentType.MimeType == "application/octet-stream" && Path.GetExtension(attachment.ContentType.Name) == ".zip"))
                {
                    pathExtration = pathDowloadDocument + Path.GetFileNameWithoutExtension(attachment.ContentType.Name);
                    Directory.CreateDirectory(pathExtration);

                    if (attachment is MimePart mimePart)
                    {
                        using (var stream = File.Create(pathDowloadDocument + attachment.ContentType.Name))
                        {
                            mimePart.Content.DecodeTo(stream);
                        }
                    }

                }
                else if (attachment.ContentType.MimeType == "application/pdf"
                    || attachment.ContentType.MimeType == "application/xml")
                {
                    if (attachment is MimePart mimePart)
                    {
                        using (var stream = File.Create(pathDowloadDocument + attachment.ContentType.Name))
                        {
                            mimePart.Content.DecodeTo(stream);
                        }
                    }
                }

                if (!string.IsNullOrEmpty(pathExtration))
                {
                    using (FileStream zipFile = File.Open(pathDowloadDocument + attachment.ContentType.Name, FileMode.Open))
                    {
                        using (var archive = new Archive(zipFile))
                        {
                            // Descomprimir archivos a la carpeta
                            archive.ExtractToDirectory(pathExtration);

                        }
                    }
                }

                ExtractFile(pathExtration);

            }
            catch (Exception ex)
            {
                _logger.Error(ex.Message);
            }
        }
        public static bool ExistsZipFile(string path)
        {
            // Verifica si el directorio existe
            if (Directory.Exists(path))
            {
                // Obtiene la lista de archivos en el directorio
                string[] archivos = Directory.GetFiles(path);

                // Itera sobre los archivos para verificar si alguno es un archivo ZIP
                foreach (string archivo in archivos)
                {

                    // Compara si la extensión es .zip
                    if (Path.GetExtension(archivo) == ".zip")
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        public void ExtractFile(string path)
        {
            if (Directory.Exists(path))
            {
                // Obtiene la lista de archivos en el directorio
                string[] archivos = Directory.GetFiles(path);

                // Itera sobre los archivos para verificar si alguno es un archivo ZIP
                foreach (string archivo in archivos)
                {
                    if (Path.GetExtension(archivo) == ".zip")
                    {
                        if (!Directory.Exists(path + "/" + Path.GetFileNameWithoutExtension(archivo)))
                            Directory.CreateDirectory(path + "/" + Path.GetFileNameWithoutExtension(archivo));

                        using (FileStream zipFile = File.Open(archivo, FileMode.Open))
                        {
                            using (var archive = new Archive(zipFile))
                            {
                                // Descomprimir archivos a la carpeta
                                archive.ExtractToDirectory(path + "/" + Path.GetFileNameWithoutExtension(archivo));

                            }
                        }
                    }
                }
            }
        }

        public InvoiceDTO GetElectronicDocument(string pathXml)
        {
            var electronicDocument = new InvoiceDTO();
            var invoiceItems = new List<InvoiceItemDTO>();

            XmlDocument xmlDocument = new XmlDocument();
            xmlDocument.Load(pathXml);

            // 🧩 Detectar namespaces del documento raíz
            var nsMgr = new XmlNamespaceManager(xmlDocument.NameTable);
            var root = xmlDocument.DocumentElement;
            var cbcNs = root?.GetNamespaceOfPrefix("cbc") ?? "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
            var cacNs = root?.GetNamespaceOfPrefix("cac") ?? "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
            nsMgr.AddNamespace("cbc", cbcNs);
            nsMgr.AddNamespace("cac", cacNs);

            try
            {
                XmlDocument targetDoc = xmlDocument; // Documento que realmente tiene los datos

                // 📦 Si es un AttachedDocument, extraer el XML interno
                var innerXmlText = xmlDocument.SelectSingleNode("//cac:Attachment//cbc:Description", nsMgr)?.InnerText;
                if (!string.IsNullOrWhiteSpace(innerXmlText))
                {
                    try
                    {
                        var innerDoc = new XmlDocument();
                        innerDoc.LoadXml(innerXmlText);

                        var innerNsMgr = new XmlNamespaceManager(innerDoc.NameTable);
                        innerNsMgr.AddNamespace("cbc", cbcNs);
                        innerNsMgr.AddNamespace("cac", cacNs);

                        targetDoc = innerDoc;
                        nsMgr = innerNsMgr;
                        //_logger.Info("📎 Documento interno (AttachedDocument) detectado y cargado correctamente.");
                    }
                    catch (Exception ex)
                    {
                        _logger.Warn($"No se pudo parsear el XML interno del AttachedDocument: {ex.Message}");
                    }
                }

                // === CABECERA ===
                electronicDocument.DocumentNumber =
                    targetDoc.SelectSingleNode("//cbc:ParentDocumentID", nsMgr)?.InnerText?.Trim()
                    ?? targetDoc.SelectSingleNode("//cbc:ID", nsMgr)?.InnerText?.Trim()
                    ?? string.Empty;

                var issueDateText = targetDoc.SelectSingleNode("//cbc:IssueDate", nsMgr)?.InnerText?.Trim();
                if (DateTime.TryParse(issueDateText, out var issueDate))
                    electronicDocument.IssueDate = issueDate;
                else
                    electronicDocument.IssueDate = DateTime.MinValue;

                electronicDocument.DocumentType =
                    targetDoc.SelectSingleNode("//cbc:InvoiceTypeCode", nsMgr)?.InnerText?.Trim()
                    ?? targetDoc.SelectSingleNode("//cbc:CreditNoteTypeCode", nsMgr)?.InnerText?.Trim()
                    ?? string.Empty;

                // === EMISOR ===
                // === EMISOR ===
                electronicDocument.SenderBusinessName =
                    targetDoc.SelectSingleNode("//cac:SenderParty//cbc:RegistrationName", nsMgr)?.InnerText?.Trim()
                    ?? targetDoc.SelectSingleNode("//cac:AccountingSupplierParty//cbc:RegistrationName", nsMgr)?.InnerText?.Trim()
                    ?? string.Empty;

                XmlNode senderIdNode =
                    targetDoc.SelectSingleNode("//cac:SenderParty//cbc:CompanyID", nsMgr)
                    ?? targetDoc.SelectSingleNode("//cac:AccountingSupplierParty//cbc:CompanyID", nsMgr);

                if (senderIdNode != null)
                {
                    electronicDocument.SenderTaxIdWithoutCheckDigit = senderIdNode.InnerText?.Trim() ?? string.Empty;
                    electronicDocument.SenderTaxId =
                        (senderIdNode.InnerText?.Trim() ?? string.Empty) +
                        (senderIdNode.Attributes?["schemeID"]?.Value ?? string.Empty);
                }

                // === RECEPTOR ===
                electronicDocument.ReceiverBusinessName =
                    targetDoc.SelectSingleNode("//cac:ReceiverParty//cbc:RegistrationName", nsMgr)?.InnerText?.Trim()
                    ?? targetDoc.SelectSingleNode("//cac:AccountingCustomerParty//cbc:RegistrationName", nsMgr)?.InnerText?.Trim()
                    ?? string.Empty;

                XmlNode receiverIdNode =
                    targetDoc.SelectSingleNode("//cac:ReceiverParty//cbc:CompanyID", nsMgr)
                    ?? targetDoc.SelectSingleNode("//cac:AccountingCustomerParty//cbc:CompanyID", nsMgr);

                if (receiverIdNode != null)
                {
                    electronicDocument.ReceiverTaxIdWithoutCheckDigit = receiverIdNode.InnerText?.Trim() ?? string.Empty;
                    electronicDocument.ReceiverTaxId =
                        (receiverIdNode.InnerText?.Trim() ?? string.Empty) +
                        (receiverIdNode.Attributes?["schemeID"]?.Value ?? string.Empty);
                }

                // === DOCUMENTO RELACIONADO ===
                electronicDocument.RelatedDocumentNumber =
                    targetDoc.SelectSingleNode("//cac:OrderReference/cbc:ID", nsMgr)?.InnerText?.Trim() ?? string.Empty;

                // === TOTALES ===
                string payableAmountText = null;

                // Buscar en contexto principal
                payableAmountText = targetDoc.SelectSingleNode("//cac:LegalMonetaryTotal/cbc:PayableAmount", nsMgr)?.InnerText;

                // Si no lo encuentra, intentar buscar en XML embebido dentro de un CDATA (caso AttachedDocument)
                if (string.IsNullOrWhiteSpace(payableAmountText))
                {
                    var innerXmlText2 = targetDoc.SelectSingleNode("//cbc:Description", nsMgr)?.InnerText;
                    if (!string.IsNullOrWhiteSpace(innerXmlText2) && innerXmlText2.Contains("<Invoice"))
                    {
                        try
                        {
                            var innerDoc2 = new XmlDocument();
                            innerDoc2.LoadXml(innerXmlText2);

                            var innerNsMgr2 = new XmlNamespaceManager(innerDoc2.NameTable);
                            innerNsMgr2.AddNamespace("cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
                            innerNsMgr2.AddNamespace("cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");

                            payableAmountText = innerDoc2.SelectSingleNode("//cac:LegalMonetaryTotal/cbc:PayableAmount", innerNsMgr2)?.InnerText;
                        }
                        catch (Exception ex)
                        {
                            _logger.Warn($"No se pudo leer el PayableAmount del XML interno: {ex.Message}");
                        }
                    }
                }

                // Valor por defecto si no se encontró
                electronicDocument.Amount = string.IsNullOrWhiteSpace(payableAmountText) ? "0.00" : payableAmountText.Trim();

                // === FECHA DE VENCIMIENTO ===
                var dueDateText = targetDoc.SelectSingleNode("//cac:PaymentMeans/cbc:PaymentDueDate", nsMgr)?.InnerText?.Trim();
                if (DateTime.TryParse(dueDateText, out var dueDate))
                    electronicDocument.DueDate = dueDate;
                else
                    electronicDocument.DueDate = DateTime.MinValue;

                // === LÍNEAS DE FACTURA ===
                var invoiceLineNodes = targetDoc.SelectNodes("//cac:InvoiceLine", nsMgr);
                if (invoiceLineNodes != null)
                {
                    foreach (XmlNode lineNode in invoiceLineNodes)
                    {
                        var item = new InvoiceItemDTO();

                        item.ItemCode = lineNode.SelectSingleNode("cbc:ID", nsMgr)?.InnerText?.Trim() ?? string.Empty;

                        var qtyText = lineNode.SelectSingleNode("cbc:InvoicedQuantity", nsMgr)?.InnerText?.Trim() ?? "0";
                        if (decimal.TryParse(qtyText, out var qtyDecimal))
                            item.Quantity = (int)Math.Round(qtyDecimal);
                        else
                            item.Quantity = 0;

                        item.Subtotal = lineNode.SelectSingleNode("cbc:LineExtensionAmount", nsMgr)?.InnerText?.Trim() ?? "0.00";
                        item.UnitPrice = lineNode.SelectSingleNode("cac:Price/cbc:PriceAmount", nsMgr)?.InnerText?.Trim() ?? "0.00";
                        item.Unit = lineNode.SelectSingleNode("cac:Price/cbc:BaseQuantity", nsMgr)?.Attributes?["unitCode"]?.Value ?? string.Empty;
                        item.Description = lineNode.SelectSingleNode("cac:Item/cbc:Description", nsMgr)?.InnerText?.Trim() ?? string.Empty;
                        item.TaxAmount = lineNode.SelectSingleNode("cac:TaxTotal/cbc:TaxAmount", nsMgr)?.InnerText?.Trim() ?? "0.00";

                        if (decimal.TryParse(item.Subtotal, out var subtotal) && decimal.TryParse(item.TaxAmount, out var tax))
                            item.Total = (subtotal + tax).ToString("0.##");
                        else
                            item.Total = item.Subtotal;

                        invoiceItems.Add(item);
                    }
                }

                electronicDocument.InvoiceItems = invoiceItems;
            }
            catch (Exception ex)
            {
                _logger?.Error($"❌ Error procesando XML '{pathXml}': {ex.Message}");
            }

            return electronicDocument;
        }

        public List<string> GetFilesUpload(string pathDowload)
        {
            List<string> files = new List<string>();

            if (Directory.Exists(pathDowload))
            {
                List<string> directorys = Directory.GetDirectories(pathDowload).ToList();
                if (directorys.Count > 0)
                {
                    foreach (string directory in directorys)
                    {
                        files.AddRange(GetFilesUpload(directory));
                    }
                }
                //else
                //{
                List<string> filesUpload = Directory.GetFiles(pathDowload).ToList();
                if (filesUpload.Count > 0)
                {
                    foreach (string file in filesUpload)
                    {
                        if (Path.GetExtension(file) == ".zip")
                            continue;

                        files.Add(file);
                    }
                }
                //}

            }

            return files;

        }
        public string GetPathXML(string pathDowloadDocument)
        {

            if (Directory.Exists(pathDowloadDocument))
            {
                List<string> files = null;
                List<string> paths = Directory.GetDirectories(pathDowloadDocument, "*", SearchOption.AllDirectories).ToList();
                if (paths.Count > 0)
                {
                    foreach (string path in paths)
                    {
                        files = Directory.GetFiles(path).ToList();
                        if (files.Count > 0)
                        {
                            foreach (string file in files)
                            {
                                if (Path.GetExtension(file) == ".xml")
                                {
                                    return file;
                                }
                            }
                        }
                    }
                }

                files = Directory.GetFiles(pathDowloadDocument).ToList();
                if (files.Count > 0)
                {
                    foreach (string file in files)
                    {
                        if (Path.GetExtension(file) == ".xml")
                        {
                            return file;
                        }
                    }
                }

                return "";

            }

            throw new NotImplementedException();
        }
        public bool ValidateInformationElectronicDocument(InvoiceDTO electronicDocument)
        {

            if (String.IsNullOrEmpty(electronicDocument.DocumentNumber))
            {
                return false;
            }

            if (String.IsNullOrEmpty(electronicDocument.ReceiverTaxId))
            {
                return false;
            }

            if (String.IsNullOrEmpty(electronicDocument.ReceiverBusinessName))
            {
                return false;
            }

            if (String.IsNullOrEmpty(electronicDocument.SenderTaxId))
            {
                return false;
            }

            if (String.IsNullOrEmpty(electronicDocument.SenderBusinessName))
            {
                return false;
            }

            if (String.IsNullOrEmpty(electronicDocument.Amount))
            {
                return false;
            }
            if (String.IsNullOrEmpty(electronicDocument.IssueDate.ToString()))
            {
                return false;
            }

            return true;

        }


    }

}
