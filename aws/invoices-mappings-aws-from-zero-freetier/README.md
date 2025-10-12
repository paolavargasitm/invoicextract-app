# Invoices — AWS from zero (VPC + RDS MySQL Free Tier) + Liquibase
Date: 2025-10-14

Este proyecto crea **red de cero** (VPC con subred pública e Internet Gateway) y una **RDS MySQL** en Free Tier (db.t4g.micro, 20GB gp2, Single-AZ), accesible **solo** desde tu IP pública (variable `allowed_cidr`). Sin NAT Gateway (ahorro de costos).

## Pasos
1) Edita `terraform/envs/dev/dev.tfvars` y pon tu IP pública (ej. `181.50.xx.yy/32`).
2) Ejecuta:
```bash
cd terraform/envs/dev
terraform init
terraform plan -var-file="dev.tfvars"
terraform apply -var-file="dev.tfvars" -auto-approve
terraform output -json > ../../outputs-dev.json
```
3) Aplica esquema con Liquibase:
```bash
cd ../../..
bash scripts/run_liquibase.sh ./terraform/outputs-dev.json
```

## Notas
- RDS `publicly_accessible = true` SOLO para dev. En prod, usa subred privada + NAT/VPC Endpoints o bastión.
- Storage `gp2` y 20GB para Free Tier.
- Cambia región/AZ si lo necesitas.


## También despliega la base de **mappings** en la MISMA instancia RDS
Para ahorrar costos (Free Tier), se usa **una sola RDS MySQL** con dos bases: `invoices` y `mappings`.
El script `scripts/run_liquibase_both.sh`:
1. Crea `mappings` si no existe (via Liquibase bootstrap).
2. Aplica los changelogs de `invoices`.
3. Aplica los changelogs de `mappings`.

### Comandos
```bash
cd terraform/envs/dev
terraform apply -var-file="dev.tfvars" -auto-approve
terraform output -json > ../../outputs-dev.json

cd ../../..
bash scripts/run_liquibase_both.sh ./terraform/outputs-dev.json
```
