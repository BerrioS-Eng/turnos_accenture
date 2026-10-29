-- Turnos de la mañana (copia de turnos_hoy.csv, mismo orden de filas: el 9 va antes del 7 y el 8).
-- ÚNICO punto acoplado a la tabla: si el back cambia nombres de columnas o el formato
-- de estados/horas en la entidad Turno, se ajusta solo este archivo.
INSERT INTO turno (numero, hora_solicitud, hora_atencion, estado) VALUES
 (1,  '08:02:00', '08:05:00', 'atendido'),
 (2,  '08:06:00', '08:15:00', 'atendido'),
 (3,  '08:10:00', '08:24:00', 'atendido'),
 (4,  '08:15:00', '08:31:00', 'atendido'),
 (5,  '08:21:00', '08:40:00', 'atendido'),
 (6,  '08:25:00', NULL,       'pendiente'),
 (9,  '08:41:00', NULL,       'pendiente'),
 (7,  '08:30:00', NULL,       'cancelado'),
 (7,  '08:34:00', NULL,       'pendiente'),
 (8,  '08:38:00', NULL,       'pendiente'),
 (10, '08:45:00', NULL,       'pendiente');
