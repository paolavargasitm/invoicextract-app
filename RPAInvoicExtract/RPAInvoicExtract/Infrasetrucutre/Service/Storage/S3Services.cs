using Amazon;
using Amazon.S3;
using Amazon.S3.Transfer;
using RPAInvoicExtract.Infrasetrucutre.Loggin;
using System;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.Service.Storage
{
    public class S3Services
    {
        private readonly string _bucketName = "bk-invoicextract";
        private readonly IAmazonS3 _s3Client;
        private readonly IAppLogger _logger;

        // 🔹 Constructor recibe el logger como dependencia
        public S3Services(IAppLogger logger)
        {
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));

            _s3Client = new AmazonS3Client(
                "AKIASMXOUPAWNIANW3VD",
                "r4Hdu4L3aqdmtXFm0h5Ums99h5wEKq0HodHrSAOK",
                RegionEndpoint.USEast2 // Ohio
            );
        }

        public async Task<string> UploadFileAsync(string filePath, string s3Key)
        {
            try
            {
                var fileTransferUtility = new TransferUtility(_s3Client);
                await fileTransferUtility.UploadAsync(filePath, _bucketName, s3Key);

                _logger.Info($"✅ Archivo subido correctamente: {s3Key}");

                // Retorna la URL pública del archivo en S3
                string fileUrl = $"https://{_bucketName}.s3.amazonaws.com/{s3Key}";
                return fileUrl;
            }
            catch (Exception ex)
            {
                _logger?.Error($"❌ Error subiendo archivo a S3: {ex.Message}");
                _logger?.Error($"🔍 StackTrace: {ex.StackTrace}");
                return null;
            }
        }
    }
}
