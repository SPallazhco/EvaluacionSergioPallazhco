package com.netby.evaluacion.service;

import com.netby.evaluacion.dto.request.CrearCuentaRequest;
import com.netby.evaluacion.dto.response.CuentaResponse;
import com.netby.evaluacion.dto.response.PaginaResponse;
import com.netby.evaluacion.entity.Cuenta;
import com.netby.evaluacion.entity.EstadoCuenta;
import com.netby.evaluacion.entity.TipoCuenta;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.RecursoDuplicadoException;
import com.netby.evaluacion.mapper.CuentaMapperImpl;
import com.netby.evaluacion.repository.CuentaRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private PanacheQuery<Cuenta> consulta;

    private CuentaService cuentaService;

    @BeforeEach
    void setUp() {
        cuentaService = new CuentaService(cuentaRepository, new CuentaMapperImpl());
    }

    private static CrearCuentaRequest solicitud(String numeroCuenta) {
        return new CrearCuentaRequest(numeroCuenta, "Ana Pérez", TipoCuenta.AHORRO,
                new BigDecimal("150.00"), EstadoCuenta.ACTIVA);
    }

    @Test
    void registrarDebePersistirLaCuentaYRetornarSusDatos() {
        when(cuentaRepository.existePorNumeroCuenta("1234567890")).thenReturn(false);

        CuentaResponse respuesta = cuentaService.registrar(solicitud("1234567890"));

        ArgumentCaptor<Cuenta> captor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).persist(captor.capture());
        Cuenta persistida = captor.getValue();
        assertThat(persistida.getNumeroCuenta()).isEqualTo("1234567890");
        assertThat(persistida.getSaldo()).isEqualByComparingTo("150.00");
        assertThat(persistida.getEstado()).isEqualTo(EstadoCuenta.ACTIVA);

        assertThat(respuesta.numeroCuenta()).isEqualTo("1234567890");
        assertThat(respuesta.titular()).isEqualTo("Ana Pérez");
        assertThat(respuesta.tipo()).isEqualTo(TipoCuenta.AHORRO);
        assertThat(respuesta.saldo()).isEqualByComparingTo("150.00");
    }

    @Test
    void registrarDebeRechazarNumeroDeCuentaDuplicado() {
        when(cuentaRepository.existePorNumeroCuenta("1234567890")).thenReturn(true);
        CrearCuentaRequest request = solicitud("1234567890");

        assertThatThrownBy(() -> cuentaService.registrar(request))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("1234567890")
                .extracting("codigo").isEqualTo(CodigoError.CUENTA_DUPLICADA);
        verify(cuentaRepository, never()).persist(any(Cuenta.class));
    }

    @Test
    void listarDebeRetornarLaPaginaSolicitadaConSusMetadatos() {
        Cuenta cuenta = new Cuenta("1234567890", "Ana Pérez", TipoCuenta.CORRIENTE,
                new BigDecimal("10.00"), EstadoCuenta.INACTIVA);
        when(cuentaRepository.findAll(any(Sort.class))).thenReturn(consulta);
        when(consulta.page(any(Page.class))).thenReturn(consulta);
        when(consulta.list()).thenReturn(List.of(cuenta));
        when(consulta.count()).thenReturn(21L);
        when(consulta.pageCount()).thenReturn(3);

        PaginaResponse<CuentaResponse> pagina = cuentaService.listar(2, 10);

        assertThat(pagina.contenido()).singleElement()
                .satisfies(c -> {
                    assertThat(c.numeroCuenta()).isEqualTo("1234567890");
                    assertThat(c.estado()).isEqualTo(EstadoCuenta.INACTIVA);
                });
        assertThat(pagina.pagina()).isEqualTo(2);
        assertThat(pagina.tamanio()).isEqualTo(10);
        assertThat(pagina.totalElementos()).isEqualTo(21L);
        assertThat(pagina.totalPaginas()).isEqualTo(3);
    }
}
