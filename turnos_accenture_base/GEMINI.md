# GEMINI.md

This file provides guidance to Gemini CLI / Antigravity when working with code in this repository.

## Working Principles

Behavioral guidelines to maximize speed and quality during this technical assessment.

### 1. Think Before Coding
- **Don't assume business logic.** If an edge case arises (like duplicate turn numbers or timestamps), adhere to the defined rules: prioritize by `hora_solicitud` (FIFO) and protect identity using unique internal IDs.
- Surface tradeoffs immediately. For a time-boxed challenge, choose the simplest robust implementation.

### 2. Simplicity First
- **Minimum code that solves the problem.** No speculative abstractions or unnecessary microservices.
- In-memory data structures (or lightweight H2) populated on startup from `turnos_hoy.csv`.
- Clean layered architecture (`Controller → Service → Repository/Store`), avoiding bloated boilerplate.

### 3. Surgical Changes
- Keep changes strictly focused on the assigned feature/issue.
- Do not refactor adjacent code or modify existing API contracts without coordination with the 5 team members.

### 4. Goal-Driven Execution
- Verify continuously:
  1. Backend unit tests passing (`./mvnw test`).
  2. Frontend compiling without TypeScript/linter warnings (`ng build`).
  3. API responding correctly (`postman collection run`).
  4. Selenium E2E flow executing green in Google Chrome.

---

## Project Overview: Fila Virtual de Turnos

An attention office aims to eliminate physical waiting lines with a digital queue management system.

### Runtime Architecture
```
backend/        # Java 17 (Eclipse Temurin) + Spring Boot 3.x REST API (Port 8080)
frontend/       # Angular 22+ (Standalone Components + Signals) (Port 4200)
postman/        # Postman Collection JSON for automated API testing via Postman CLI
selenium/       # End-to-End browser automation tests (Chrome)
data/           # Initial dataset (turnos_hoy.csv)
```

### Core User Stories
1. **📱 HU-01 (Usuario Móvil):**
   * Request a new turn (`1` to `999`).
   * See own assigned turn number, current turn being served, number of people ahead in line, and estimated wait time in minutes.
2. **💻 HU-02 (Atendedor / Ventanilla):**
   * Real-time counter of **"Personas esperando en este momento"** (count of `pendiente` turns).
   * Primary action button: **"🛎️ Llamar Siguiente Turno"** (picks oldest pending turn by `hora_solicitud`, marks as `atendido`, records `hora_atencion`).
   * Live dashboard table of turns loaded initially from `turnos_hoy.csv`.

---

## Technical Constraints & Invariants

1. **Turn Sequence & Limits:** Sequential `1` to `999`. Resets or rolls over at 999.
2. **Unique Internal Identity:** Entity `Turno` uses an auto-generated internal ID (Long/UUID) + display `numero`. This natively supports duplicate turn numbers in the dataset (such as `#7` canceled then re-requested).
3. **Strict FIFO Queue:** Ordering of pending turns is strictly by `hora_solicitud ASC` (solves chronological disorder in raw data).
4. **Estimated Wait Time Algorithm:**
   $$\text{Tiempo Estimado (min)} = \text{Personas por delante} \times \text{Tiempo Promedio Histórico (~12.6 min)}$$
5. **UI Design System:** *Modern FinTech Aurora* palette:
   * App Background: `#FAFAFC`
   * Primary Action: Indigo `#4F46E5`
   * Text: Zinc 900 `#18181B` / Secondary Zinc 500 `#71717A`
   * Badges: `pendiente` (Amber `#CA8A04` / `#FEFCE8`), `atendido` (Zinc `#52525B` / `#F4F4F5`), `cancelado` (Red `#DC2626` / `#FEF2F2`).
6. **Frontend Signals:** Exclusive use of Angular Signals (`signal()`, `computed()`) for reactive state. Avoid complex RxJS state subjects.

---

## Commands

### Root / Global
```powershell
# Run API test suite via Postman CLI
postman collection run ./postman/turnos_collection.json
```

### Backend (`backend/`)
```powershell
# Run backend development server (http://localhost:8080)
./mvnw clean spring-boot:run

# Run unit and integration tests
./mvnw test

# Run Selenium E2E test suite
./mvnw test -Dtest=*SeleniumTest*
```

### Frontend (`frontend/`)
```powershell
# Install dependencies
npm install

# Run Angular dev server (http://localhost:4200)
ng serve --open

# Production build check
ng build

# Run frontend tests
ng test
```

---

## API Contracts (`/api/turnos`)

- `POST /api/turnos/solicitar`: Issue new turn.
  - Returns: `{ id, numero, horaSolicitud, estado: "pendiente", turnosPorDelante, tiempoEstimadoMinutos }`
- `GET /api/turnos/{numero}/estado`: User tracking endpoint.
  - Returns: `{ numero, estado, turnoActual, turnosPorDelante, tiempoEstimadoMinutos }`
- `POST /api/turnos/llamar-siguiente`: Clerk call next button.
  - Returns: `{ id, numero, horaAtencion, estado: "atendido" }`
- `GET /api/turnos/metricas-fila`: Clerk header KPIs.
  - Returns: `{ personasEsperando, totalAtendidos, totalCancelados, turnoActual, promedioEsperaMinutos }`
- `GET /api/turnos`: List all turns for table.
