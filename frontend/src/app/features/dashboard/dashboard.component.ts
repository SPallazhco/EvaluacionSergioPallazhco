import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CuentasComponent } from '../cuentas/cuentas.component';
import { TransferenciaFormComponent } from '../transferencias/transferencia-form.component';

@Component({
  selector: 'app-dashboard',
  imports: [CuentasComponent, TransferenciaFormComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {}
