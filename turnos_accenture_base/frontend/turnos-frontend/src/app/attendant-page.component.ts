import { Component } from '@angular/core';
import { TurnosTableComponent } from './turnos-table.component';

@Component({
  selector: 'app-attendant-page',
  standalone: true,
  imports: [TurnosTableComponent],
  template: `
    <section class="page-intro">
      <p class="eyebrow">Ventanilla</p>
      <h1>Panel de atención</h1>
      <p>Consulta el historial de turnos de la jornada.</p>
    </section>
    <app-turnos-table />
  `,
  styles: [`
    :host { display: grid; gap: 1.5rem; }
    .page-intro { padding: .5rem 0; }
    .eyebrow { color: var(--primary); font-size: .75rem; font-weight: 700; letter-spacing: .08em; margin: 0; text-transform: uppercase; }
    h1 { color: var(--text-primary); font-size: clamp(2rem, 5vw, 3rem); letter-spacing: -.04em; margin: .5rem 0 .75rem; }
    p:last-child { color: var(--text-secondary); margin: 0; }
  `]
})
export class AttendantPageComponent {}
