import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Turno,
  SolicitarTurnoResponse,
  EstadoTurnoResponse,
  LlamarTurnoResponse,
  MetricasFilaResponse,
  CancelarTurnoResponse
} from './turno.model';

@Injectable({ providedIn: 'root' })
export class TurnosService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = '/api/turnos';

  getTurnos(): Observable<Turno[]> {
    return this.http.get<Turno[]>(this.endpoint);
  }

  solicitarTurno(): Observable<SolicitarTurnoResponse> {
    return this.http.post<SolicitarTurnoResponse>(`${this.endpoint}/solicitar`, {});
  }

  getEstado(numero: number): Observable<EstadoTurnoResponse> {
    return this.http.get<EstadoTurnoResponse>(`${this.endpoint}/${numero}/estado`);
  }

  llamarSiguiente(): Observable<LlamarTurnoResponse> {
    return this.http.post<LlamarTurnoResponse>(`${this.endpoint}/llamar-siguiente`, {});
  }

  getMetricas(): Observable<MetricasFilaResponse> {
    return this.http.get<MetricasFilaResponse>(`${this.endpoint}/metricas-fila`);
  }

  cancelarTurno(numero: number): Observable<CancelarTurnoResponse> {
    return this.http.put<CancelarTurnoResponse>(`${this.endpoint}/${numero}/cancelar`, {});
  }
}
