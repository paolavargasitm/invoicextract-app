variable "aws_region" {
  description = "Región de AWS donde crear el bucket"
  type        = string
  default     = "us-east-1"
}

variable "bucket_name" {
  description = "Nombre único del bucket S3"
  type        = string
  default     = "invoicextract-s3"
}
