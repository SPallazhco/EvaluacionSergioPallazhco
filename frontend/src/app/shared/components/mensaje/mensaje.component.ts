import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type TipoMensaje = 'exito' | 'advertencia' | 'error';

const ICONOS: Readonly<Record<TipoMensaje, string>> = {
  exito: 'check_circle',
  advertencia: 'warning',
  error: 'error',
};

@Component({
  selector: 'app-mensaje',
  imports: [MatIconModule],
  templateUrl: './mensaje.component.html',
  styleUrl: './mensaje.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'mensaje',
    '[class.mensaje--exito]': "tipo() === 'exito'",
    '[class.mensaje--advertencia]': "tipo() === 'advertencia'",
    '[class.mensaje--error]': "tipo() === 'error'",
    '[attr.role]': 'rol()',
  },
})
export class MensajeComponent {
  readonly tipo = input.required<TipoMensaje>();
  readonly texto = input.required<string>();
  readonly detalles = input<readonly string[]>([]);

  protected readonly icono = computed(() => ICONOS[this.tipo()]);
  protected readonly rol = computed(() => (this.tipo() === 'error' ? 'alert' : 'status'));
}
