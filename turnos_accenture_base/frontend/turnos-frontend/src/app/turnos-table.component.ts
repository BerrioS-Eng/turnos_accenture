import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, PLATFORM_ID, inject, signal } from '@angular/core';
import { Turno } from './turno.model';
import { TurnosService } from './turnos.service';

@Component({
  selector: 'app-turnos-table',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="table-card" aria-labelledby="turnos-title">
      <div class="table-card__header">
        <div>
          <p class="eyebrow">Control en vivo</p>
          <h2 id="turnos-title">Historial de turnos</h2>
        </div>
        <span class="table-card__count">{{ turnos().length }} registros</span>
      </div>

      @if (loading()) {
        <p class="table-message" role="status">Cargando turnos...</p>
      } @else if (error()) {
        <p class="table-message table-message--error" role="alert">{{ error() }}</p>
      } @else {
        <div class="table-wrapper">
          <table data-testid="tabla-turnos">
            <thead>
              <tr>
                <th scope="col">Turno</th>
                <th scope="col">Solicitud</th>
                <th scope="col">Atención</th>
                <th scope="col">Estado</th>
              </tr>
            </thead>
            <tbody>
              @for (turno of turnos(); track turno.id) {
                <tr data-testid="fila-turno">
                  <th scope="row">#{{ turno.numero }}</th>
                  <td>{{ turno.horaSolicitud || '—' }}</td>
                  <td>{{ turno.horaAtencion || '—' }}</td>
                  <td>
                    <span class="status-badge" [class]="statusClass(turno.estado)">
                      {{ statusLabel(turno.estado) }}
                    </span>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td class="table-message" colspan="4">No hay turnos registrados.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </section>
  `,
  styles: [`
    :host { display: block; }
    .table-card { background: var(--surface-card); border: 1px solid var(--surface-border); border-radius: 1.25rem; box-shadow: var(--surface-shadow); overflow: hidden; }
    .table-card__header { align-items: center; display: flex; gap: 1rem; justify-content: space-between; padding: 1.5rem; }
    .table-card__header h2 { color: var(--text-primary); font-size: 1.25rem; margin: .25rem 0 0; }
    .table-card__count { color: var(--text-secondary); font-size: .875rem; white-space: nowrap; }
    .eyebrow { color: var(--primary); font-size: .75rem; font-weight: 700; letter-spacing: .08em; margin: 0; text-transform: uppercase; }
    .table-wrapper { overflow-x: auto; }
    table { border-collapse: collapse; min-width: 38rem; width: 100%; }
    th, td { border-top: 1px solid var(--surface-border); padding: .9rem 1.5rem; text-align: left; }
    thead th { background: var(--surface-muted); color: var(--text-secondary); font-size: .75rem; letter-spacing: .04em; text-transform: uppercase; }
    tbody th { color: var(--text-primary); font-weight: 700; }
    tbody td { color: var(--text-secondary); font-size: .9rem; }
    .status-badge { border-radius: 999px; display: inline-flex; font-size: .75rem; font-weight: 700; padding: .35rem .65rem; }
    .status-badge--pending { background: var(--badge-pending-bg); color: var(--badge-pending-text); }
    .status-badge--done { background: var(--badge-done-bg); color: var(--badge-done-text); }
    .status-badge--cancelled { background: var(--badge-cancel-bg); color: var(--badge-cancel-text); }
    .status-badge--unknown { background: var(--primary-subtle); color: var(--primary); }
    .table-message { color: var(--text-secondary); margin: 0; padding: 2rem 1.5rem; text-align: center; }
    .table-message--error { color: var(--badge-cancel-text); }
  `]
})
export class TurnosTableComponent {
  private readonly turnosService = inject(TurnosService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly turnos = signal<Turno[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.loadTurnos();
    } else {
      this.loading.set(false);
    }
  }

  statusLabel(status: string): string {
    return {
      pendiente: 'Pendiente',
      atendido: 'Atendido',
      cancelado: 'Cancelado'
    }[status] ?? status;
  }

  statusClass(status: string): string {
    return {
      pendiente: 'status-badge--pending',
      atendido: 'status-badge--done',
      cancelado: 'status-badge--cancelled'
    }[status] ?? 'status-badge--unknown';
  }

  loadTurnos(): void {
    this.turnosService.getTurnos().subscribe({
      next: (turnos) => {
        this.turnos.set(turnos);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial de turnos.');
        this.loading.set(false);
      }
    });
  }
}
