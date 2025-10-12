variable "region" {
  type    = string
  default = "us-east-1"
}

variable "project" {
  type    = string
  default = "invoices"
}

variable "allowed_cidr" {
  type        = string
  description = "Your IP/CIDR for MySQL (e.g., 203.0.113.10/32)"
}
