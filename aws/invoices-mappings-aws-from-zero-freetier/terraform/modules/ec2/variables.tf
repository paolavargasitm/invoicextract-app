variable "region" {
  description = "AWS region where to deploy"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project/name prefix for resources"
  type        = string
  default     = "invoicextract"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.medium"
}

variable "key_pair_name" {
  description = "Existing AWS key pair name to SSH into the instance"
  type        = string
}

variable "allowed_ssh_cidr" {
  description = "CIDR allowed to SSH (e.g. your public IP/32)"
  type        = string
}

variable "allowed_http_cidr" {
  description = "CIDR allowed to access web ports (frontend, keycloak, etc.)"
  type        = string
  default     = "0.0.0.0/0"
}
