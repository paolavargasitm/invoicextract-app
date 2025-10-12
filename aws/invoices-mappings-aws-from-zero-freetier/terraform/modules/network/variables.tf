variable "name" {
  type = string
}

variable "cidr_block" {
  type    = string
  default = "10.0.0.0/16"
}

variable "region_az1" {
  type    = string
  default = "us-east-1a"
}

variable "public_subnet_cidr" {
  type    = string
  default = "10.0.1.0/24"
}
