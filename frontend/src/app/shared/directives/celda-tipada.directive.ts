import { Directive, input } from '@angular/core';

export interface ContextoCelda<T> {
  readonly $implicit: T;
}

// eslint-disable-next-line @angular-eslint/directive-selector -- extiende la microsintaxis de matCellDef
@Directive({ selector: '[matCellDef][matCellDefDatos]' })
export class CeldaTipadaDirective<T> {
  readonly matCellDefDatos = input.required<readonly T[]>();

  static ngTemplateContextGuard<T>(
    _directiva: CeldaTipadaDirective<T>,
    _contexto: unknown,
  ): _contexto is ContextoCelda<T> {
    return true;
  }
}
