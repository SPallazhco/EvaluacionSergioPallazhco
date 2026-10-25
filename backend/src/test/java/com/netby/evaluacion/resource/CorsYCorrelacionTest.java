package com.netby.evaluacion.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class CorsYCorrelacionTest {

    private static final String FRONTEND = "http://localhost:4200";

    @Test
    void elFrontendAngularDebePoderHacerUnaSolicitudPreflight() {
        given()
                .header("Origin", FRONTEND)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type")
                .when().options("/api/transferencias")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", equalTo(FRONTEND))
                .header("Access-Control-Allow-Methods", containsString("POST"));
    }

    @Test
    void elFrontendDebePoderLeerLasCabecerasLocationYRequestId() {
        given()
                .header("Origin", FRONTEND)
                .when().get("/api/cuentas")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", equalTo(FRONTEND))
                .header("Access-Control-Expose-Headers", containsString("Location"))
                .header("Access-Control-Expose-Headers", containsString("X-Request-Id"));
    }

    @Test
    void unOrigenNoAutorizadoNoDebeRecibirPermisoCors() {
        given()
                .header("Origin", "https://sitio-malicioso.example")
                .when().get("/api/cuentas")
                .then()
                .header("Access-Control-Allow-Origin", nullValue());
    }

    @Test
    void cadaRespuestaDebeIncluirUnIdDeCorrelacion() {
        given()
                .when().get("/api/cuentas")
                .then()
                .statusCode(200)
                .header("X-Request-Id", matchesPattern("[0-9a-f-]{36}"));
    }

    @Test
    void debeReutilizarElIdDeCorrelacionEnviadoPorElCliente() {
        given()
                .header("X-Request-Id", "frontend-abc-123")
                .when().get("/api/cuentas/999999999")
                .then()
                .statusCode(404)
                .header("X-Request-Id", equalTo("frontend-abc-123"));
    }

    @Test
    void debeDescartarUnIdDeCorrelacionConCaracteresNoPermitidos() {
        given()
                .header("X-Request-Id", "id con espacios\ny saltos")
                .when().get("/api/cuentas")
                .then()
                .header("X-Request-Id", matchesPattern("[0-9a-f-]{36}"));
    }
}
