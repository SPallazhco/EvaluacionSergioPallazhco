import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ResumenCuentaResponse } from './cuenta.model';

@Component({
  selector: 'app-resumen-cuenta',
  imports: [CurrencyPipe],
  templateUrl: './resumen-cuenta.component.html',
  styleUrl: './resumen-cuenta.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResumenCuentaComponent {
  readonly resumen = input.required<ResumenCuentaResponse>();
}
