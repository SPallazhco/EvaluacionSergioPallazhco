import { CurrencyPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  LOCALE_ID,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { rxResource, takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  FormGroupDirective,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { finalize } from 'rxjs';
import { MensajeComponent } from '../../shared/components/mensaje/mensaje.component';
import { ETIQUETAS_ESTADO_CUENTA } from '../cuentas/cuenta.model';
import { CuentaService } from '../cuentas/cuenta.service';
import {
  ResultadoTransferencia,
  resultadoExitoso,
  resultadoFallido,
} from './resultado-transferencia';
import { CrearTransferenciaRequest, TransferenciaResponse } from './transferencia.model';
import { TransferenciaService } from './transferencia.service';
import {
  convertirMonto,
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
    MensajeComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './transferencia-form.component.html',
  styleUrl: './transferencia-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransferenciaFormComponent {
  private readonly cuentaService = inject(CuentaService);
  private readonly transferenciaService = inject(TransferenciaService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly locale = inject(LOCALE_ID);

  readonly transferenciaRealizada = output<TransferenciaResponse>();

  private readonly directivaFormulario = viewChild.required(FormGroupDirective);

  protected readonly cuentasIgualesMatcher = new CuentasIgualesErrorStateMatcher();

  protected readonly formulario = this.fb.group(
    {
      cuentaOrigen: ['', Validators.required],
      cuentaDestino: ['', Validators.required],
      monto: ['', [Validators.required, montoNumerico, montoMayorQueCero, montoConDosDecimales]],
    },
    { validators: cuentasDistintas },
  );

  protected readonly enviando = signal(false);
  protected readonly resultado = signal<ResultadoTransferencia | null>(null);

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
    const solicitud = this.crearSolicitud();
    if (this.formulario.invalid || this.enviando() || !solicitud) {
      return;
    }

    this.enviando.set(true);
    this.resultado.set(null);
    this.transferenciaService
      .ejecutar(solicitud)
      .pipe(
        finalize(() => {
          this.enviando.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (transferencia) => {
          this.alCompletar(transferencia);
        },
        error: (error: unknown) => {
          this.resultado.set(resultadoFallido(error));
        },
      });
  }

  private crearSolicitud(): CrearTransferenciaRequest | null {
    const valor = this.formulario.getRawValue();
    const monto = convertirMonto(valor.monto);
    return monto === null
      ? null
      : {
          numeroCuentaOrigen: valor.cuentaOrigen,
          numeroCuentaDestino: valor.cuentaDestino,
          monto,
        };
  }

  private alCompletar(transferencia: TransferenciaResponse): void {
    this.resultado.set(resultadoExitoso(transferencia, this.locale));
    this.directivaFormulario().resetForm();
    this.cuentas.reload();
    this.transferenciaRealizada.emit(transferencia);
  }
}
