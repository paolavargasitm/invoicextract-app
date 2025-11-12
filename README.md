# 📄 InvoiceExtract Application

## 🚀 Descripción del Repositorio

**InvoiceExtract** es una aplicación empresarial completa desarrollada con **Spring Boot** para la gestión automatizada de facturas. La aplicación proporciona un sistema robusto de extracción, procesamiento y gestión de documentos de facturación con capacidades de integración ERP y procesamiento asíncrono mediante Apache Kafka.

### 🏗️ Arquitectura del Sistema

La aplicación está construida siguiendo principios de **Clean Architecture** y **Domain-Driven Design (DDD)**, organizando el código en las siguientes capas:

- **🎯 Domain Layer**: Entidades, enums y repositorios de dominio
- **🔧 Application Layer**: Casos de uso, servicios, DTOs y controladores REST
- **🏗️ Infrastructure Layer**: Implementaciones de repositorios, configuraciones y servicios externos

### 🛠️ Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Backend Framework** | Spring Boot | 3.2.0 |
| **Base de Datos** | MySQL | 8.0 |
| **Message Broker** | Apache Kafka | 3.5.1 |
| **Migración DB** | Liquibase | 4.33.0 |
| **Documentación API** | Swagger/OpenAPI | 3.0 |
| **Contenedores** | Docker & Docker Compose | Latest |
| **Administrador DB** | Adminer | Latest |

### 🎯 Funcionalidades Principales

- ✅ **Gestión de Facturas**: CRUD completo con validación y auditoría
- ✅ **Extracción de Metadatos**: Procesamiento automático de documentos PDF
- ✅ **Configuración de Email**: Gestión segura de credenciales IMAP/SMTP
- ✅ **Integración ERP**: Notificaciones y equivalencias de sistemas externos
- ✅ **Procesamiento Asíncrono**: Cola de mensajes con Kafka
- ✅ **Auditoría Completa**: Trazabilidad de todas las operaciones
- ✅ **API RESTful**: Documentada con Swagger/OpenAPI
- ✅ **Seguridad**: Encriptación de credenciales y validación de datos

### 🧩 Módulos del Monorepo

- **invoicextract-backend**: API principal de facturas (Spring Boot)
- **invoicextract-mapping-service**: Servicio de mapeos ERP (Spring Boot)
- **frontend**: Frontend React (versión actual)
- **keycloak/themes**: Tema de Keycloak para autenticación
- **liquibase**: Cambios de base de datos para `invoices`
- **liquibase-mappings | liquibase-mappings**: Cambios de base de datos para `mappings`
- **mysql-init**: Scripts de inicialización local
- **postman**: Colecciones para pruebas
 - **RPAInvoicExtract**: Agente RPA Windows (.NET Framework 4.8) para descarga/procesamiento de facturas

## 🐳 Construcción y Despliegue con Docker

### 📋 Prerrequisitos

Antes de ejecutar la aplicación, asegúrate de tener instalado:

- **Docker**: Versión 20.10 o superior
- **Docker Compose**: Versión 2.0 o superior
- **Git**: Para clonar el repositorio

### 🚀 Instrucciones de Construcción

#### 1. **Clonar el Repositorio**
```bash
git clone <repository-url>
cd invoicextract-app
```

#### 2. **Construcción y Ejecución Completa**
```bash
# Construir y ejecutar todos los servicios
docker-compose up --build

# Ejecutar en segundo plano (detached mode)
docker-compose up --build -d
```

#### 3. **Comandos Útiles de Docker**

```bash
# Ver el estado de los contenedores
docker-compose ps

# Ver logs de todos los servicios
docker-compose logs

# Ver logs de un servicio específico
docker-compose logs app
docker-compose logs mysql
docker-compose logs kafka

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v --remove-orphans

# Reconstruir solo la aplicación
docker-compose up --build app
```

### 🏗️ Arquitectura de Contenedores

La aplicación utiliza **Docker Compose** para orquestar múltiples servicios:

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **keycloak-db** | `-` | PostgreSQL para Keycloak |
| **keycloak** | `8085` | Servidor de autenticación Keycloak |
| **mysql** | `3306` | Base de datos MySQL `invoices` |
| **mysql-mappings** | `3307` | Base de datos MySQL `mappings` |
| **adminer** | `8081` | Admin de base de datos para `invoices` y `mappings` |
| **liquibase** | `-` | Migraciones para `invoices` |
| **liquibase-mappings** | `-` | Migraciones para `mappings` |
| **kafka** | `9092` | Message broker para procesamiento asíncrono |
| **app** | `8080` | API principal Spring Boot (`/invoicextract`) |
| **mapping-service** | `8082` | API de mapeos ERP (`/invoice-mapping`) |
| **frontend** | `3000` | Frontend React |
| **sonar-db** | `-` | PostgreSQL para SonarQube |
| **sonarqube** | `9000` | Plataforma de calidad de código |

