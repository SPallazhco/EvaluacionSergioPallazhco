export interface PaginaResponse<T> {
  readonly contenido: readonly T[];
  readonly pagina: number;
  readonly tamanio: number;
  readonly totalElementos: number;
  readonly totalPaginas: number;
}
