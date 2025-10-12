output "rds_endpoint"             { value = aws_db_instance.this.address }
output "db_username"              { value = var.db_username }
output "db_name"                  { value = var.db_name }
output "db_password_secret_name"  { value = aws_secretsmanager_secret.db.name }
output "jdbc_url" {
  value = "jdbc:mysql://${aws_db_instance.this.address}:3306/${var.db_name}?sslMode=REQUIRED&allowPublicKeyRetrieval=false"
}
output "db_sg_id"                 { value = aws_security_group.db.id }
