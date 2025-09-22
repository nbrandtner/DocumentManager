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
git commit -m "feat(<scope>): <summary> (#<issue/PR>)"
git push origin main
```

---

## Cleaning Up Branches

After merging, remove old branches to reduce clutter:

```bash
git branch -d feature/<short-name>          # local delete
git push origin --delete feature/<short-name>   # remote delete
```