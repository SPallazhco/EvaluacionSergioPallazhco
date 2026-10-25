import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ETIQUETAS_ESTADO_CUENTA } from '../cuentas/cuenta.model';
import { CuentaService } from '../cuentas/cuenta.service';
import {
  cuentasDistintas,
  CuentasIgualesErrorStateMatcher,
  mensajeErrorDestino,
  mensajeErrorMonto,
  montoConDosDecimales,
  montoMayorQueCero,
  montoNumerico,
} from './transferencia.validators';

@Component({
  selector: 'app-transferencia-form',
  imports: [
    CurrencyPipe,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
  ],
  templateUrl: './transferencia-form.component.html',
  styleUrl: './transferencia-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransferenciaFormComponent {
  private readonly cuentaService = inject(CuentaService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly cuentasIgualesMatcher = new CuentasIgualesErrorStateMatcher();

  protected readonly formulario = this.fb.group(
    {
      cuentaOrigen: ['', Validators.required],
      cuentaDestino: ['', Validators.required],
      monto: ['', [Validators.required, montoNumerico, montoMayorQueCero, montoConDosDecimales]],
    },
    { validators: cuentasDistintas },
  );

  protected readonly cuentas = rxResource({
    stream: () => this.cuentaService.listarTodas(),
  });

  private readonly numeroOrigen = toSignal(this.formulario.controls.cuentaOrigen.valueChanges, {
    initialValue: '',
  });

  private readonly listaCuentas = computed(() =>
    this.cuentas.hasValue() ? this.cuentas.value() : [],
  );

  protected readonly opcionesCuentas = computed(() =>
    this.listaCuentas().map((cuenta) => {
      const inactiva = cuenta.estado === 'INACTIVA';
      const sufijo = inactiva ? ` (${ETIQUETAS_ESTADO_CUENTA.INACTIVA})` : '';
      return {
        numero: cuenta.numeroCuenta,
        etiqueta: `${cuenta.numeroCuenta} · ${cuenta.titular}${sufijo}`,
        inactiva,
      };
    }),
  );

  protected readonly cuentaOrigen = computed(() =>
    this.listaCuentas().find((cuenta) => cuenta.numeroCuenta === this.numeroOrigen()),
  );

  protected mensajeErrorMonto(): string {
    return mensajeErrorMonto(this.formulario.controls.monto.errors);
  }

  protected mensajeErrorDestino(): string {
    return mensajeErrorDestino(this.formulario.controls.cuentaDestino.errors);
  }

  protected enviar(): void {
    this.formulario.markAllAsTouched();
  }
}
