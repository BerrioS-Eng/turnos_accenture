# 🏛️ WORKFLOW SPEC & BLUEPRINT TÉCNICO
> **Proyecto:** Fila Virtual de Turnos (Eliminar Filas Físicas)  
> **Candidato:** Alan Moreno  
> **Fecha:** 29 de Octubre de 2026  
> **Stack:** Java 17 (Spring Boot 3) | Angular | Selenium WebDriver | Postman CLI | GitHub

---

## 1. Misión del Reto & Historias de Usuario (HU)

Una oficina de atención busca **eliminar las filas físicas** implementando una solución de autoservicio digital y gestión de ventanilla.

### 📱 HU-01: Vista Usuario (Celular / Móvil)
> **"Como usuario, quiero pedir turno desde mi celular y ver cuánto falta para ser atendido."**
* **Criterios de Aceptación:**
  1. El usuario puede solicitar un turno con un clic desde una interfaz optimizada para móvil.
  2. El sistema le asigna un número de turno secuencial (rango `1` a `999`).
  3. La pantalla muestra en tiempo real:
     * **Su número de turno asignado** (ej. Turno `#11`).
     * **Turno actualmente en ventanilla** (ej. Atendiendo `#5`).
     * **Personas por delante** en la fila (ej. *5 personas antes de ti*).
     * **Tiempo estimado de espera:** Calculado con base en la métrica histórica del CSV ($5 \text{ personas} \times \approx 12.6 \text{ min} = \sim 63 \text{ min}$).

### 💻 HU-02: Vista Atendedor (Ventanilla / Oficina)
> **"Como atendedor, quiero llamar los turnos y ver cuántas personas están esperando."**
* **Criterios de Aceptación:**
  1. Contador destacado y en tiempo real: **"Personas esperando en este momento"** (suma de turnos en estado `pendiente`).
  2. Botón principal y destacado: **"🛎️ Llamar Siguiente Turno"**.
  3. Al presionar el botón:
     * El sistema selecciona el turno más antiguo por `hora_solicitud` (política FIFO estricta).
     * Cambia su estado a `atendido` (o `en_atencion`).
     * Registra la `hora_atencion` con la hora del sistema.
     * Decrementa automáticamente el contador de personas en espera.
  4. Tabla detallada con el historial de turnos de la jornada (cargados inicialmente desde `turnos_hoy.csv`).

---

## 2. Reglas de Negocio & Casos Borde Confirmados

1. **Rango de Turnos:**
   * Numeración secuencial de `1` a `999`. Al llegar a 999 se reinicia el contador de emisión.
2. **Identificador Compuesto / Único:**
   * Dado que en el CSV el turno `#7` aparece duplicado (uno cancelado y otro pendiente posterior), la base de datos/entidad utiliza un **ID único interno (UUID o Long autogenerado)** + el campo visible `numero`. Esto permite que el número de turno se repita a lo largo del tiempo sin violar restricciones de clave primaria.
3. **Prioridad FIFO Real:**
   * La cola de pendientes se atiende ordenando por `hora_solicitud ASC` (solucionando el desorden del turno `#9` en el CSV).
4. **Cálculo de Tiempos:**
   * $\text{Tiempo de Espera} = \text{hora\_atencion} - \text{hora\_solicitud}$ (para turnos atendidos).
   * $\text{Tiempo Estimado Restante} = (\text{Personas por delante}) \times (\text{Promedio de Espera Histórico})$.

---

## 3. Contratos de API REST (Spring Boot)

