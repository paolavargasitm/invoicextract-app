output "vpc_id"               { value = aws_vpc.this.id }
output "public_subnet_id"     { value = aws_subnet.public.id }
output "client_sg_id"         { value = aws_security_group.client.id }
