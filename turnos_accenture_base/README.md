# Fila virtual de turnos

Una oficina de atención quiere acabar con las filas: el usuario pide turno desde el celular y ve cuánto le falta; el atendedor llama al siguiente y ve cuántas personas esperan.

**Regla del caso:** los turnos se atienden en orden de llegada, los números no se repiten y no se puede llamar al siguiente si la fila está vacía.

## Estructura

| Ruta | Contenido |
|---|---|
| `backend/` | API en Spring Boot 3 (Java 17) con SQLite local |
| `backend/src/main/resources/data/turnos_hoy.csv` | Turnos de la mañana, se cargan al iniciar |
| `backend/src/test/` | Pruebas: Cucumber en español + REST Assured + Selenium, con reporte HTML |
| `turnos_collection.json` | Colección de Postman para probar el contrato de la API |
| `frontend/` | (Angular) |

## Cómo correr

### Back
```bash
cd backend
mvn spring-boot:run          # http://localhost:8080 · Swagger en /swagger-ui.html
```

### Pruebas
```bash
cd backend
mvn test                                         # pruebas de API (levantan la app sola en el 8080)
mvn test -Dcucumber.filter.tags="@ui"            # pantallas con Selenium (front corriendo en 4200)
mvn test -Dcucumber.filter.tags="@api or @ui"    # todo
postman collection run ./turnos_collection.json  # contrato de la API (desde la raíz, con el back corriendo)
```
- Reporte de Cucumber: `backend/target/cucumber-report.html`. Las pruebas de pantalla adjuntan una captura final.
- Antes de `mvn test`, detener el back si está corriendo: las pruebas levantan la aplicación en el 8080 con su propia base (`target/turnos-test.db`).
- Opciones: `-Dfront.url=http://localhost:4200 -Dheadless=true`.

## Contrato de la API

| Método | Ruta | Respuesta |
|---|---|---|
| `POST` | `/api/turnos/solicitar` | `{"id", "numero": 11, "horaSolicitud": "09:00:00", "estado": "pendiente", "turnosPorDelante": 5, "tiempoEstimadoMinutos": 61}` |
| `GET` | `/api/turnos/{numero}/estado` | `{"numero", "estado", "turnoActual": 5, "turnosPorDelante": 4, "tiempoEstimadoMinutos"}` |
| `POST` | `/api/turnos/llamar-siguiente` | `{"numero": 6, "horaAtencion": "08:52:10", "estado": "atendido"}` · fila vacía → `409` |
| `GET` | `/api/turnos/metricas-fila` | `{"personasEsperando": 5, "totalAtendidos": 5, "totalCancelados": 1, "turnoActual": 5, "promedioEsperaMinutos": 12.2}` |
| `GET` | `/api/turnos` | Lista de todos los turnos |

## Contrato con el front (Selenium)
Rutas `/usuario` y `/atendedor`. Los elementos se ubican por `data-testid`:

| Pantalla | `data-testid` |
|---|---|
| Usuario | `btn-solicitar-turno`, `turno-asignado`, `turnos-por-delante`, `tiempo-estimado` |
| Atendedor | `btn-llamar-siguiente`, `personas-esperando`, `turno-actual`, `tabla-turnos`, `fila-turno` (cada fila), `mensaje-error` |

## Qué prueban los escenarios
- **Carga y métricas:** 11 turnos cargados, 5 esperando, 5 atendidos, 1 cancelado, en ventanilla el 5, espera promedio 12,2 min.
- **FIFO real:** se llama por hora de solicitud, no por orden del archivo (el 9 aparece antes que el 7 y el 8): 6 → 7 → 8 → 9 → 10.
- **Cancelados:** el 7 cancelado nunca se llama; tras 5 llamados la fila queda vacía.
- **Fila vacía:** llamar al siguiente se rechaza.
- **Concurrencia:** dos atendedores que llaman a la vez no reciben el mismo turno.
- **Numeración:** los turnos nuevos siguen en 11, 12, 13; después del 999 vuelve a 1.
- **Cuánto falta:** turnos por delante, turno actual y tiempo estimado (5 × 12,2 ≈ 61 min).

## Decisiones
- **SQLite local:** corre en cualquier computador sin instalar un servidor de base de datos.
- **ID interno + `numero` visible:** el archivo trae el 7 dos veces (cancelado y pendiente) y la numeración se reinicia en 999, así que `numero` no puede ser la clave primaria.
- **FIFO por `hora_solicitud`:** el orden de las filas del archivo no es el de llegada.
- **Espera promedio:** solo con atendidos (`hora_atencion − hora_solicitud`). Con los datos de la mañana: (3+9+14+16+19)/5 = **12,2 min**.
- **Pruebas independientes:** antes de cada escenario la base se recarga con los turnos de la mañana (`backend/src/test/resources/qa/datos_manana.sql`), así cada prueba arranca del mismo estado.
- **Lógica por API, recorrido por Selenium:** la regla del caso se verifica en el servicio, donde vive; Selenium cubre lo que hace el usuario en pantalla.
