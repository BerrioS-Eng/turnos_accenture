import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TurnosService } from './turnos.service';
import { SolicitarTurnoResponse } from './turno.model';

@Component({
  selector: 'app-user-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="user-container">
      <section class="page-intro">
        <p class="eyebrow">Autoservicio Móvil</p>
        <h1>Mi turno en línea</h1>
        <p>Pide tu turno sin hacer filas físicas y sigue tu posición en tiempo real.</p>
      </section>

      @if (!turno()) {
        <!-- Vista para solicitar turno -->
        <section class="action-card">
          <div class="action-card__content">
            <span class="action-icon">🎫</span>
            <h2>¿Listo para ser atendido?</h2>
            <p>Genera un nuevo turno digital al instante.</p>
          </div>
          <button
            type="button"
            class="btn-primary"
            data-testid="btn-solicitar-turno"
            (click)="pedirTurno()"
            [disabled]="loading()"
          >
            {{ loading() ? 'Generando turno...' : '+ Pedir Nuevo Turno' }}
          </button>
        </section>
      } @else {
        <!-- Vista de turno asignado -->
        <section class="ticket-card">
          <div class="ticket-header">
            <span class="badge" [class.badge--cancelled]="turno()?.estado === 'cancelado'">
              {{ turno()?.estado === 'cancelado' ? 'Cancelado' : 'En Espera' }}
            </span>
            <span class="ticket-time">{{ turno()?.horaSolicitud }}</span>
          </div>

          <div class="ticket-body">
            <p class="ticket-label">Tu Turno Asignado</p>
            <div class="ticket-number" data-testid="turno-asignado">
              #{{ turno()?.numero }}
            </div>
          </div>

          @if (turno()?.estado !== 'cancelado') {
            <div class="ticket-metrics">
              <div class="metric-item">
                <span class="metric-label">Personas antes de ti</span>
                <span class="metric-value" data-testid="turnos-por-delante">
                  {{ turno()?.turnosPorDelante }}
                </span>
              </div>
              <div class="metric-divider"></div>
              <div class="metric-item">
                <span class="metric-label">Tiempo estimado</span>
                <span class="metric-value" data-testid="tiempo-estimado">
                  ~{{ turno()?.tiempoEstimadoMinutos }} min
                </span>
              </div>
            </div>

            <div class="ticket-actions">
              <button
                type="button"
                class="btn-cancel"
                data-testid="btn-cancelar-turno"
                (click)="cancelar()"
                [disabled]="loading()"
              >
                ✕ Cancelar mi turno
              </button>
            </div>
          } @else {
            <div class="ticket-cancelled-msg">
              <p>Tu turno ha sido cancelado con éxito.</p>
              <button type="button" class="btn-secondary" (click)="nuevoTurno()">
                Pedir otro turno
              </button>
            </div>
          }
        </section>
      }

      @if (error()) {
        <p class="error-msg" role="alert">{{ error() }}</p>
      }
    </div>
  `,
  styles: [`
    :host { display: block; max-width: 32rem; margin: 0 auto; width: 100%; }
    .user-container { display: flex; flex-direction: column; gap: 1.5rem; }
    .page-intro { background: var(--surface-card); border: 1px solid var(--surface-border); border-radius: 1.25rem; box-shadow: var(--surface-shadow); padding: clamp(1.5rem, 5vw, 2.5rem); }
    .eyebrow { color: var(--primary); font-size: .75rem; font-weight: 700; letter-spacing: .08em; margin: 0; text-transform: uppercase; }
    h1 { color: var(--text-primary); font-size: clamp(1.75rem, 5vw, 2.5rem); letter-spacing: -.03em; margin: .5rem 0 .75rem; }
    .page-intro p:last-child { color: var(--text-secondary); margin: 0; font-size: .95rem; }

    .action-card { background: var(--surface-card); border: 1px solid var(--surface-border); border-radius: 1.25rem; box-shadow: var(--surface-shadow); padding: 2rem; text-align: center; display: flex; flex-direction: column; align-items: center; gap: 1.5rem; }
    .action-icon { font-size: 3rem; }
    .action-card h2 { color: var(--text-primary); margin: 0 0 .5rem; font-size: 1.35rem; }
    .action-card p { color: var(--text-secondary); margin: 0; font-size: .9rem; }

    .btn-primary { background: var(--primary); color: #FFFFFF; border: none; border-radius: .75rem; padding: 1rem 1.75rem; font-size: 1rem; font-weight: 700; cursor: pointer; transition: background .15s ease, transform .1s ease; width: 100%; }
    .btn-primary:hover:not(:disabled) { background: var(--primary-hover); transform: translateY(-1px); }
    .btn-primary:disabled { opacity: .6; cursor: not-allowed; }

    .ticket-card { background: var(--surface-card); border: 2px solid var(--primary-subtle); border-radius: 1.5rem; box-shadow: 0 10px 30px -5px rgba(79, 70, 229, 0.12); padding: 1.75rem; }
    .ticket-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; }
    .badge { background: var(--badge-pending-bg); color: var(--badge-pending-text); padding: .35rem .75rem; border-radius: 999px; font-size: .75rem; font-weight: 700; }
    .badge--cancelled { background: var(--badge-cancel-bg); color: var(--badge-cancel-text); }
    .ticket-time { color: var(--text-secondary); font-size: .85rem; }

    .ticket-body { text-align: center; padding: 1rem 0; border-bottom: 1px dashed var(--surface-border); }
    .ticket-label { color: var(--text-secondary); font-size: .85rem; margin: 0 0 .5rem; text-transform: uppercase; letter-spacing: .05em; font-weight: 600; }
    .ticket-number { color: var(--primary); font-size: 4rem; font-weight: 900; letter-spacing: -.04em; line-height: 1; }

    .ticket-metrics { display: flex; justify-content: space-around; align-items: center; padding: 1.25rem 0; }
    .metric-item { display: flex; flex-direction: column; align-items: center; gap: .25rem; }
    .metric-label { color: var(--text-secondary); font-size: .75rem; font-weight: 500; }
    .metric-value { color: var(--text-primary); font-size: 1.35rem; font-weight: 800; }
    .metric-divider { width: 1px; height: 2.5rem; background: var(--surface-border); }

    .ticket-actions { margin-top: .75rem; text-align: center; }
    .btn-cancel { background: transparent; color: var(--badge-cancel-text); border: 1px solid #FECACA; border-radius: .65rem; padding: .75rem 1.25rem; font-size: .9rem; font-weight: 600; cursor: pointer; transition: all .15s ease; width: 100%; }
    .btn-cancel:hover:not(:disabled) { background: var(--badge-cancel-bg); }
    .btn-cancel:disabled { opacity: .5; cursor: not-allowed; }

    .ticket-cancelled-msg { text-align: center; padding: 1rem 0; color: var(--text-secondary); }
    .btn-secondary { background: var(--surface-card); color: var(--primary); border: 1px solid var(--primary); border-radius: .65rem; padding: .75rem 1.25rem; font-weight: 600; cursor: pointer; margin-top: .75rem; }

    .error-msg { color: var(--badge-cancel-text); background: var(--badge-cancel-bg); padding: .75rem 1rem; border-radius: .5rem; font-size: .85rem; text-align: center; margin: 0; }
  `]
})
export class UserPageComponent {
  private readonly turnosService = inject(TurnosService);

  readonly turno = signal<SolicitarTurnoResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');

  pedirTurno() {
    this.loading.set(true);
    this.error.set('');
    this.turnosService.solicitarTurno().subscribe({
      next: (res) => {
        this.turno.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No fue posible solicitar el turno. Por favor intenta de nuevo.');
        this.loading.set(false);
      }
    });
  }

  cancelar() {
    const t = this.turno();
    if (!t) return;
    this.loading.set(true);
    this.turnosService.cancelarTurno(t.numero).subscribe({
      next: () => {
        this.turno.update((curr) => curr ? { ...curr, estado: 'cancelado' } : null);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cancelar el turno.');
        this.loading.set(false);
      }
    });
  }

  nuevoTurno() {
    this.turno.set(null);
    this.error.set('');
  }
}
