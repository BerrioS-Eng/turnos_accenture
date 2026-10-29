import { Routes } from '@angular/router';
import { AttendantPageComponent } from './attendant-page.component';
import { UserPageComponent } from './user-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'usuario' },
  { path: 'usuario', component: UserPageComponent },
  { path: 'atendedor', component: AttendantPageComponent },
  { path: '**', redirectTo: 'usuario' }
];
