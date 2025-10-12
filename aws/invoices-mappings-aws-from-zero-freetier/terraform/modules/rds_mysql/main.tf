// Subnet group (single subnet to keep Single-AZ and ultra-simple demo)
resource "aws_db_subnet_group" "this" {
  name       = "${var.name}-subnet-group"
  subnet_ids = [var.subnet_id]
  tags = { Name = "${var.name}-subnet-group" }
}

// SG for RDS (allow 3306 only from allowed CIDR)
resource "aws_security_group" "db" {
  name        = "${var.name}-db-sg"
  description = "MySQL inbound from allowed CIDR"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = [var.allowed_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

// Random password + Secrets Manager
resource "random_password" "db" {
  length  = 20
  special = true
  override_characters = "!@#%^*-_=+."
}

resource "aws_secretsmanager_secret" "db" { name = "${var.name}-db-pw" }

resource "aws_secretsmanager_secret_version" "db" {
  secret_id     = aws_secretsmanager_secret.db.id
  secret_string = random_password.db.result
}

// RDS Instance (Free Tier pinned)
resource "aws_db_instance" "this" {
  identifier       = "${var.name}-db"
  engine           = "mysql"
  engine_version   = var.engine_version

  instance_class   = "db.t4g.micro"
  allocated_storage= 20
  storage_type     = "gp2"

  username         = var.db_username
  password         = aws_secretsmanager_secret_version.db.secret_string
  db_name          = var.db_name

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.db.id]

  multi_az               = false
  publicly_accessible    = true   // cheaper: no NAT needed; lock to your IP with allowed_cidr
  storage_encrypted      = true
  backup_retention_period= 7
  skip_final_snapshot    = true

  tags = { Name = "${var.name}-db" }
}
