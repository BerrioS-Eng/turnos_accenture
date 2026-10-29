# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Working Principles

Behavioral guidelines to maximize velocity and code quality in this team assessment.

### 1. Think Before Coding
- **Don't assume business logic.** Strictly respect confirmed invariants:
  - Sequence up to 999.
  - Priority queue by `hora_solicitud ASC` (true FIFO).
  - Internal unique ID to prevent collisions on duplicate numbers (e.g. `#7`).
- Keep code lean and direct. Time is of the essence.

### 2. Simplicity First
- **Minimum code that solves the problem.** No over-engineering.
- In-memory data store with CSV seeding at startup (`turnos_hoy.csv`).
- Clear 3-tier backend: `TurnoController → TurnoService → TurnoRepository`.
- Angular Standalone components with Signals for clean, low-overhead reactivity.

### 3. Surgical Changes
- 5 developers are working concurrently on separate branches.
- Do not introduce breaking changes to shared DTOs or endpoints without updating team contracts.
- Strictly adhere to Conventional Commits: `feat(backend): ...`, `feat(frontend): ...`, `test(qa): ...`.

### 4. Goal-Driven Execution
- Verify continuously before marking issues or pull requests as ready:
  1. Unit tests pass: `./mvnw test`
  2. Frontend build succeeds: `ng build`
  3. API endpoints verified: `postman collection run ./postman/turnos_collection.json`
  4. Selenium E2E flow runs clean in Chrome.

---

## Project Overview: Fila Virtual de Turnos

A customer service office queue management system designed to eliminate physical waiting lines.

### Monorepo Structure
```
backend/        # Java 17 (Eclipse Temurin) + Spring Boot 3.x REST API (Port 8080)
frontend/       # Angular 22+ (Standalone Components + Signals) (Port 4200)
postman/        # Postman Collection JSON for automated CLI verification
selenium/       # End-to-End browser tests (Chrome)
data/           # Initial dataset (turnos_hoy.csv)
```

### Core User Stories
1. **📱 HU-01 (Usuario Móvil):**
   * Request a turn (`1` to `999`).
   * Track: assigned turn, current turn at desk, number of people ahead, and estimated wait time.
2. **💻 HU-02 (Atendedor / Ventanilla):**
   * Real-time counter of **"Personas esperando en este momento"** (`pendiente` count).
   * Primary action button: **"🛎️ Llamar Siguiente Turno"** (FIFO by `hora_solicitud`).
   * Live dashboard table of turns seeded from `turnos_hoy.csv`.

---

## Technical Constraints & Invariants

1. **Turn Sequence & Limits:** `1` to `999`. Rolls over cleanly to 1 upon reaching 999.
2. **Unique Internal Identity:** Entity `Turno` uses internal unique ID (`Long`/`UUID`) + display `numero`. This natively supports duplicate turn numbers in the dataset.
3. **Strict FIFO Queue:** Order pending turns strictly by `hora_solicitud ASC` (solves chronological disorder in raw data).
4. **Estimated Wait Time Algorithm:**
   $$\text{Tiempo Estimado (min)} = \text{Personas por delante} \times \text{Tiempo Promedio Histórico (~12.6 min)}$$
5. **UI Design System:** *Modern FinTech Aurora* palette:
   * App Background: `#FAFAFC`
   * Primary Action: Indigo `#4F46E5`
   * Text: Zinc 900 `#18181B` / Secondary Zinc 500 `#71717A`
   * Badges: `pendiente` (Amber `#CA8A04` / `#FEFCE8`), `atendido` (Zinc `#52525B` / `#F4F4F5`), `cancelado` (Red `#DC2626` / `#FEF2F2`).
6. **Frontend State:** Exclusively use Angular Signals (`signal()`, `computed()`). No legacy `BehaviorSubject` for component state.

---

## Commands

### Global
```powershell
# Run API test suite via Postman CLI
postman collection run ./postman/turnos_collection.json
```

### Backend (`backend/`)
```powershell
# Start Spring Boot API (http://localhost:8080)
./mvnw clean spring-boot:run

# Run unit tests
./mvnw test

# Run Selenium E2E suite
./mvnw test -Dtest=*SeleniumTest*
```

### Frontend (`frontend/`)
```powershell
# Install packages
npm install

# Start Angular dev server (http://localhost:4200)
ng serve --open

# Check production compilation
ng build

# Run unit tests
ng test
```

---

## API Endpoints (`/api/turnos`)

- `POST /api/turnos/solicitar`:
  - Request: `{}` (or optional customer metadata).
  - Response: `{ id, numero, horaSolicitud, estado: "pendiente", turnosPorDelante, tiempoEstimadoMinutos }`
- `GET /api/turnos/{numero}/estado`:
  - Response: `{ numero, estado, turnoActual, turnosPorDelante, tiempoEstimadoMinutos }`
- `POST /api/turnos/llamar-siguiente`:
  - Response: `{ id, numero, horaAtencion, estado: "atendido" }`
- `GET /api/turnos/metricas-fila`:
  - Response: `{ personasEsperando, totalAtendidos, totalCancelados, turnoActual, promedioEsperaMinutos }`
- `GET /api/turnos`:
  - Response: `Array<Turno>` (list all records for desk table)
