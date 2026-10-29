package com.turnos.qa;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Las pruebas levantan la aplicación real en el puerto 8080 (el mismo que usa el front),
 * con una base SQLite propia de pruebas (application-test.properties).
 * Antes de correrlas, detener cualquier back que esté usando el 8080.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfig {
}
