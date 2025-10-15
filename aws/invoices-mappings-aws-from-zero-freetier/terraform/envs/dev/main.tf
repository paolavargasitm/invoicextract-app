module "network" {
  source             = "../../modules/network"
  name               = var.project
  cidr_block         = "10.0.0.0/16"
  public_subnet_cidr = "10.0.1.0/24"
}

module "rds" {
  source       = "../../modules/rds_mysql"
  name         = var.project
  db_name      = "invoices"
  db_username  = "invoices"
  vpc_id       = module.network.vpc_id
  subnet_ids    = module.network.public_subnet_ids
  allowed_cidr = var.allowed_cidr
}

output "vpc_id" { value = module.network.vpc_id }
output "client_sg_id" { value = module.network.client_sg_id }

output "rds_endpoint" { value = module.rds.rds_endpoint }
output "jdbc_url" { value = module.rds.jdbc_url }
output "db_username" { value = module.rds.db_username }
output "db_name" { value = module.rds.db_name }
output "db_password_secret_name" { value = module.rds.db_password_secret_name }