### 🔄 Proceso de Inicialización

1. **MySQL** se inicia y crea la base de datos `invoices`
2. **Liquibase** ejecuta las migraciones de esquema automáticamente
3. **Kafka** se configura con los topics necesarios
4. **Spring Boot App** se conecta a todos los servicios y expone la API
5. **Adminer** proporciona interfaz web para gestión de BD

### 🌐 URLs de Acceso

Una vez que la aplicación esté ejecutándose, puedes acceder a:

- **🔗 API Principal**: http://localhost:8080/invoicextract
- **📚 Swagger UI (Backend)**: http://localhost:8080/invoicextract/swagger-ui/index.html
- **🔗 API Mapping Service**: http://localhost:8082/invoice-mapping
- **📚 Swagger UI (Mapping Service)**: http://localhost:8082/swagger-ui.html
- **🗄️ Adminer (DB Admin)**: http://localhost:8081
- **🔐 Keycloak**: http://localhost:8085
- **🖥️ Frontend**: http://localhost:3000
- **🖥️ Frontend (New)**: http://localhost:3001
- **📊 Health Check**: http://localhost:8080/invoicextract/actuator/health
- **🧪 SonarQube**: http://localhost:9000

### 🧪 Calidad de Código con SonarQube

SonarQube está incluido en docker-compose para análisis de calidad del backend y servicios.

1. Inicia la plataforma:
   - `docker-compose up -d --build`
2. Accede a SonarQube: http://localhost:9000
   - Credenciales por defecto: `admin` / `admin` (se te pedirá cambiar contraseña)
3. Crea un Token Personal en SonarQube (My Account → Security).
4. Ejecuta el análisis desde el módulo backend (o raíz, según tu POM):

```bash
# Desde c:\invoicextract-app\invoicextract-backend
mvn -DskipTests=true clean verify sonar:sonar ^
  -Dsonar.host.url=http://localhost:9000 ^
  -Dsonar.login=<TU_TOKEN> ^
  -Dsonar.projectKey=invoicextract-backend
```

Notas:
- Ajusta `sonar.projectKey` si tienes varios módulos (e.g., `invoicextract-mapping-service`).
- Si tu POM ya define propiedades Sonar, puedes omitir flags redundantes.

## 🤖 Servicio RPA (Windows)

### Descripción

