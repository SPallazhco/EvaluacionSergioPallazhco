import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Transferencias Interbancarias',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  { path: '**', redirectTo: '' },
];
