export type EstadoTurno = 'pendiente' | 'atendido' | 'cancelado' | string;

export interface Turno {
  id: number | string;
  numero: number;
  horaSolicitud: string;
  horaAtencion?: string | null;
  estado: EstadoTurno;
}

export interface SolicitarTurnoResponse {
  id: number;
  numero: number;
  horaSolicitud: string;
  estado: string;
  turnosPorDelante: number;
  tiempoEstimadoMinutos: number;
}

export interface EstadoTurnoResponse {
  numero: number;
  estado: string;
  turnoActual: number;
  turnosPorDelante: number;
  tiempoEstimadoMinutos: number;
}

export interface LlamarTurnoResponse {
  id: number;
  numero: number;
  horaAtencion: string;
  estado: string;
}

export interface MetricasFilaResponse {
  personasEsperando: number;
  totalAtendidos: number;
  totalCancelados: number;
  turnoActual: number;
  promedioEsperaMinutos: number;
}

export interface CancelarTurnoResponse {
  numero: number;
  estado: string;
  mensaje: string;
}
