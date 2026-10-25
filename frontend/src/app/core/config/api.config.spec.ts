import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { API_URL } from './api.config';

describe('API_URL', () => {
  it('toma la URL de la API del entorno', () => {
    expect(TestBed.inject(API_URL)).toBe(environment.apiUrl);
  });

  it('permite reemplazar la URL desde un provider', () => {
    const urlPrueba = 'https://api.prueba/api';
    TestBed.configureTestingModule({
      providers: [{ provide: API_URL, useValue: urlPrueba }],
    });

    expect(TestBed.inject(API_URL)).toBe(urlPrueba);
  });
});
