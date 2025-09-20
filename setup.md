# Document Manager – Setup Guide

This project is a **Java 21 / Spring Boot 3** REST service with PostgreSQL for persistence.  
Sprint 1 delivers a minimal **Document** API with create/list/get endpoints.

## Run with Docker Compose

Port `8080` (app) and `5432` (DB) must be free on your machine. Open Docker Desktop before starting.

1. Build & start containers:

   ```powershell
   docker compose build
   docker compose up
   ```

2. Containers started:

    - **document-manager-app** → Spring Boot service on `http://localhost:8080`
    - **paperless-db** → PostgreSQL on `localhost:5432`

3. Stop the stack:

   ```powershell
   docker compose down
   ```

---

## API Endpoints

### Create a document

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/documents" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"originalFilename":"hello.pdf","contentType":"application/pdf","size":321}'
```

### List all documents

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/documents" -Method GET | ConvertTo-Json -Depth 5
```

### Get a document by ID

```powershell
$id = "<replace-with-your-id>"
Invoke-RestMethod -Uri "http://localhost:8080/api/documents/$id" -Method GET | ConvertTo-Json -Depth 5
```

---

## Sprint 1 Deliverables

- REST API for **Document** entity (create/list/get)
- Persistence via **PostgreSQL (JPA repository)**
- **Flyway migration** for schema creation
- Docker Compose stack (REST + DB)
- Unit tests with **mocked repository/service**  