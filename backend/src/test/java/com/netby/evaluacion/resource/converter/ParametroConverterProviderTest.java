package com.netby.evaluacion.resource.converter;

import com.netby.evaluacion.entity.EstadoTransferencia;
import com.netby.evaluacion.exception.ParametroInvalidoException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ext.ParamConverter;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParametroConverterProviderTest {

    private final ParametroConverterProvider proveedor = new ParametroConverterProvider();

    private static Annotation[] queryParam(String nombre) {
        return new Annotation[]{new QueryParam() {
            @Override
            public String value() {
                return nombre;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return QueryParam.class;
            }
        }};
    }

    @Test
    void debeConvertirFechasYEnumsValidos() {
        ParamConverter<LocalDate> fecha = proveedor.getConverter(LocalDate.class, LocalDate.class, queryParam("f"));
        ParamConverter<EstadoTransferencia> estado =
                proveedor.getConverter(EstadoTransferencia.class, EstadoTransferencia.class, queryParam("e"));

        assertThat(fecha.fromString("2026-10-24")).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(fecha.toString(LocalDate.of(2026, 10, 24))).isEqualTo("2026-10-24");
        assertThat(estado.fromString(" en_revision ")).isEqualTo(EstadoTransferencia.EN_REVISION);
        assertThat(estado.toString(EstadoTransferencia.RECHAZADA)).isEqualTo("RECHAZADA");
    }

    @Test
    void valoresVaciosDebenInterpretarseComoAusentes() {
        ParamConverter<LocalDate> fecha = proveedor.getConverter(LocalDate.class, LocalDate.class, queryParam("f"));
        ParamConverter<EstadoTransferencia> estado =
                proveedor.getConverter(EstadoTransferencia.class, EstadoTransferencia.class, queryParam("e"));

        assertThat(fecha.fromString(" ")).isNull();
        assertThat(fecha.fromString(null)).isNull();
        assertThat(fecha.toString(null)).isNull();
        assertThat(estado.fromString("")).isNull();
        assertThat(estado.toString(null)).isNull();
    }

    @Test
    void elErrorDebeIndicarElNombreDelParametro() {
        ParamConverter<LocalDate> fecha =
                proveedor.getConverter(LocalDate.class, LocalDate.class, queryParam("fechaDesde"));

        assertThatThrownBy(() -> fecha.fromString("24/10/2026"))
                .isInstanceOfSatisfying(ParametroInvalidoException.class,
                        e -> assertThat(e.getParametro()).isEqualTo("fechaDesde"));
    }

    @Test
    void debeUsarElNombreDelPathParamOUnNombreGenerico() {
        PathParam pathParam = new PathParam() {
            @Override
            public String value() {
                return "id";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return PathParam.class;
            }
        };
        ParamConverter<EstadoTransferencia> conPath = proveedor.getConverter(
                EstadoTransferencia.class, EstadoTransferencia.class, new Annotation[]{pathParam});
        ParamConverter<EstadoTransferencia> sinNombre = proveedor.getConverter(
                EstadoTransferencia.class, EstadoTransferencia.class, new Annotation[0]);

        assertThatThrownBy(() -> conPath.fromString("X"))
                .isInstanceOfSatisfying(ParametroInvalidoException.class,
                        e -> assertThat(e.getParametro()).isEqualTo("id"));
        assertThatThrownBy(() -> sinNombre.fromString("X"))
                .isInstanceOfSatisfying(ParametroInvalidoException.class,
                        e -> assertThat(e.getParametro()).isEqualTo("parámetro"));
    }

    @Test
    void noDebeIntervenirEnOtrosTipos() {
        assertThat(proveedor.getConverter(String.class, String.class, queryParam("x"))).isNull();
    }
}