| Método | Endpoint | Descripción | Body / Respuesta |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/turnos/solicitar` | **(Móvil)** Solicita un nuevo turno | Responde: `{ "id": 12, "numero": 11, "horaSolicitud": "09:00:00", "estado": "pendiente", "turnosPorDelante": 5, "tiempoEstimadoMinutos": 63 }` |
| `GET` | `/api/turnos/{numero}/estado` | **(Móvil)** Consulta estado y cuánto falta | Responde: `{ "numero": 11, "estado": "pendiente", "turnoActual": 5, "turnosPorDelante": 4, "tiempoEstimadoMinutos": 50 }` |
| `POST` | `/api/turnos/llamar-siguiente` | **(Atendedor)** Llama el siguiente turno FIFO | Responde: `{ "numero": 6, "horaAtencion": "08:52:10", "estado": "atendido" }` |
| `GET` | `/api/turnos/metricas-fila` | **(Atendedor)** Métricas en vivo | Responde: `{ "personasEsperando": 5, "totalAtendidos": 5, "totalCancelados": 1, "turnoActual": 5, "promedioEsperaMinutos": 12.6 }` |
| `GET` | `/api/turnos` | **(General)** Lista completa para la tabla | Responde: `Array<Turno>` |

---

## 4. UI / UX: Sistema de Diseño *"Modern FinTech (Aurora)"*

Diseño dual responsive que permite alternar fácilmente entre **Vista Usuario Móvil** y **Vista Atendedor**.

### Paleta de Tokens CSS
```css
:root {
  --bg-app: #FAFAFC;                /* Blanco con tinte tenue violeta/azul */
  --surface-card: #FFFFFF;
  --surface-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.05);
  --surface-border: #F1F1F4;

  --text-primary: #18181B;          /* Zinc 900 */
  --text-secondary: #71717A;        /* Zinc 500 */

  --primary: #4F46E5;               /* Indigo 600: Botones de llamada y solicitud */
  --primary-hover: #4338CA;
  --primary-subtle: #EEF2FF;        /* Fondo activo */

  /* Semántica de Turnos */
  --badge-pending-text: #CA8A04;    /* Ámbar: En espera */
  --badge-pending-bg: #FEFCE8;

  --badge-active-text: #4F46E5;     /* Índigo: Siendo llamado */
  --badge-active-bg: #EEF2FF;

  --badge-done-text: #52525B;       /* Zinc: Finalizado / Atendido */
  --badge-done-bg: #F4F4F5;

  --badge-cancel-text: #DC2626;     /* Rojo sutil: Cancelado */
  --badge-cancel-bg: #FEF2F2;
}
```

### Layout de Vistas
```text
[ Switch Superior:  (🔘 Vista Celular Usuario)  |  (⚪ Vista Atendedor Ventanilla) ]

=== VISTA 1: CELULAR USUARIO ===                === VISTA 2: ATENDEDOR ===
+------------------------------------+          +--------------------------------------------+
|  🏛️ MI TURNO EN LÍNEA              |          |  🏛️ PANEL DE ATENCIÓN                      |
|                                    |          |                                            |
|  [ + PEDIR NUEVO TURNO ]           |          |  [ 👥 PERSONAS ESPERANDO: 5 ]              |
|                                    |          |  [ 🛎️ LLAMAR SIGUIENTE TURNO ]            |
|  Tu Turno Asignado:                |          |                                            |
|  +------------------------------+  |          |  Turno actual en ventanilla: #5            |
|  |           #11                |  |          |  Tiempo promedio de atención: 12.6 min     |
|  +------------------------------+  |          |                                            |
|                                    |          |  TABLA DE CONTROL EN VIVO:                 |
|  • Turno en ventanilla: #5         |          |  Turno | Solicitud | Atención | Estado     |
|  • Personas antes de ti: 5         |          |  #1    | 08:02:00  | 08:05:00 | Atendido   |
|  • Tiempo estimado: ~63 min        |          |  #6    | 08:25:00  | --:--:-- | En espera  |
+------------------------------------+          +--------------------------------------------+
```

---

## 5. Estrategia de Pruebas Automatizadas

1. **Backend (JUnit 5 + Mockito):**
   * Carga del archivo `turnos_hoy.csv`.
   * Verificación de asignación del número consecutivo hasta 999.
   * Aserción del orden FIFO al llamar el siguiente turno.
2. **API Testing (Postman CLI):**
   * Colección `turnos_collection.json` ejecutada desde terminal:
     ```powershell
     postman collection run ./turnos_collection.json
     ```
   * Verificación de códigos HTTP `200 OK` y contratos JSON.
3. **Pruebas E2E (Selenium WebDriver):**
   * Apertura en Google Chrome (`http://localhost:4200`).
   * Simulación del usuario pidiendo un turno.
   * Simulación del atendedor viendo el incremento en "Personas esperando" y presionando "Llamar siguiente turno".
   * Aserción de actualización del DOM.

---

## 6. Flujo de Entrega Git & GitHub

1. Rama de desarrollo: `feature/fila-virtual-turnos`.
2. Commits atómicos con Conventional Commits (`feat(backend): ...`, `feat(frontend): ...`, `test: ...`).
3. Creación de Pull Request hacia `main` usando la plantilla: [PULL_REQUEST_TEMPLATE_ASSESSMENT.md](file:///c:/Users/alanm/Documents/PULL_REQUEST_TEMPLATE_ASSESSMENT.md).
