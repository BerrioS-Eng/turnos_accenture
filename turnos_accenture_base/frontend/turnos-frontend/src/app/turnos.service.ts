import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Turno } from './turno.model';

@Injectable({ providedIn: 'root' })
export class TurnosService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = '/api/turnos';

  getTurnos(): Observable<Turno[]> {
    return this.http.get<Turno[]>(this.endpoint);
  }
}
