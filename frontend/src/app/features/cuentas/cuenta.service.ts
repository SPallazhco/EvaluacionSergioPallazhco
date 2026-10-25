import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { API_URL } from '../../core/config/api.config';
import { PaginaResponse } from '../../shared/models/pagina.model';
import { CuentaResponse } from './cuenta.model';

export const TAMANIO_MAXIMO_PAGINA = 100;

@Injectable({ providedIn: 'root' })
export class CuentaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(API_URL)}/cuentas`;

  listar(pagina: number, tamanio: number): Observable<PaginaResponse<CuentaResponse>> {
    return this.http.get<PaginaResponse<CuentaResponse>>(this.url, {
      params: { pagina, tamanio },
    });
  }

  listarTodas(): Observable<readonly CuentaResponse[]> {
    return this.listar(0, TAMANIO_MAXIMO_PAGINA).pipe(
      switchMap((primera) => {
        if (primera.totalPaginas <= 1) {
          return of(primera.contenido);
        }
        const restantes = Array.from({ length: primera.totalPaginas - 1 }, (_, indice) =>
          this.listar(indice + 1, TAMANIO_MAXIMO_PAGINA),
        );
        return forkJoin(restantes).pipe(
          map((paginas) => [primera, ...paginas].flatMap((pagina) => pagina.contenido)),
        );
      }),
    );
  }
}
