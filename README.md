# InvoiceExtract Application

This project is a Spring Boot application for managing invoices. It provides a RESTful API for creating, retrieving, updating, and deleting invoices.

## Running the Application

The application is containerized using Docker and can be run with Docker Compose.

1.  **Prerequisites:**
    *   Docker
    *   Docker Compose

2.  **Build and Run:**
    Navigate to the project's root directory and run the following command:
    ```sh
    docker-compose up --build
    ```

## API Documentation

The API is documented using Swagger/OpenAPI. Once the application is running, you can access the Swagger UI at the following URL:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

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
