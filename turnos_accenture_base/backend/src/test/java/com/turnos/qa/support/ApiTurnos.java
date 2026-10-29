package com.turnos.qa.support;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/** Contrato de la API acordado por el equipo. Si cambia una ruta, se ajusta solo aquí. */
public final class ApiTurnos {
    private ApiTurnos() {}

    public static Response solicitar() {
        return given().contentType(ContentType.JSON).post("/api/turnos/solicitar");
    }

    public static Response estado(int numero) {
        return given().get("/api/turnos/" + numero + "/estado");
    }

    public static Response llamarSiguiente() {
        return given().post("/api/turnos/llamar-siguiente");
    }

    public static Response metricas() {
        return given().get("/api/turnos/metricas-fila");
    }

    public static Response listar() {
        return given().get("/api/turnos");
    }
}
