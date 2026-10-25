package com.netby.evaluacion.resource;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static io.restassured.RestAssured.given;

final class ApiDePrueba {

    private static final AtomicLong SECUENCIA = new AtomicLong(System.nanoTime() % 1_000_000_000L);

    private ApiDePrueba() {
    }

    record CuentaCreada(long id, String numero) {
    }

    static String nuevoNumeroCuenta() {
        return String.format("%010d", SECUENCIA.incrementAndGet());
    }

    static CuentaCreada crearCuenta(String saldoInicial, String estado) {
        String numero = nuevoNumeroCuenta();
        Number id = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "numeroCuenta", numero,
                        "titular", "Titular " + numero,
                        "tipo", "AHORRO",
                        "saldoInicial", new BigDecimal(saldoInicial),
                        "estado", estado))
                .post("/api/cuentas")
                .then().statusCode(201)
                .extract().path("id");
        return new CuentaCreada(id.longValue(), numero);
    }

    static CuentaCreada crearCuentaActiva(String saldoInicial) {
        return crearCuenta(saldoInicial, "ACTIVA");
    }

    static Response transferir(CuentaCreada origen, CuentaCreada destino, String monto) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "numeroCuentaOrigen", origen.numero(),
                        "numeroCuentaDestino", destino.numero(),
                        "monto", new BigDecimal(monto)))
                .post("/api/transferencias");
    }

    static BigDecimal saldoDe(CuentaCreada cuenta) {
        String saldo = given()
                .get("/api/cuentas/" + cuenta.id())
                .then().statusCode(200)
                .extract().jsonPath().getString("saldo");
        return new BigDecimal(saldo);
    }
}
