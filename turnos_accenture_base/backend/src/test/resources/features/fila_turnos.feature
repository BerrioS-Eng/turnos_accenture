# language: es
@api
Característica: Fila virtual de turnos (API)
  Regla del caso: los turnos se atienden en orden de llegada, los números no se repiten
  y no se puede llamar al siguiente si la fila está vacía.

  Antecedentes:
    Dado que la fila tiene los turnos registrados en la mañana

  # ---------- Carga de datos y métricas ----------

  Escenario: Las métricas reflejan los turnos cargados de la mañana
    Entonces hay 5 personas esperando
    Y hay 5 atendidos y 1 cancelado
    Y el turno actual en ventanilla es el 5
    Y la lista general tiene 11 turnos

  Escenario: La espera promedio se calcula con los turnos atendidos
    Entonces la espera promedio es 12,2 minutos

  # ---------- Orden FIFO (HU-02) ----------

  Escenario: El asesor llama primero al que llegó primero
    Cuando el asesor llama al siguiente turno
    Entonces se llama el turno 6
    Y hay 4 personas esperando
    Y el turno actual en ventanilla es el 6

  Escenario: La fila sigue la hora de solicitud y no el orden de las filas del archivo
    Cuando el asesor llama al siguiente turno 5 veces
    Entonces los turnos llamados son 6, 7, 8, 9 y 10
    Y hay 0 personas esperando

  Escenario: Un turno cancelado nunca se llama
    Cuando el asesor llama al siguiente turno 5 veces
    Y el asesor llama al siguiente turno
    Entonces el sistema rechaza la llamada porque la fila está vacía

  Escenario: No se puede llamar al siguiente si la fila está vacía
    Dado que la fila está vacía
    Cuando el asesor llama al siguiente turno
    Entonces el sistema rechaza la llamada porque la fila está vacía
    Y hay 0 personas esperando

  @concurrencia
  Escenario: Dos asesores que llaman al mismo tiempo no reciben el mismo turno
    Cuando 2 asesores llaman al siguiente turno al mismo tiempo
    Entonces se llaman los turnos 6 y 7 sin repetir
    Y hay 3 personas esperando

  # ---------- Pedir turno (HU-01) ----------

  Escenario: Un turno nuevo continúa la numeración y queda al final de la fila
    Cuando un cliente pide un turno
    Entonces recibe el turno 11 en estado pendiente
    Y tiene 5 turnos por delante
    Y el tiempo estimado es de unos 61 minutos

  Escenario: Los números no se repiten
    Cuando 3 clientes piden turno
    Entonces reciben los turnos 11, 12 y 13
    Y hay 8 personas esperando

  Escenario: Después del 999 la numeración vuelve a empezar en 1
    Dado que el último turno emitido fue el 999
    Cuando un cliente pide un turno
    Entonces recibe el turno 1 en estado pendiente

  Escenario: El cliente consulta cuánto le falta
    Cuando el cliente consulta el estado del turno 9
    Entonces tiene 3 turnos por delante
    Y ve que en ventanilla están atendiendo el 5

  Escenario: Al llamar un turno, los demás avanzan en la fila
    Cuando el asesor llama al siguiente turno
    Y el cliente consulta el estado del turno 9
    Entonces tiene 2 turnos por delante
    Y ve que en ventanilla están atendiendo el 6
