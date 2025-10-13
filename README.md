# Document Manager – Setup Guide

This project is a **Java 21 / Spring Boot 3** REST service with **PostgreSQL** persistence and a **React + Vite + Tailwind** frontend.  
It can be run in **two modes**:

- **Development**: backend in Docker, frontend locally with Vite (hot reload + proxy).
- **Production**: full stack in Docker Compose (frontend built and served by Nginx).

---

## Prerequisites

Make sure you have the following installed:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Node.js](https://nodejs.org/) **20.19.0** (or via [NVM for Windows](https://github.com/coreybutler/nvm-windows))
- [Git](https://git-scm.com/)

---

## Run – Development Mode (recommended while coding)

1. Start only backend + database with Docker Compose:

   ```powershell
   docker compose up db app
   ```
   This starts:

      - app → Spring Boot backend at http://localhost:8080

      - db → PostgreSQL database at localhost:5432

2. Install frontend dependencies (first time only):
   ```powershell
      cd frontend
      npm install
   ```
3. Start Vite dev server (with proxy to backend):
   ```powershell
      npm run dev
   ```
   Open browser → http://localhost:5173
   API calls to /api/... are proxied to backend at http://localhost:8080.

4. Stop backend stack:
    ```powershell
       docker compose down
    ```
---
## Run – Production Mode (full Docker stack)
This runs frontend + backend + DB + Nginx together in Docker.
1. Build and start all services:
   ```powershell
   docker compose up --build -d
   ```
   Running containers:

      - document-manager-app → Spring Boot backend (http://localhost:8080)

      - paperless-db → PostgreSQL (localhost:5432)

      - document-manager-frontend → frontend build stage

      - document-manager-nginx → serves frontend + proxies /api to backend (http://localhost)

2. Open in browser:
   http://localhost

3. Stop stack:
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

---
# Git Workflow Guide

This guide describes a practical Git workflow for feature development.  
It emphasizes clear commit history, frequent syncing with `main`, and safe cleanup after merging.

---

## Getting Started

Always begin from the latest `main` branch:

```bash
git checkout main
git pull origin main
```

---

## Create a Feature Branch

Branches are used for each feature or bugfix:

```bash
git checkout -b feature/<short-name>   # e.g., feature/login-form
git push -u origin feature/<short-name>   # publish branch (initial push)
```

Use `feature/`, `bugfix/`, or `chore/` prefixes for clarity.

---

## Working on the Branch

Make small, focused commits.

```bash
git add .
git commit -m "add login form UI"
```

Push changes regularly to keep your branch backed up:

```bash
git push
```

---

## Keeping Your Branch Updated

If `main` has advanced since you started, rebase your branch before finishing:

```bash
git checkout main
git pull origin main

git checkout feature/<short-name>
git rebase main   # keeps history linear
```

If conflicts occur:

```bash
# fix conflicts in files
git add <resolved-file>
git rebase --continue
```

Abort rebase if necessary:

```bash
git rebase --abort
```

After rebasing, force-push safely:

```bash
git push --force-with-lease
```

---

## Merging Your Work

### Option A – Fast-forward (preferred if rebased)

```bash
git checkout main
git pull origin main
git merge --ff-only feature/<short-name>
git push origin main
```

### Option B – Squash merge (condenses history)

```bash
git checkout main
git pull origin main
git merge --squash feature/<short-name>
git commit -m "squash merge feature/<short-name>"
git push origin main
```

---

## Cleaning Up Branches

After merging, remove old branches to reduce clutter:

```bash
git branch -d feature/<short-name>          # local delete
git push origin --delete feature/<short-name>   # remote delete
```



### Creating the RabbitMQ user:
docker compose up -d rabbitmq

docker compose exec rabbitmq rabbitmqctl add_user appuser "supersecret123" 2>$null `
  ; if ($LASTEXITCODE -ne 0) { docker compose exec rabbitmq rabbitmqctl change_password appuser "supersecret123" }

docker compose exec rabbitmq rabbitmqctl set_user_tags appuser administrator

docker compose exec rabbitmq rabbitmqctl set_permissions -p / appuser ".*" ".*" ".*"

# Then check if it worked:
docker compose exec rabbitmq rabbitmqctl list_users

docker compose exec rabbitmq rabbitmqctl list_permissions -p /
