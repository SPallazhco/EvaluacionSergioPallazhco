package com.netby.evaluacion.resource;

import com.netby.evaluacion.resource.ApiDePrueba.CuentaCreada;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.netby.evaluacion.resource.ApiDePrueba.crearCuenta;
import static com.netby.evaluacion.resource.ApiDePrueba.crearCuentaActiva;
import static com.netby.evaluacion.resource.ApiDePrueba.transferir;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;

/**
 * CA-05: historial de transferencias con filtros y resumen de cuenta, contra la API y MySQL.
 */
@QuarkusTest
class ConsultasResourceTest {

    private static final String HISTORIAL = "/api/transferencias";
    private static final String PROBLEM_JSON = "application/problem+json";

    private static String hoyEnEcuador() {
        return LocalDate.now(ZoneId.of("America/Guayaquil")).toString();
    }

    @Test
    void historialDebeFiltrarPorCuentaOrigenYOrdenarPorFechaDescendente() {
        CuentaCreada origen = crearCuentaActiva("1000.00");
        CuentaCreada destino = crearCuentaActiva("0.00");
        transferir(origen, destino, "1.00").then().statusCode(201);
        transferir(origen, destino, "2.00").then().statusCode(201);
        transferir(origen, destino, "3.00").then().statusCode(201);

        given()
                .queryParam("cuentaOrigen", origen.numero())
                .when().get(HISTORIAL)
                .then()
                .statusCode(200)
                .body("totalElementos", equalTo(3))
                .body("contenido.monto", contains(3.0f, 2.0f, 1.0f))
                .body("contenido.numeroCuentaOrigen", everyItem(equalTo(origen.numero())));
    }

    @Test
    void historialDebeFiltrarPorCuentaDestinoYEstado() {
        CuentaCreada origen = crearCuentaActiva("100.00");
        CuentaCreada destino = crearCuentaActiva("0.00");
        transferir(origen, destino, "10.00").then().statusCode(201);
        transferir(origen, destino, "500.00").then().statusCode(422);

        given()
                .queryParam("cuentaDestino", destino.numero())
                .queryParam("estado", "RECHAZADA")
                .when().get(HISTORIAL)
                .then()
                .statusCode(200)
                .body("totalElementos", equalTo(1))
                .body("contenido[0].monto", equalTo(500.0f))
                .body("contenido[0].estado", equalTo("RECHAZADA"))
                .body("contenido[0].motivo", equalTo("La cuenta origen no tiene saldo suficiente"));
    }

    @Test
    void historialDebeFiltrarPorRangoDeFechasInclusivo() {
        CuentaCreada origen = crearCuentaActiva("100.00");
        CuentaCreada destino = crearCuentaActiva("0.00");
        transferir(origen, destino, "10.00").then().statusCode(201);
        String hoy = hoyEnEcuador();
        String manana = LocalDate.parse(hoy).plusDays(1).toString();

        given()
                .queryParam("cuentaOrigen", origen.numero())
                .queryParam("fechaDesde", hoy)
                .queryParam("fechaHasta", hoy)
                .when().get(HISTORIAL)
                .then()
                .statusCode(200)
                .body("totalElementos", equalTo(1));

        given()
                .queryParam("cuentaOrigen", origen.numero())
                .queryParam("fechaDesde", manana)
                .when().get(HISTORIAL)
                .then()
                .statusCode(200)
                .body("contenido", empty());
    }

    @Test
    void historialDebePaginarLosResultados() {
        CuentaCreada origen = crearCuentaActiva("1000.00");
        CuentaCreada destino = crearCuentaActiva("0.00");
        for (int i = 1; i <= 3; i++) {
            transferir(origen, destino, i + ".00").then().statusCode(201);
        }

        given()
                .queryParam("cuentaOrigen", origen.numero())
                .queryParam("pagina", 1)
                .queryParam("tamanio", 2)
                .when().get(HISTORIAL)
                .then()
                .statusCode(200)
                .body("contenido.monto", contains(1.0f))
                .body("totalElementos", equalTo(3))
                .body("totalPaginas", equalTo(2));
    }

    @Test
    void historialDebeRechazarFiltrosInvalidos() {
        given()
                .queryParam("cuentaOrigen", "123")
                .queryParam("fechaDesde", "2026-10-24")
                .queryParam("fechaHasta", "2026-10-01")
                .when().get(HISTORIAL)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("errores.campo", hasItem("cuentaOrigen"))
                .body("errores.campo", hasItem("rangoFechasValido"));
    }

    @Test
    void historialDebeResponder400ConEstadoOFechaConFormatoInvalido() {
        given()
                .queryParam("estado", "APROBADA")
                .when().get(HISTORIAL)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("errores[0].campo", equalTo("estado"))
                .body("errores[0].mensaje",
                        equalTo("Valor no permitido. Valores aceptados: COMPLETADA, RECHAZADA, EN_REVISION"));

        given()
                .queryParam("fechaDesde", "24/10/2026")
                .when().get(HISTORIAL)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("errores[0].campo", equalTo("fechaDesde"))
                .body("errores[0].mensaje", equalTo("Debe ser una fecha con formato yyyy-MM-dd"));
    }

    @Test
    void resumenDebeReflejarLaActividadDelDia() {
        CuentaCreada origen = crearCuentaActiva("20000.00");
        CuentaCreada destino = crearCuentaActiva("0.00");
        transferir(origen, destino, "6000.00").then().statusCode(201);
        transferir(origen, destino, "1000.00").then().statusCode(201);
        transferir(origen, crearCuenta("0.00", "INACTIVA"), "50.00").then().statusCode(422);

        given()
                .when().get("/api/cuentas/" + origen.id() + "/resumen")
                .then()
                .statusCode(200)
                .body("numeroCuenta", equalTo(origen.numero()))
                .body("saldoActual", equalTo(13000.0f))
                .body("totalTransferidoHoy", equalTo(7000.0f))
                .body("montoDisponibleHoy", equalTo(3000.0f))
                .body("cantidadTransferenciasHoy", equalTo(2))
                .body("cantidadSospechosasHoy", equalTo(1))
                .body("cantidadSospechosasTotal", equalTo(1));
    }

    @Test
    void resumenDeCuentaInexistenteDebeResponder404() {
        given()
                .when().get("/api/cuentas/999999999/resumen")
                .then()
                .statusCode(404)
                .body("codigo", equalTo("CUENTA_NO_ENCONTRADA"));
    }
}
