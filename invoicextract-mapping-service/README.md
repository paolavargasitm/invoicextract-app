# InvoiceExtract Mapping Service (Spring Boot + MapStruct + Hexagonal + Liquibase)

Microservicio para traducir facturas de `Invoices DB` a formatos de distintos ERPs usando reglas configurables guardadas
en `Mappings DB`.

## Stack

- Java 21, Spring Boot 3.3.x, MapStruct, Liquibase, Springdoc OpenAPI, Spring Security, Spring Cache (Caffeine)
- PostgreSQL (Mappings DB como principal; Invoices DB lectura futura)
- Maven, Testcontainers

## Ejecutar (dev)

1. `docker-compose up -d` (levanta postgres para mappings y invoices)
2. Exporta variables si lo deseas (o usa defaults del `application.yml`)
3. `./mvnw spring-boot:run` o `mvn spring-boot:run`
4. Swagger UI: `http://localhost:8080/swagger-ui.html`

## Endpoints

- ERPs: `POST /api/erps`, `GET /api/erps`, `PATCH /api/erps/{id}/status`
- Mappings: `GET /api/mappings?erp={name}&status=ACTIVE`, `POST /api/mappings`, `PUT /api/mappings/{id}`,
  `PATCH /api/mappings/{id}/status`
- Export: `GET /api/export?erp=ERP_NAME&format=json|csv`

> Nota: El adapter de Invoices está como **stub** de datos para facilitar la prueba local. Puedes reemplazarlo por
> JDBC/JPA contra tu Invoices DB.

## Arquitectura Hexagonal

- `domain/` (modelos, servicios, puertos)
- `application/` (casos de uso)
- `adapters/in/` (REST)
- `adapters/out/` (JPA/JDBC/Exportadores)
- `infra/` (config, security, liquibase)
