import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CuentasComponent } from '../cuentas/cuentas.component';

@Component({
  selector: 'app-dashboard',
  imports: [CuentasComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {}
