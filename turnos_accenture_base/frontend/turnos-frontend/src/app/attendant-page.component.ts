import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, PLATFORM_ID, ViewChild, inject, signal } from '@angular/core';
import { TurnosTableComponent } from './turnos-table.component';
import { TurnosService } from './turnos.service';
import { MetricasFilaResponse } from './turno.model';

@Component({
  selector: 'app-attendant-page',
  standalone: true,
  imports: [CommonModule, TurnosTableComponent],
  template: `
    <div class="attendant-container">
      <section class="page-intro">
        <p class="eyebrow">Ventanilla de Atención</p>
        <h1>Panel del Atendedor</h1>
        <p>Control de fila virtual, métricas en tiempo real y llamado de turnos.</p>
      </section>

      <!-- Panel Superior de Acciones y KPIs -->
      <section class="desk-dashboard">
        <!-- Tarjeta de Llamar Turno -->
        <div class="call-card">
          <div class="call-card__header">
            <span class="call-icon">🛎️</span>
            <div>
              <h2>Atención en Ventanilla</h2>
              <p>Llama al siguiente turno pendiente según orden de llegada (FIFO).</p>
            </div>
          </div>

          <button
            type="button"
            class="btn-call"
            data-testid="btn-llamar-siguiente"
            (click)="llamarSiguiente()"
            [disabled]="loading()"
          >
            {{ loading() ? 'Llamando...' : '🛎️ Llamar Siguiente Turno' }}
          </button>
        </div>

        <!-- Métricas Rápidas -->
        <div class="kpi-grid">
          <!-- Personas esperando -->
          <div class="kpi-card kpi-card--highlight">
            <span class="kpi-label">Personas esperando</span>
            <div class="kpi-value" data-testid="personas-esperando">
              {{ metricas()?.personasEsperando ?? 0 }}
            </div>
            <span class="kpi-sub">En cola virtual</span>
          </div>

          <!-- Turno actual en ventanilla -->
          <div class="kpi-card">
            <span class="kpi-label">Turno en ventanilla</span>
            <div class="kpi-value kpi-value--active" data-testid="turno-actual">
              {{ metricas()?.turnoActual ? '#' + metricas()?.turnoActual : '—' }}
            </div>
            <span class="kpi-sub">Siendo atendido</span>
          </div>

          <!-- Total Atendidos -->
          <div class="kpi-card">
            <span class="kpi-label">Total atendidos</span>
            <div class="kpi-value">
              {{ metricas()?.totalAtendidos ?? 0 }}
            </div>
            <span class="kpi-sub">Completados hoy</span>
          </div>

          <!-- Promedio de Espera -->
          <div class="kpi-card">
            <span class="kpi-label">Espera promedio</span>
            <div class="kpi-value">
              {{ metricas()?.promedioEsperaMinutos ?? 0 }}m
            </div>
            <span class="kpi-sub">Tiempo histórico</span>
          </div>
        </div>
      </section>

      <!-- Mensaje de Error / Fila Vacía -->
      @if (error()) {
        <div class="error-banner" data-testid="mensaje-error" role="alert">
          <span class="error-icon">⚠️</span>
          <span>{{ error() }}</span>
        </div>
      }

      <!-- Tabla de Control en Vivo -->
      <app-turnos-table />
    </div>
  `,
  styles: [`
    :host { display: block; width: 100%; }
    .attendant-container { display: flex; flex-direction: column; gap: 1.5rem; }
    
    .page-intro { background: var(--surface-card); border: 1px solid var(--surface-border); border-radius: 1.25rem; box-shadow: var(--surface-shadow); padding: clamp(1.25rem, 4vw, 2rem); }
    .eyebrow { color: var(--primary); font-size: .75rem; font-weight: 700; letter-spacing: .08em; margin: 0; text-transform: uppercase; }
    h1 { color: var(--text-primary); font-size: clamp(1.75rem, 5vw, 2.5rem); letter-spacing: -.03em; margin: .5rem 0 .5rem; }
    .page-intro p:last-child { color: var(--text-secondary); margin: 0; }

    .desk-dashboard { display: grid; grid-template-columns: 1fr; gap: 1.25rem; }
    @media (min-width: 64rem) {
      .desk-dashboard { grid-template-columns: 1.2fr 2fr; }
    }

    .call-card { background: var(--surface-card); border: 1px solid var(--surface-border); border-radius: 1.25rem; box-shadow: var(--surface-shadow); padding: 1.75rem; display: flex; flex-direction: column; justify-content: space-between; gap: 1.5rem; }
    .call-card__header { display: flex; align-items: flex-start; gap: 1rem; }
    .call-icon { font-size: 2.25rem; }
    .call-card h2 { color: var(--text-primary); margin: 0 0 .25rem; font-size: 1.25rem; }
    .call-card p { color: var(--text-secondary); margin: 0; font-size: .875rem; }

    .btn-call { background: var(--primary); color: #FFFFFF; border: none; border-radius: .75rem; padding: 1.15rem 1.5rem; font-size: 1.1rem; font-weight: 700; cursor: pointer; transition: all .15s ease; box-shadow: 0 4px 14px rgba(79, 70, 229, 0.3); width: 100%; }
    .btn-call:hover:not(:disabled) { background: var(--primary-hover); transform: translateY(-2px); box-shadow: 0 6px 20px rgba(79, 70, 229, 0.4); }
    .btn-call:disabled { opacity: .6; cursor: not-allowed; }

    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(9.5rem, 1fr)); gap: 1rem; }
    .kpi-card { background: var(--surface-card); border: 1px solid var(--surface-border); border-radius: 1.25rem; box-shadow: var(--surface-shadow); padding: 1.25rem; display: flex; flex-direction: column; justify-content: center; }
    .kpi-card--highlight { border-color: var(--primary-subtle); background: #FAF5FF; }
    .kpi-label { color: var(--text-secondary); font-size: .75rem; font-weight: 600; text-transform: uppercase; letter-spacing: .04em; margin-bottom: .25rem; }
    .kpi-value { color: var(--text-primary); font-size: 2.25rem; font-weight: 900; line-height: 1.1; }
    .kpi-value--active { color: var(--primary); }
    .kpi-sub { color: var(--text-secondary); font-size: .75rem; margin-top: .35rem; }

    .error-banner { display: flex; align-items: center; gap: .75rem; background: var(--badge-cancel-bg); border: 1px solid #FECACA; color: var(--badge-cancel-text); padding: 1rem 1.25rem; border-radius: .75rem; font-weight: 600; font-size: .95rem; }
    .error-icon { font-size: 1.25rem; }
  `]
})
export class AttendantPageComponent {
  private readonly turnosService = inject(TurnosService);
  private readonly platformId = inject(PLATFORM_ID);

  @ViewChild(TurnosTableComponent) turnosTable?: TurnosTableComponent;

  readonly metricas = signal<MetricasFilaResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.cargarMetricas();
    }
  }

  cargarMetricas() {
    this.turnosService.getMetricas().subscribe({
      next: (data) => this.metricas.set(data),
      error: () => this.error.set('No fue posible cargar las métricas de la fila.')
    });
  }

  llamarSiguiente() {
    this.loading.set(true);
    this.error.set('');
    this.turnosService.llamarSiguiente().subscribe({
      next: () => {
        this.cargarMetricas();
        this.turnosTable?.loadTurnos();
        this.loading.set(false);
      },
      error: (err) => {
        if (err.status === 409 || err.error?.mensaje) {
          this.error.set(err.error?.mensaje || 'No hay personas esperando en la fila.');
        } else {
          this.error.set('Error al intentar llamar el siguiente turno.');
        }
        this.cargarMetricas();
        this.loading.set(false);
      }
    });
  }
}
