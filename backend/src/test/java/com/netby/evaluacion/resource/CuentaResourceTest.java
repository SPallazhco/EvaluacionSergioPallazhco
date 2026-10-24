package com.netby.evaluacion.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class CuentaResourceTest {

    private static final String RUTA = "/api/cuentas";
    private static final String PROBLEM_JSON = "application/problem+json";

    /** Genera números únicos para que las pruebas no dependan del orden ni de datos previos. */
    private static final AtomicLong SECUENCIA = new AtomicLong(System.currentTimeMillis() % 1_000_000_000L);

    private static String nuevoNumeroCuenta() {
        return String.format("%010d", SECUENCIA.incrementAndGet());
    }

    private static Map<String, Object> cuenta(String numeroCuenta) {
        return Map.of(
                "numeroCuenta", numeroCuenta,
                "titular", "Ana Pérez",
                "tipo", "AHORRO",
                "saldoInicial", 1500.50,
                "estado", "ACTIVA");
    }

    @Test
    void registrarDebeCrearLaCuentaYResponder201ConLocation() {
        String numero = nuevoNumeroCuenta();

        int id = given()
                .contentType(ContentType.JSON)
                .body(cuenta(numero))
                .when().post(RUTA)
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("id", notNullValue())
                .body("numeroCuenta", equalTo(numero))
                .body("saldo", equalTo(1500.50f))
                .body("estado", equalTo("ACTIVA"))
                .body("fechaCreacion", notNullValue())
                .extract().path("id");

        given()
                .when().get(RUTA + "/" + id)
                .then()
                .statusCode(200)
                .body("numeroCuenta", equalTo(numero));
    }

    @Test
    void registrarDebeIncluirElIdEnLaCabeceraLocation() {
        ExtractableResponse<Response> respuesta = given()
                .contentType(ContentType.JSON)
                .body(cuenta(nuevoNumeroCuenta()))
                .when().post(RUTA)
                .then()
                .statusCode(201)
                .extract();

        int id = respuesta.path("id");
        assertThat(respuesta.header("Location"), endsWith(RUTA + "/" + id));
    }

    @Test
    void registrarDebeResponder409CuandoElNumeroDeCuentaYaExiste() {
        String numero = nuevoNumeroCuenta();
        given().contentType(ContentType.JSON).body(cuenta(numero)).post(RUTA).then().statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(cuenta(numero))
                .when().post(RUTA)
                .then()
                .statusCode(409)
                .contentType(PROBLEM_JSON)
                .body("status", equalTo(409))
                .body("codigo", equalTo("CUENTA_DUPLICADA"))
                .body("detail", equalTo("Ya existe una cuenta con el número " + numero));
    }

    @Test
    void registrarDebeResponder400ConElDetalleDeCadaCampoInvalido() {
        Map<String, Object> invalida = Map.of(
                "numeroCuenta", "12345",
                "titular", "",
                "tipo", "AHORRO",
                "saldoInicial", -10,
                "estado", "ACTIVA");

        given()
                .contentType(ContentType.JSON)
                .body(invalida)
                .when().post(RUTA)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("SOLICITUD_INVALIDA"))
                .body("errores.campo", containsInAnyOrder("numeroCuenta", "saldoInicial", "titular"));
    }

    @Test
    void registrarDebeResponder400CuandoElCuerpoEsVacio() {
        given()
                .contentType(ContentType.JSON)
                .when().post(RUTA)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("SOLICITUD_INVALIDA"));
    }

    @Test
    void registrarDebeResponder400CuandoElJsonEstaMalFormado() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"numeroCuenta\": ")
                .when().post(RUTA)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("SOLICITUD_INVALIDA"));
    }

    @Test
    void registrarDebeResponder400CuandoElTipoDeCuentaNoExiste() {
        Map<String, Object> invalida = Map.of(
                "numeroCuenta", nuevoNumeroCuenta(),
                "titular", "Ana Pérez",
                "tipo", "PLAZO_FIJO",
                "saldoInicial", 10,
                "estado", "ACTIVA");

        given()
                .contentType(ContentType.JSON)
                .body(invalida)
                .when().post(RUTA)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("SOLICITUD_INVALIDA"))
                .body("errores[0].campo", equalTo("tipo"))
                .body("errores[0].mensaje", equalTo("Valor no permitido. Valores aceptados: AHORRO, CORRIENTE"));
    }

    @Test
    void registrarDebeResponder400CuandoHayPropiedadesDesconocidas() {
        String json = """
                {"numeroCuenta": "%s", "titular": "Ana Pérez", "tipo": "AHORRO",
                 "saldoInicial": 10, "estado": "ACTIVA", "saldo": 999999}
                """.formatted(nuevoNumeroCuenta());

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post(RUTA)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("errores[0].campo", equalTo("saldo"))
                .body("errores[0].mensaje", equalTo("Propiedad no reconocida"));
    }

    @Test
    void listarDebeRetornarLaPaginaConLasCuentasRegistradas() {
        String numero = nuevoNumeroCuenta();
        given().contentType(ContentType.JSON).body(cuenta(numero)).post(RUTA).then().statusCode(201);

        given()
                .queryParam("pagina", 0)
                .queryParam("tamanio", 100)
                .when().get(RUTA)
                .then()
                .statusCode(200)
                .body("pagina", equalTo(0))
                .body("tamanio", equalTo(100))
                .body("totalElementos", greaterThanOrEqualTo(1))
                .body("contenido.numeroCuenta", hasItem(numero));
    }

    @Test
    void listarDebeResponder400CuandoLaPaginacionEsInvalida() {
        given()
                .queryParam("pagina", -1)
                .queryParam("tamanio", 500)
                .when().get(RUTA)
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("errores.campo", containsInAnyOrder("pagina", "tamanio"));
    }

    @Test
    void obtenerPorIdDebeResponder404CuandoLaCuentaNoExiste() {
        given()
                .when().get(RUTA + "/999999999")
                .then()
                .statusCode(404)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("CUENTA_NO_ENCONTRADA"));
    }

    @Test
    void rutaInexistenteDebeResponder404ConFormatoProblema() {
        given()
                .when().get("/api/no-existe")
                .then()
                .statusCode(404)
                .contentType(PROBLEM_JSON)
                .body("codigo", equalTo("RECURSO_NO_ENCONTRADO"));
    }
}
