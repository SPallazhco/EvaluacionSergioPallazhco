import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/config/api.config';
import { CrearTransferenciaRequest, TransferenciaResponse } from './transferencia.model';

@Injectable({ providedIn: 'root' })
export class TransferenciaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(API_URL)}/transferencias`;

  ejecutar(solicitud: CrearTransferenciaRequest): Observable<TransferenciaResponse> {
    const cuerpo: CrearTransferenciaRequest = {
      numeroCuentaOrigen: solicitud.numeroCuentaOrigen,
      numeroCuentaDestino: solicitud.numeroCuentaDestino,
      monto: solicitud.monto,
    };
    return this.http.post<TransferenciaResponse>(this.url, cuerpo);
  }
}
