package com.turnos.qa.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.turnos.qa.support.QaConfig;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Pruebas E2E en Chrome. Acuerdo con el front: los elementos se ubican por data-testid (ver README),
 * así las pruebas no se rompen si cambia el diseño.
 */
public class PantallasSteps {

    private WebDriver driver;
    private WebDriverWait espera;

    private WebDriver driver() {
        if (driver == null) {
            ChromeOptions opciones = new ChromeOptions();
            if (QaConfig.headless()) opciones.addArguments("--headless=new");
            opciones.addArguments("--window-size=1280,900");
            driver = new ChromeDriver(opciones); // Selenium Manager descarga el driver
            espera = new WebDriverWait(driver, Duration.ofSeconds(5));
        }
        return driver;
    }

    private By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    private WebElement el(String id) {
        driver();
        return espera.until(ExpectedConditions.visibilityOfElementLocated(testId(id)));
    }

    private void contiene(String id, String texto) {
        driver();
        espera.until(ExpectedConditions.textToBePresentInElementLocated(testId(id), texto));
        assertThat(el(id).getText()).contains(texto);
    }

    @After("@ui")
    public void cerrar(Scenario escenario) {
        if (driver != null) {
            escenario.attach(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES), "image/png", "pantalla final");
            driver.quit();
        }
    }

    // ---------- Usuario (móvil) ----------

    @Cuando("el cliente abre la pantalla para pedir turno")
    public void abrePantallaCliente() {
        driver().get(QaConfig.frontUrl() + "/usuario");
    }

    @Cuando("pide un turno")
    public void pideUnTurno() {
        el("btn-solicitar-turno").click();
    }

    @Entonces("ve que le asignaron el turno {int}")
    public void veSuTurno(int numero) {
        contiene("turno-asignado", String.valueOf(numero));
    }

    @Entonces("ve que tiene {int} personas delante")
    public void vePersonasDelante(int delante) {
        contiene("turnos-por-delante", String.valueOf(delante));
    }

    @Entonces("ve el tiempo estimado de espera")
    public void veTiempoEstimado() {
        assertThat(el("tiempo-estimado").getText()).isNotBlank();
    }

    // ---------- Atendedor (ventanilla) ----------

    @Cuando("el asesor abre su pantalla")
    public void abrePantallaAsesor() {
        driver().get(QaConfig.frontUrl() + "/atendedor");
    }

    @Cuando("llama al siguiente turno desde la pantalla")
    public void llamaDesdePantalla() {
        el("btn-llamar-siguiente").click();
    }

    @Entonces("ve {int} personas esperando")
    public void vePersonasEsperando(int cantidad) {
        contiene("personas-esperando", String.valueOf(cantidad));
    }

    @Entonces("ve el turno {int} en ventanilla")
    public void veTurnoActual(int numero) {
        contiene("turno-actual", String.valueOf(numero));
    }

    @Entonces("ve la tabla con {int} turnos")
    public void veTabla(int filas) {
        el("tabla-turnos");
        espera.until(ExpectedConditions.numberOfElementsToBe(testId("fila-turno"), filas));
    }

    @Entonces("ve un mensaje de fila vacía")
    public void veMensajeFilaVacia() {
        assertThat(el("mensaje-error").getText()).isNotBlank();
    }
}
