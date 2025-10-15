variable "name" {
  type = string
  # e.g., "invoices"
}

variable "db_name" {
  type    = string
  default = "invoices"
}

variable "db_username" {
  type    = string
  default = "invoices"
}

variable "vpc_id" {
  type = string
}

variable "allowed_cidr" {
  type = string
  # e.g., "YOUR_IP/32" for dev
}

variable "engine_version" {
  type    = string
  default = "8.0.36"
}

variable "subnet_ids" {
  type = list(string)
  description = "List of subnet IDs covering at least two AZs"
}