<!--
  =============================================================================
  TECHNICAL ASSESSMENT / PULL REQUEST TEMPLATE
  Estándar de Entrega Técnica: Clean Code | SOLID | Testing Pyramid
  =============================================================================
-->

## 1. Resumen Ejecutivo (Summary)

<!-- 
Proporcione una descripción concisa de la solución implementada para el reto técnico.
Explique el objetivo principal, el alcance cubierto y el valor entregado.
-->
- **Objetivo del Reto:** 
- **Solución Implementada:** 
- **Rama:** `feature/` | **Rama Destino:** `main` (o `develop`)

---

## 2. Requerimientos Técnicos Cumplidos

- [ ] **Procesamiento de Datos:** Ingesta y validación del archivo de turnos (`.csv`).
- [ ] **Backend (Spring Boot):** API REST con arquitectura en capas (Controller, Service, Repository/Domain).
- [ ] **Frontend (Angular):** Interfaz interactiva y reactiva para visualización y gestión de la cola de turnos.
- [ ] **Pruebas de Integración / API (Postman):** Colección parametrizada y ejecutable vía Postman CLI.
- [ ] **Pruebas End-to-End (Selenium WebDriver):** Automatización de flujos críticos sobre el navegador.
- [ ] **Control de Versiones (Git/GitHub):** Historial atómico con Conventional Commits.

---

## 3. Tipo de Cambio (Type of Change)

- [ ] `feat`: Nueva funcionalidad (endpoints, componentes de UI, lógica de cola)
- [ ] `fix`: Corrección de defecto o caso borde en el procesamiento de datos
- [ ] `refactor`: Mejora de arquitectura o simplificación de código sin alterar comportamiento
- [ ] `test`: Incorporación de pruebas unitarias (JUnit/Mockito), Postman o Selenium
- [ ] `perf`: Optimización de rendimiento o consultas
- [ ] `docs`: Documentación técnica, diagramas o instrucciones del README

---

## 4. Cambios Realizados por Componente (Changes Breakdown)

### Backend (`Spring Boot / Java 17+`)
- **Controladores / Endpoints:**
  - `GET /api/...`: 
  - `POST /api/...`: 
  - `PUT /api/...`: 
- **Lógica de Negocio / Servicios:**
  - Ingesta y parseo resiliente de datos desde archivo delimitado.
  - Gestión de cola de atención y cálculo de métricas (tiempos de espera).
- **Manejo Global de Excepciones:** Respuestas estandarizadas (`ProblemDetail` / `@ControllerAdvice`).

### Frontend (`Angular`)
- **Componentes:**
  - Visualización tabular y badges de estado (`atendido`, `pendiente`, `cancelado`).
  - Panel de control para llamado y gestión del siguiente turno.
- **Servicios & Estado:**
  - Consumo reactivo de la API mediante `HttpClient` (Observables / Signals).
- **UX & Feedback:** Notificaciones de éxito, error y confirmación de acciones.

### Pruebas Automatizadas
- **Postman:** Colección de pruebas de contrato y validación de esquemas HTTP.
- **Selenium WebDriver:** Automatización E2E del flujo de atención de turnos en navegador.

---

## 5. Decisiones de Diseño y Manejo de Casos Borde (Edge Cases)

<!--
Justifique las decisiones clave tomadas para garantizar la robustez del sistema frente a irregularidades de los datos.
-->
- **Desorden Cronológico en Ingesta:**
  - *Decisión:* La cola de atención se prioriza estrictamente por `hora_solicitud` (política FIFO real) y no por el orden físico de las líneas del archivo ni por el número secuencial.
- **No Unicidad del Identificador de Turno:**
  - *Decisión:* Manejo de identificador interno único independiente del número impreso del turno (permite soportar reemisiones o turnos cancelados con el mismo número).
- **Tolerancia a Nulos (`hora_atencion` vacía):**
  - *Decisión:* Parseo seguro y representación en modelos mediante tipos opcionales (`Optional<LocalTime>` / `null`) evitando excepciones de deserialización.
- **Cálculo de Tiempos de Espera:**
  - *Decisión:* Uso de `java.time.Duration` para cálculo exacto de tiempos promedio y descarte automático de turnos no atendidos en el indicador.

---

## 6. Guía Rápida de Ejecución Local (Reproduction Steps)

### Prerrequisitos
- **Java:** JDK 17+ (`JAVA_HOME` configurado)
- **Node.js:** v20+ / npm v10+
- **Angular CLI:** `ng` disponible
- **Navegador:** Google Chrome o Microsoft Edge

### Comandos de Puesta en Marcha

```powershell
# 1. Iniciar Backend (Spring Boot)
cd backend
./mvnw clean spring-boot:run

# 2. Iniciar Frontend (Angular)
cd frontend
npm install
ng serve --open

# 3. Ejecutar Pruebas Automatizadas con Postman CLI
postman collection run ./postman/turnos_collection.json

# 4. Ejecutar Pruebas E2E de Selenium WebDriver
# (Desde suite de pruebas de Java o script configurado)
./mvnw test -Dtest=*SeleniumTest*
```

---

## 7. Evidencias de Prueba (Verification & Testing)

| Tipo de Prueba | Herramienta | Resultado | Evidencia / Notas |
| :--- | :---: | :---: | :--- |
| **Pruebas Unitarias** | JUnit 5 / Mockito | [ ] PASS | Cobertura en servicios de dominio |
| **Pruebas de API** | Postman CLI | [ ] PASS | Validaciones de status 200, 201, 400 |
| **Pruebas E2E** | Selenium WebDriver | [ ] PASS | Flujo completo en navegador |
| **Compilación Frontend** | Angular CLI (`ng build`) | [ ] PASS | 0 errores de TypeScript / linter |

---

## 8. Checklist de Calidad (Quality Gate Pre-Merge)

- [ ] El código compila limpiamente sin advertencias críticas.
- [ ] Se aplican principios **SOLID** y separación de responsabilidades.
- [ ] No existen credenciales, claves API ni secretos expuestos en el código.
- [ ] Las dependencias utilizadas son compatibles con Java 17 y Node.js.
- [ ] Los mensajes de commit respetan el estándar de **Conventional Commits**.
- [ ] Se incluye documentación en el `README.md` con instrucciones de reproducción.

---

## 9. Autor & Información de Entrega

- **Candidato / Desarrollador:** Alan Moreno
- **Email:** alanmoreno1412@gmail.com
- **Fecha de Entrega:** 29 de Octubre de 2026
