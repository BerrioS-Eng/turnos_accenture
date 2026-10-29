package com.turnos.qa.steps;

import io.cucumber.java.Before;
import io.cucumber.java.es.Dado;
import io.restassured.RestAssured;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/** Prepara un estado conocido de la base antes de cada escenario: pruebas independientes y repetibles. */
public class ComunesSteps {

    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbc;
    @LocalServerPort private int puerto;

    @Before
    public void apuntarApiALaAplicacion() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = puerto;
    }

    @Dado("que la fila tiene los turnos registrados en la mañana")
    public void filaConDatosManana() {
        jdbc.update("DELETE FROM turno");
        new ResourceDatabasePopulator(new ClassPathResource("qa/datos_manana.sql")).execute(dataSource);
    }

    @Dado("que la fila está vacía")
    public void filaVacia() {
        jdbc.update("DELETE FROM turno WHERE estado = 'pendiente'");
    }

    @Dado("que el último turno emitido fue el {int}")
    public void ultimoTurnoEmitido(int numero) {
        jdbc.update("INSERT INTO turno (numero, hora_solicitud, estado) VALUES (?, '08:50:00', 'pendiente')", numero);
    }
}
