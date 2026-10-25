import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { CuentasComponent } from '../cuentas/cuentas.component';
import { TransferenciaFormComponent } from '../transferencias/transferencia-form.component';

@Component({
  selector: 'app-dashboard',
  imports: [CuentasComponent, TransferenciaFormComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {
  protected readonly actualizacionCuentas = signal(0);

  protected refrescarCuentas(): void {
    this.actualizacionCuentas.update((valor) => valor + 1);
  }
}
