package com.turnos.qa.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.turnos.qa.support.ApiTurnos;
import io.cucumber.java.ParameterType;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FilaApiSteps {

    private Response ultimaRespuesta;
    private final List<Integer> llamados = new ArrayList<>();
    private final List<Integer> asignados = new ArrayList<>();

    // ---------- Tipos de parámetro ----------

    /** "6, 7, 8 y 9" -> [6, 7, 8, 9] */
    @ParameterType("\\d+(?:(?:, | y )\\d+)*")
    public List<Integer> listaNumeros(String texto) {
        return Arrays.stream(texto.split(", | y ")).map(Integer::parseInt).toList();
    }

    /** Acepta "12,2" o "12.2" (con # language: es, {double} usaría la coma como decimal). */
    @ParameterType("\\d+(?:[.,]\\d+)?")
    public Double decimal(String texto) {
        return Double.parseDouble(texto.replace(',', '.'));
    }

    // ---------- Acciones ----------

    @Cuando("el asesor llama al siguiente turno")
    public void llamaAlSiguiente() {
        ultimaRespuesta = ApiTurnos.llamarSiguiente();
        if (ultimaRespuesta.statusCode() == 200) {
            llamados.add(ultimaRespuesta.jsonPath().getInt("numero"));
        }
    }

    @Cuando("el asesor llama al siguiente turno {int} veces")
    public void llamaVariasVeces(int veces) {
        for (int i = 0; i < veces; i++) {
            llamaAlSiguiente();
            assertThat(ultimaRespuesta.statusCode()).as("llamada #%d", i + 1).isEqualTo(200);
        }
    }

    @Cuando("{int} asesores llaman al siguiente turno al mismo tiempo")
    public void asesoresSimultaneos(int asesores) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(asesores);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Response>> futuros = new ArrayList<>();
        for (int i = 0; i < asesores; i++) {
            Callable<Response> tarea = () -> { salida.await(); return ApiTurnos.llamarSiguiente(); };
            futuros.add(pool.submit(tarea));
        }
        salida.countDown(); // todos arrancan a la vez
        for (Future<Response> f : futuros) {
            Response r = f.get();
            assertThat(r.statusCode()).isEqualTo(200);
            llamados.add(r.jsonPath().getInt("numero"));
        }
        pool.shutdown();
    }

    @Cuando("un cliente pide un turno")
    public void clientePideTurno() {
        ultimaRespuesta = ApiTurnos.solicitar();
        assertThat(ultimaRespuesta.statusCode()).as("POST /api/turnos/solicitar").isIn(200, 201);
        asignados.add(ultimaRespuesta.jsonPath().getInt("numero"));
    }

    @Cuando("{int} clientes piden turno")
    public void variosClientesPidenTurno(int clientes) {
        for (int i = 0; i < clientes; i++) clientePideTurno();
    }

    @Cuando("el cliente consulta el estado del turno {int}")
    public void consultaEstado(int numero) {
        ultimaRespuesta = ApiTurnos.estado(numero);
        assertThat(ultimaRespuesta.statusCode()).as("GET /api/turnos/%d/estado", numero).isEqualTo(200);
    }

    // ---------- Verificaciones: métricas de la fila ----------

    private Response metricas() {
        Response r = ApiTurnos.metricas();
        assertThat(r.statusCode()).as("GET /api/turnos/metricas-fila").isEqualTo(200);
        return r;
    }

    @Entonces("hay {int} personas esperando")
    public void personasEsperando(int esperadas) {
        assertThat(metricas().jsonPath().getInt("personasEsperando")).isEqualTo(esperadas);
    }

    @Entonces("el turno actual en ventanilla es el {int}")
    public void turnoActual(int numero) {
        assertThat(metricas().jsonPath().getInt("turnoActual")).isEqualTo(numero);
    }

    @Entonces("hay {int} atendidos y {int} cancelado(s)")
    public void totales(int atendidos, int cancelados) {
        Response r = metricas();
        assertThat(r.jsonPath().getInt("totalAtendidos")).isEqualTo(atendidos);
        assertThat(r.jsonPath().getInt("totalCancelados")).isEqualTo(cancelados);
    }

    @Entonces("la espera promedio es {decimal} minutos")
    public void esperaPromedio(Double minutos) {
        assertThat(metricas().jsonPath().getDouble("promedioEsperaMinutos")).isCloseTo(minutos, within(0.05));
    }

    // ---------- Verificaciones: llamar al siguiente ----------

    @Entonces("se llama el turno {int}")
    public void seLlamaElTurno(int numero) {
        assertThat(ultimaRespuesta.statusCode()).isEqualTo(200);
        assertThat(ultimaRespuesta.jsonPath().getInt("numero")).isEqualTo(numero);
        assertThat(ultimaRespuesta.jsonPath().getString("estado")).isEqualTo("atendido");
        assertThat(ultimaRespuesta.jsonPath().getString("horaAtencion")).isNotBlank();
    }

    @Entonces("los turnos llamados son {listaNumeros}")
    public void turnosLlamados(List<Integer> esperados) {
        assertThat(llamados).containsExactlyElementsOf(esperados);
    }

    @Entonces("se llaman los turnos {listaNumeros} sin repetir")
    public void llamadosSinRepetir(List<Integer> esperados) {
        assertThat(llamados).doesNotHaveDuplicates().containsExactlyInAnyOrderElementsOf(esperados);
    }

    @Entonces("el sistema rechaza la llamada porque la fila está vacía")
    public void filaVaciaRechazada() {
        // El contrato no fija el código: se acepta cualquier 4xx (recomendado 409 Conflict)
        assertThat(ultimaRespuesta.statusCode()).as("llamar con fila vacía").isBetween(400, 499);
    }

    // ---------- Verificaciones: pedir turno / estado ----------

    @Entonces("recibe el turno {int} en estado pendiente")
    public void recibeElTurno(int numero) {
        assertThat(ultimaRespuesta.jsonPath().getInt("numero")).isEqualTo(numero);
        assertThat(ultimaRespuesta.jsonPath().getString("estado")).isEqualTo("pendiente");
    }

    @Entonces("tiene {int} turnos por delante")
    public void turnosPorDelante(int delante) {
        assertThat(ultimaRespuesta.jsonPath().getInt("turnosPorDelante")).isEqualTo(delante);
    }

    @Entonces("el tiempo estimado es de unos {int} minutos")
    public void tiempoEstimado(int minutos) {
        // Personas por delante x promedio histórico; se tolera ±1 min por redondeo
        assertThat(ultimaRespuesta.jsonPath().getDouble("tiempoEstimadoMinutos")).isCloseTo(minutos, within(1.0));
    }

    @Entonces("ve que en ventanilla están atendiendo el {int}")
    public void veTurnoActual(int numero) {
        assertThat(ultimaRespuesta.jsonPath().getInt("turnoActual")).isEqualTo(numero);
    }

    @Entonces("reciben los turnos {listaNumeros}")
    public void recibenLosTurnos(List<Integer> esperados) {
        assertThat(asignados).doesNotHaveDuplicates().containsExactlyElementsOf(esperados);
    }

    @Entonces("la lista general tiene {int} turnos")
    public void listaGeneral(int total) {
        Response r = ApiTurnos.listar();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("$")).hasSize(total);
    }
}
