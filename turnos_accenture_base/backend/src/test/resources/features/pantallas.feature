# language: es
@ui
Característica: Pantallas del usuario y del atendedor (Selenium)
  Requiere el front corriendo en http://localhost:4200 apuntando al back en el puerto 8080.

  Antecedentes:
    Dado que la fila tiene los turnos registrados en la mañana

  Escenario: El usuario pide turno desde el celular y ve cuánto le falta
    Cuando el cliente abre la pantalla para pedir turno
    Y pide un turno
    Entonces ve que le asignaron el turno 11
    Y ve que tiene 5 personas delante
    Y ve el tiempo estimado de espera

  Escenario: El atendedor ve la fila y llama al siguiente
    Cuando el asesor abre su pantalla
    Entonces ve 5 personas esperando
    Y ve la tabla con 11 turnos
    Cuando llama al siguiente turno desde la pantalla
    Entonces ve el turno 6 en ventanilla
    Y ve 4 personas esperando

  Escenario: El atendedor no puede llamar al siguiente con la fila vacía
    Dado que la fila está vacía
    Cuando el asesor abre su pantalla
    Y llama al siguiente turno desde la pantalla
    Entonces ve un mensaje de fila vacía
    Y ve 0 personas esperando
