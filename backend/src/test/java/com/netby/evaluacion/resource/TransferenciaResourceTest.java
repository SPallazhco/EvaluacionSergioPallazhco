package com.netby.evaluacion.resource;

import com.netby.evaluacion.entity.EstadoTransferencia;
import com.netby.evaluacion.repository.TransferenciaRepository;
import com.netby.evaluacion.resource.ApiDePrueba.CuentaCreada;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.netby.evaluacion.resource.ApiDePrueba.crearCuenta;
import static com.netby.evaluacion.resource.ApiDePrueba.crearCuentaActiva;
import static com.netby.evaluacion.resource.ApiDePrueba.saldoDe;
import static com.netby.evaluacion.resource.ApiDePrueba.transferir;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class TransferenciaResourceTest {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Inject
    TransferenciaRepository transferenciaRepository;

    private long transferenciasRechazadasDesde(CuentaCreada origen) {
        return QuarkusTransaction.requiringNew().call(() -> transferenciaRepository.count(
                "cuentaOrigen.id = ?1 and estado = ?2", origen.id(), EstadoTransferencia.RECHAZADA));
    }

    @Test
    void transferenciaValidaDebeActualizarAmbosSaldosYResponder201() {
        CuentaCreada origen = crearCuentaActiva("1000.00");
        CuentaCreada destino = crearCuentaActiva("50.00");

        ExtractableResponse<Response> respuesta = transferir(origen, destino, "300.00")
                .then()
                .statusCode(201)
                .body("estado", equalTo("COMPLETADA"))
                .body("numeroCuentaOrigen", equalTo(origen.numero()))
                .body("numeroCuentaDestino", equalTo(destino.numero()))
                .body("fecha", notNullValue())
                .extract();
        int id = respuesta.path("id");

        assertThat(respuesta.header("Location")).endsWith("/api/transferencias/" + id);
        assertThat(saldoDe(origen)).isEqualByComparingTo("700.00");
        assertThat(saldoDe(destino)).isEqualByComparingTo("350.00");

        given().get("/api/transferencias/" + id)
                .then()
                .statusCode(200)
                .body("monto", equalTo(300.0f))
                .body("estado", equalTo("COMPLETADA"));
    }

    @Test
    void montoMayorA5000DebeEjecutarseYQuedarEnRevision() {
        CuentaCreada origen = crearCuentaActiva("8000.00");
        CuentaCreada destino = crearCuentaActiva("0.00");

        transferir(origen, destino, "5000.01")
                .then()
                .statusCode(201)
                .body("estado", equalTo("EN_REVISION"))
                .body("motivo", equalTo("Monto superior a 5000.00"));

        assertThat(saldoDe(origen)).isEqualByComparingTo("2999.99");
        assertThat(saldoDe(destino)).isEqualByComparingTo("5000.01");
    }

    @Test
    void laSextaTransferenciaEnUnaHoraDebeQuedarEnRevision() {
        CuentaCreada origen = crearCuentaActiva("1000.00");
        CuentaCreada destino = crearCuentaActiva("0.00");

        for (int i = 1; i <= 5; i++) {
            transferir(origen, destino, "10.00").then().statusCode(201).body("estado", equalTo("COMPLETADA"));
        }

        transferir(origen, destino, "10.00")
                .then()
                .statusCode(201)
                .body("estado", equalTo("EN_REVISION"))
                .body("motivo", containsString("Más de 5 transferencias"));
    }

    @Test
    void saldoInsuficienteDebeResponder422RegistrarElRechazoYNoMoverDinero() {
        CuentaCreada origen = crearCuentaActiva("100.00");
        CuentaCreada destino = crearCuentaActiva("0.00");

        transferir(origen, destino, "100.01")
                .then()
                .statusCode(422)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("SALDO_INSUFICIENTE"))
                .body("detail", equalTo("La cuenta origen no tiene saldo suficiente"));

        assertThat(saldoDe(origen)).isEqualByComparingTo("100.00");
        assertThat(saldoDe(destino)).isEqualByComparingTo("0.00");
        assertThat(transferenciasRechazadasDesde(origen)).isEqualTo(1);
    }

    @Test
    void cuentaDestinoInactivaDebeResponder422() {
        CuentaCreada origen = crearCuentaActiva("100.00");
        CuentaCreada destino = crearCuenta("0.00", "INACTIVA");

        transferir(origen, destino, "10.00")
                .then()
                .statusCode(422)
                .body("codigo", equalTo("CUENTA_INACTIVA"))
                .body("detail", equalTo("La cuenta destino no está activa"));

        assertThat(transferenciasRechazadasDesde(origen)).isEqualTo(1);
    }

    @Test
    void superarElLimiteDiarioDebeResponder422IndicandoElMontoDisponible() {
        CuentaCreada origen = crearCuentaActiva("20000.00");
        CuentaCreada destino = crearCuentaActiva("0.00");
        transferir(origen, destino, "6000.00").then().statusCode(201);
        transferir(origen, destino, "3000.00").then().statusCode(201);

        transferir(origen, destino, "1000.01")
                .then()
                .statusCode(422)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("LIMITE_DIARIO_EXCEDIDO"))
                .body("detail", containsString("Monto disponible hoy: 1000.00"))
                .body("detalles.montoDisponible", equalTo(1000.0f));

        transferir(origen, destino, "1000.00").then().statusCode(201);
        assertThat(saldoDe(origen)).isEqualByComparingTo("10000.00");
    }

    @Test
    void cuentaInexistenteDebeResponder404() {
        CuentaCreada origen = crearCuentaActiva("100.00");
        CuentaCreada inexistente = new CuentaCreada(-1, ApiDePrueba.nuevoNumeroCuenta());

        transferir(origen, inexistente, "10.00")
                .then()
                .statusCode(404)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("CUENTA_NO_ENCONTRADA"));
    }

    @Test
    void cuentasIgualesOMontoNoPositivoDebenResponder400() {
        CuentaCreada cuenta = crearCuentaActiva("100.00");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("numeroCuentaOrigen", cuenta.numero(), "numeroCuentaDestino", cuenta.numero(),
                        "monto", 0))
                .post("/api/transferencias")
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("errores.campo", hasItem("cuentasDistintas"))
                .body("errores.campo", hasItem("monto"));
    }

    @Test
    void transferenciaInexistenteDebeResponder404() {
        given().get("/api/transferencias/999999999")
                .then()
                .statusCode(404)
                .body("codigo", equalTo("TRANSFERENCIA_NO_ENCONTRADA"));
    }
}
