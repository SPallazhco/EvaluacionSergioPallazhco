import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule, Sort, SortDirection } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { ErrorApi } from '../../core/models/error-api.model';
import { CeldaTipadaDirective } from '../../shared/directives/celda-tipada.directive';
import { PaginadorIntl } from '../../shared/paginador-intl';
import { ETIQUETAS_ESTADO_CUENTA, ETIQUETAS_TIPO_CUENTA } from './cuenta.model';
import { CuentaService } from './cuenta.service';
import { ordenarPorSaldo } from './ordenar-cuentas';

const MENSAJE_ERROR_CARGA = 'No se pudieron cargar las cuentas.';

@Component({
  selector: 'app-cuentas',
  imports: [
    CeldaTipadaDirective,
    CurrencyPipe,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSortModule,
    MatTableModule,
  ],
  providers: [{ provide: MatPaginatorIntl, useClass: PaginadorIntl }],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CuentasComponent {
  private readonly cuentaService = inject(CuentaService);

  protected readonly columnas = ['numeroCuenta', 'titular', 'tipo', 'saldo', 'estado'];
  protected readonly opcionesTamanioPagina = [10, 25, 50];
  protected readonly etiquetasTipo = ETIQUETAS_TIPO_CUENTA;
  protected readonly etiquetasEstado = ETIQUETAS_ESTADO_CUENTA;

  protected readonly cuentas = rxResource({
    stream: () => this.cuentaService.listarTodas(),
  });

  private readonly direccionSaldo = signal<SortDirection>('');
  protected readonly indicePagina = signal(0);
  protected readonly tamanioPagina = signal(10);

  protected readonly cuentasOrdenadas = computed(() =>
    ordenarPorSaldo(this.cuentas.hasValue() ? this.cuentas.value() : [], this.direccionSaldo()),
  );

  protected readonly cuentasPagina = computed(() => {
    const inicio = this.indicePagina() * this.tamanioPagina();
    return this.cuentasOrdenadas().slice(inicio, inicio + this.tamanioPagina());
  });

  protected readonly mensajeError = computed(() => {
    const error = this.cuentas.error();
    return error instanceof ErrorApi ? error.message : MENSAJE_ERROR_CARGA;
  });

  protected ordenar(orden: Sort): void {
    this.direccionSaldo.set(orden.direction);
    this.indicePagina.set(0);
  }

  protected cambiarPagina(evento: PageEvent): void {
    this.indicePagina.set(evento.pageIndex);
    this.tamanioPagina.set(evento.pageSize);
  }

  protected recargar(): void {
    this.cuentas.reload();
  }
}