El proyecto `RPAInvoicExtract` es un agente Windows (C# .NET Framework 4.8) que automatiza la lectura de emails y el envío de documentos de factura al backend. Consume credenciales de correo seguras desde la API y envía las facturas para procesamiento asíncrono.

### Prerrequisitos

- Windows 10/11
- .NET Framework 4.8 Runtime
- (Opcional) Visual Studio 2022 para compilar/depurar

### Configuración (App.config)

Clave-valor principales en `RPAInvoicExtract/RPAInvoicExtract/App.config`:

- `keyencrypt`: Clave de encriptación simétrica de 32 caracteres. Debe coincidir con la clave usada por el backend para desencriptar credenciales.
- `urlToken`: URL de token de Keycloak para obtener `access_token`.
- `client_idServices` y `client_secret`: Credenciales del cliente Keycloak que el RPA usa para autenticarse.
- `urlEmails`: Endpoint del backend para obtener la configuración de email activa del usuario.
- `urlInvoice`: Endpoint del backend para enviar la factura a procesamiento asíncrono.
- `pathDowload`: Carpeta local donde el RPA descarga los archivos procesados.

Valores por defecto de ejemplo alineados con Docker local:

```xml
<add key="urlToken" value="http://localhost:8085/realms/invoicextract/protocol/openid-connect/token" />
<add key="client_idServices" value="invoices-backend" />
<add key="client_secret" value="TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA" />
<add key="urlEmails" value="http://localhost:8080/invoicextract/api/config/email/active" />
<add key="urlInvoice" value="http://localhost:8080/invoicextract/api/invoices/async" />
<add key="pathDowload" value="C:\\RPAInvoicExtract\\Downloads\\" />
```

⚠️ Seguridad: No uses `client_secret` ni `keyencrypt` por defecto en producción. Administra secretos de forma segura.

### Ejecución local

1. Levanta la plataforma con Docker:
   - `docker-compose up -d --build`
2. Verifica que los servicios estén disponibles:
   - Keycloak: http://localhost:8085
   - Backend: http://localhost:8080/invoicextract
3. Ajusta `App.config` si es necesario (URLs/secretos/rutas).
4. Compila y ejecuta el proyecto `RPAInvoicExtract.sln` en Visual Studio (o ejecuta el `.exe` compilado).

### (Opcional) Instalar como servicio de Windows

- Puedes usar NSSM o `sc.exe` para registrar el ejecutable como servicio.
- Asegura permisos de escritura en `pathDowload` y acceso a red.

### Flujo de trabajo

1. Obtiene token de Keycloak usando `urlToken` + `client_idServices`/`client_secret`.
2. Consulta credenciales activas de email vía `urlEmails`.
3. Descarga/lee adjuntos, los guarda en `pathDowload`.
4. Envía a backend para procesamiento con `urlInvoice` (proceso asíncrono vía Kafka).

## 📁 Estructura del Proyecto

```
invoicextract-app/
├── 📂 invoicextract-backend/          # Aplicación Spring Boot principal
│   ├── 📂 src/main/java/
│   │   └── 📂 co/edu/itm/invoiceextract/
│   │       ├── 📂 application/        # Capa de aplicación
│   │       │   ├── 📂 config/         # Configuraciones (Swagger, Security, etc.)
│   │       │   ├── 📂 controller/     # Controladores REST
│   │       │   ├── 📂 dto/            # Data Transfer Objects
│   │       │   ├── 📂 mapper/         # Mappers entre entidades y DTOs
│   │       │   ├── 📂 service/        # Servicios de aplicación
│   │       │   └── 📂 usecase/        # Casos de uso específicos
│   │       ├── 📂 domain/             # Capa de dominio
│   │       │   ├── 📂 entity/         # Entidades JPA
│   │       │   ├── 📂 enums/          # Enumeraciones
│   │       │   └── 📂 repository/     # Interfaces de repositorio
│   │       └── 📂 infrastructure/     # Capa de infraestructura
│   │           └── 📂 errors/         # Manejo de errores
│   └── 📂 src/main/resources/
│       ├── 📄 application.yml         # Configuración de la aplicación
│       └── 📂 db/changelog/           # Migraciones Liquibase alternativas
├── 📂 liquibase/                      # Migraciones de base de datos
│   ├── 📄 db.changelog-master.yaml    # Changelog principal
│   ├── 📄 001-create-tables.yaml      # Tablas principales (invoices, invoice_metadata)
│   ├── 📄 002-create-email-configurations-table.yaml
│   └── 📄 003-create-processing-error-logs-table.yaml
├── 📂 keycloak-config/                # Configuración de autenticación

La aplicación incluye configuración completa para despliegue en **Microsoft Azure** usando:

- **Azure App Service**: Para hospedar la aplicación Spring Boot
- **Azure Database for MySQL**: Base de datos MySQL gestionada
- **Azure Container Registry**: Almacenamiento de imágenes Docker
- **Azure Key Vault**: Gestión segura de secretos

#### 📋 Requisitos Previos

- Azure CLI instalado y configurado
- Docker instalado y ejecutándose
- Suscripción de Azure con permisos apropiados

#### 🚀 Despliegue Rápido

```bash
# Navegar al directorio de Azure
cd azure

# Ejecutar script de despliegue (PowerShell)
.\deploy.ps1 -ResourceGroupName "invoicextract-rg" `
             -Location "East US" `
             -MySqlAdminPassword "TuPasswordSeguro123!" `
             -EncryptionSecretKey "TuClaveDeEncriptacion123456789012345678901234567890"

# O ejecutar script de despliegue (Bash)
./deploy.sh "invoicextract-rg" "East US" "TuPasswordSeguro123!" "TuClaveDeEncriptacion123456789012345678901234567890"
```

#### 🌐 URLs de Azure

Después del despliegue exitoso:

- **🔗 Aplicación**: `https://{app-name}-app.azurewebsites.net/invoicextract`
- **📚 Swagger UI**: `https://{app-name}-app.azurewebsites.net/invoicextract/swagger-ui/index.html`
- **📊 Health Check**: `https://{app-name}-app.azurewebsites.net/invoicextract/actuator/health`

📖 **Documentación completa**: Ver [azure/README.md](azure/README.md) para instrucciones detalladas.

## 🛠️ Desarrollo y Configuración

### 🔧 Variables de Entorno

La aplicación utiliza las siguientes variables de entorno principales:

```yaml
# Base de datos
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/invoices
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: password

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092

# Encriptación (⚠️ Cambiar en producción)
ENCRYPTION_SECRET_KEY: your-secret-key-here

# Mapping Service (invoicextract-mapping-service)
MAPPINGS_DB_URL: jdbc:mysql://mysql-mappings:3306/mappings
MAPPINGS_DB_USER: root
MAPPINGS_DB_PASS: root
INVOICES_DB_URL: jdbc:mysql://mysql:3306/invoices
INVOICES_DB_USER: root
INVOICES_DB_PASS: root

# Frontend (build args)
VITE_KEYCLOAK_URL: http://localhost:8085
VITE_KEYCLOAK_REALM: invoicextract
VITE_KEYCLOAK_CLIENT_ID: invoices-frontend
VITE_BACKEND_BASE_URL: http://localhost:8080/invoicextract
VITE_MAPPINGS_BASE_URL: http://localhost:8082/invoice-mapping
```

### 🗄️ Esquema de Base de Datos

La aplicación gestiona las siguientes tablas principales:

| Tabla | Descripción | Entidad Java |
|-------|-------------|--------------|
| `invoices` | Información principal de facturas | `Invoice.java` |
| `invoice_metadata` | Metadatos detallados de facturas | `InvoiceMetadata.java` |
| `email_configurations` | Configuraciones de email IMAP/SMTP | `EmailConfiguration.java` |
| `processing_error_logs` | Logs de errores de procesamiento | `ProcessingErrorLog.java` |

### 🔄 Migraciones de Base de Datos

Las migraciones se gestionan con **Liquibase** y se ejecutan automáticamente al iniciar la aplicación:

- ✅ **Changesets separados por tabla** para mejor organización
- ✅ **Definiciones iniciales** (no cambios incrementales)
- ✅ **Tipos ENUM** correctamente configurados
- ✅ **Campos de auditoría** consistentes en todas las tablas

### 🚨 Solución de Problemas

#### Problema: "Table already exists"
```bash
# Limpiar volúmenes y reiniciar desde cero
docker-compose down -v --remove-orphans
docker-compose up --build
```

#### Problema: Errores de conexión a MySQL
```bash
# Verificar que MySQL esté completamente iniciado
docker-compose logs mysql
# Esperar a ver: "ready for connections"
```

#### Problema: Kafka no se conecta
```bash
# Verificar logs de Kafka
docker-compose logs kafka
# Reiniciar solo Kafka si es necesario
docker-compose restart kafka
```

## API Documentation

The API is documented using Swagger/OpenAPI. Once the application is running, you can access the Swagger UI at the following URL:

[http://localhost:8080/invoicextract/swagger-ui/index.html](http://localhost:8080/invoicextract/swagger-ui/index.html)

### Auditing and Traceability

All primary entities (`EmailConfiguration`, `Invoice`, `InvoiceMetadata`) now include audit fields to provide full traceability for every record. These fields are automatically managed by the application and will be present in all API responses that return these entities.

- `createdDate`: The date and time when the record was created.
- `modifiedDate`: The date and time when the record was last modified.
- `createdBy`: The user who created the record (currently defaults to `api-user`).
- `modifiedBy`: The user who last modified the record (currently defaults to `api-user`).

### Email Configuration API

This API provides endpoints to securely manage the credentials (username and password) for the email service used by the application.

### Set or Update Email Credentials

Creates a new **ACTIVE** email configuration for a user. If an active configuration already exists for that user, it will be marked as **INACTIVE**. This approach preserves a history of credentials and allows for easy rollback if needed. The password is automatically encrypted before being stored.

*   **URL:** `/api/v1/config/email`
*   **Method:** `POST`
*   **Body:**

    ```json
    {
        "username": "your-email@example.com",
        "password": "your-email-password"
    }
    ```

### Get Latest Active Email Credentials

Retrieves the latest **ACTIVE** and **encrypted** credentials (username and password) for a given username. This endpoint is intended to be used by internal services, such as a Windows service, that need to authenticate with the email server. The client service is responsible for decrypting the password using the shared secret key.

*   **URL:** `/api/v1/config/email/{username}`
*   **Method:** `GET`
*   **URL Params:**
    *   `username=[string]` (Required) - The username for the email configuration to retrieve.

### Get Email Configurations by Status

Retrieves a list of email configurations for a given username, filtered by their status (`ACTIVE` or `INACTIVE`). This is useful for auditing and viewing the history of credentials.

*   **URL:** `/api/v1/config/email/filter`
*   **Method:** `GET`
*   **Query Params:**
    *   `username=[string]` (Required) - The username to filter by.
    *   `status=[string]` (Required) - The status to filter by (e.g., `ACTIVE`, `INACTIVE`).

### Security Note

The encryption and decryption processes rely on a secret key defined in the `application.yml` file under `encryption.secret-key`.

**WARNING:** For production environments, it is critical to move this key out of the configuration file and manage it securely using environment variables or a dedicated secret management service (e.g., HashiCorp Vault, AWS Secrets Manager).
