package com.netby.evaluacion.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class DocumentacionOpenApiTest {

    private JsonPath documento;

    @BeforeEach
    void obtenerDocumento() {
        documento = given().queryParam("format", "json")
                .get("/q/openapi")
                .then().statusCode(200)
                .extract().jsonPath();
    }

    @Test
    void debeIncluirLaInformacionGeneralDeLaApi() {
        assertThat(documento.getString("info.title")).isEqualTo("API de Transferencias Interbancarias");
        assertThat(documento.getString("info.description")).contains("RFC 7807");
    }

    @ParameterizedTest(name = "{0} {1} documenta {2}")
    @CsvSource(delimiter = '|', value = {
            "/api/cuentas                | post | 201,400,409,500",
            "/api/cuentas                | get  | 200,400",
            "/api/cuentas/{id}           | get  | 200,404",
            "/api/cuentas/{id}/resumen   | get  | 200,404",
            "/api/transferencias         | post | 201,400,404,422,500",
            "/api/transferencias         | get  | 200,400",
            "/api/transferencias/{id}    | get  | 200,404"
    })
    void cadaEndpointDebeDocumentarSusRespuestasYDescripcion(String ruta, String metodo, String codigos) {
        Map<String, Object> operacion = documento.getMap("paths.'" + ruta + "'." + metodo);

        assertThat(operacion).containsKeys("summary", "description", "responses");
        assertThat(documento.getMap("paths.'" + ruta + "'." + metodo + ".responses"))
                .containsKeys(codigos.split(","));
    }

    @Test
    void elRegistroDeTransferenciasDebeIncluirEjemplosDeLosErroresDeNegocio() {
        Map<String, Object> ejemplos = documento.getMap(
                "paths.'/api/transferencias'.post.responses.'422'.content.'application/problem+json'.examples");

        assertThat(ejemplos).containsKeys("Saldo insuficiente", "Cuenta inactiva", "Límite diario");
    }

    @Test
    void swaggerUiDebeEstarDisponible() {
        given().get("/q/swagger-ui/").then().statusCode(200);
    }
}
