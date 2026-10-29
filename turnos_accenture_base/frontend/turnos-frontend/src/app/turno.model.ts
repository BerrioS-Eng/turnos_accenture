export type EstadoTurno = 'pendiente' | 'atendido' | 'cancelado' | string;

export interface Turno {
  id: number | string;
  numero: number;
  horaSolicitud: string;
  horaAtencion?: string | null;
  estado: EstadoTurno;
}
