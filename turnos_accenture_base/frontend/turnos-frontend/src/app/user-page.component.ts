import { Component } from '@angular/core';

@Component({
  selector: 'app-user-page',
  standalone: true,
  template: `
    <section class="page-intro">
      <p class="eyebrow">Autoservicio</p>
      <h1>Tu turno en línea</h1>
      <p>Solicita y consulta tu turno desde cualquier lugar.</p>
    </section>
  `,
  styles: [`
    :host { display: block; }
    .page-intro { background: var(--surface-card); border: 1px solid var(--surface-border); border-radius: 1.25rem; box-shadow: var(--surface-shadow); padding: clamp(1.5rem, 5vw, 3rem); }
    .eyebrow { color: var(--primary); font-size: .75rem; font-weight: 700; letter-spacing: .08em; margin: 0; text-transform: uppercase; }
    h1 { color: var(--text-primary); font-size: clamp(2rem, 6vw, 3.5rem); letter-spacing: -.04em; margin: .5rem 0 1rem; }
    p:last-child { color: var(--text-secondary); margin: 0; }
  `]
})
export class UserPageComponent {}
