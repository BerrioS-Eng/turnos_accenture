package com.turnos.service;

import com.turnos.dto.CancelarTurnoResponse;
import com.turnos.dto.EstadoTurnoResponse;
import com.turnos.dto.LlamarTurnoResponse;
import com.turnos.dto.MetricasFilaResponse;
import com.turnos.dto.SolicitarTurnoResponse;
import com.turnos.exception.FilaVaciaException;
import com.turnos.model.Turno;
import com.turnos.repository.TurnoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TurnoServiceTest {

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private TurnoRepository turnoRepository;

    @BeforeEach
    void setUp() {
        turnoRepository.deleteAll();
    }

    @Test
    @DisplayName("solicitarTurno debe asignar el siguiente número secuencial y calcular turnos por delante")
    void debeSolicitarTurnoSecuencial() {
        turnoRepository.save(Turno.builder().numero(10).horaSolicitud(LocalTime.of(8, 45)).estado("pendiente").build());

        SolicitarTurnoResponse response = turnoService.solicitarTurno();

        assertThat(response.getNumero()).isEqualTo(11);
        assertThat(response.getEstado()).isEqualTo("pendiente");
        assertThat(response.getTurnosPorDelante()).isEqualTo(1);
    }

    @Test
    @DisplayName("solicitarTurno después del 999 debe reiniciar la numeración en 1")
    void debeReiniciarNumeracionDespuesDe999() {
        turnoRepository.save(Turno.builder().numero(999).horaSolicitud(LocalTime.of(8, 50)).estado("pendiente").build());

        SolicitarTurnoResponse response = turnoService.solicitarTurno();

        assertThat(response.getNumero()).isEqualTo(1);
    }

    @Test
    @DisplayName("llamarSiguiente debe atender estrictamente según FIFO (horaSolicitud ASC)")
    void debeLlamarSegunFifo() {
        turnoRepository.save(Turno.builder().numero(9).horaSolicitud(LocalTime.of(8, 41)).estado("pendiente").build());
        turnoRepository.save(Turno.builder().numero(6).horaSolicitud(LocalTime.of(8, 25)).estado("pendiente").build());
        turnoRepository.save(Turno.builder().numero(7).horaSolicitud(LocalTime.of(8, 30)).estado("cancelado").build());
        turnoRepository.save(Turno.builder().numero(7).horaSolicitud(LocalTime.of(8, 34)).estado("pendiente").build());

        LlamarTurnoResponse llamada1 = turnoService.llamarSiguiente();
        assertThat(llamada1.getNumero()).isEqualTo(6);
        assertThat(llamada1.getEstado()).isEqualTo("atendido");

        LlamarTurnoResponse llamada2 = turnoService.llamarSiguiente();
        // Debe llamar al 7 pendiente (08:34), ignorando el 7 cancelado (08:30)
        assertThat(llamada2.getNumero()).isEqualTo(7);

        LlamarTurnoResponse llamada3 = turnoService.llamarSiguiente();
        assertThat(llamada3.getNumero()).isEqualTo(9);
    }

    @Test
    @DisplayName("llamarSiguiente con fila vacía debe lanzar FilaVaciaException")
    void debeLanzarExcepcionConFilaVacia() {
        assertThatThrownBy(() -> turnoService.llamarSiguiente())
                .isInstanceOf(FilaVaciaException.class)
                .hasMessageContaining("No hay turnos pendientes");
    }

    @Test
    @DisplayName("consultarEstado debe devolver métricas correctas de posición y ventanilla")
    void debeConsultarEstadoCorrectamente() {
        turnoRepository.save(Turno.builder().numero(5).horaSolicitud(LocalTime.of(8, 21)).horaAtencion(LocalTime.of(8, 40)).estado("atendido").build());
        turnoRepository.save(Turno.builder().numero(6).horaSolicitud(LocalTime.of(8, 25)).estado("pendiente").build());
        Turno turno9 = turnoRepository.save(Turno.builder().numero(9).horaSolicitud(LocalTime.of(8, 41)).estado("pendiente").build());

        EstadoTurnoResponse estado = turnoService.consultarEstado(9);

        assertThat(estado.getNumero()).isEqualTo(9);
        assertThat(estado.getTurnoActual()).isEqualTo(5);
        assertThat(estado.getTurnosPorDelante()).isEqualTo(1); // solo el 6 está antes del 9
        assertThat(estado.getTiempoEstimadoMinutos()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("cancelarTurno debe pasar el turno a cancelado y excluirlo de la fila")
    void debeCancelarTurno() {
        turnoRepository.save(Turno.builder().numero(11).horaSolicitud(LocalTime.of(9, 0)).estado("pendiente").build());

        CancelarTurnoResponse response = turnoService.cancelarTurno(11);

        assertThat(response.getNumero()).isEqualTo(11);
        assertThat(response.getEstado()).isEqualTo("cancelado");

        MetricasFilaResponse metricas = turnoService.obtenerMetricasFila();
        assertThat(metricas.getPersonasEsperando()).isEqualTo(0);
        assertThat(metricas.getTotalCancelados()).isEqualTo(1);
    }

    @Test
    @DisplayName("calcularPromedioEsperaMinutos debe calcular el promedio exacto de los atendidos")
    void debeCalcularPromedioCorrectamente() {
        // Turno 1: 3 min (08:02 a 08:05)
        turnoRepository.save(Turno.builder().numero(1).horaSolicitud(LocalTime.of(8, 2)).horaAtencion(LocalTime.of(8, 5)).estado("atendido").build());
        // Turno 2: 9 min (08:06 a 08:15)
        turnoRepository.save(Turno.builder().numero(2).horaSolicitud(LocalTime.of(8, 6)).horaAtencion(LocalTime.of(8, 15)).estado("atendido").build());
        // Turno 3: 14 min (08:10 a 08:24)
        turnoRepository.save(Turno.builder().numero(3).horaSolicitud(LocalTime.of(8, 10)).horaAtencion(LocalTime.of(8, 24)).estado("atendido").build());
        // Turno 4: 16 min (08:15 a 08:31)
        turnoRepository.save(Turno.builder().numero(4).horaSolicitud(LocalTime.of(8, 15)).horaAtencion(LocalTime.of(8, 31)).estado("atendido").build());
        // Turno 5: 19 min (08:21 a 08:40)
        turnoRepository.save(Turno.builder().numero(5).horaSolicitud(LocalTime.of(8, 21)).horaAtencion(LocalTime.of(8, 40)).estado("atendido").build());

        // (3 + 9 + 14 + 16 + 19) / 5 = 61 / 5 = 12.2 min
        double promedio = turnoService.calcularPromedioEsperaMinutos();
        assertThat(promedio).isEqualTo(12.2);
    }
}
