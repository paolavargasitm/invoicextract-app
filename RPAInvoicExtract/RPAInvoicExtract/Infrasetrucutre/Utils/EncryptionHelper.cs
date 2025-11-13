using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace RPAInvoicExtract.Infrasetrucutre.Utils
{
    internal class EncryptionHelper
    {
        public static string DecryptBase64(string base64Cipher, string utf8Key)
        {
            byte[] key = Encoding.UTF8.GetBytes(utf8Key);
            byte[] ct = Convert.FromBase64String(base64Cipher);

            using (var aes = Aes.Create())
            {
                aes.Mode = CipherMode.ECB;
                aes.Padding = PaddingMode.PKCS7;
                aes.KeySize = 256;
                aes.Key = key;

                using (var dec = aes.CreateDecryptor())
                {
                    byte[] pt = dec.TransformFinalBlock(ct, 0, ct.Length);
                    return Encoding.UTF8.GetString(pt);
                }
            }
        }
    }
}
