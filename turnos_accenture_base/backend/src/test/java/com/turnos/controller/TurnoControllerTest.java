package com.turnos.controller;

import com.turnos.dto.CancelarTurnoResponse;
import com.turnos.dto.EstadoTurnoResponse;
import com.turnos.dto.LlamarTurnoResponse;
import com.turnos.dto.MetricasFilaResponse;
import com.turnos.dto.SolicitarTurnoResponse;
import com.turnos.exception.FilaVaciaException;
import com.turnos.exception.TurnoNoEncontradoException;
import com.turnos.model.Turno;
import com.turnos.service.TurnoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TurnoController.class)
class TurnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TurnoService turnoService;

    @Test
    @DisplayName("POST /api/turnos/solicitar debe retornar 200 y contrato esperado")
    void debeSolicitarTurno() throws Exception {
        SolicitarTurnoResponse response = SolicitarTurnoResponse.builder()
                .id(12L)
                .numero(11)
                .horaSolicitud(LocalTime.of(9, 0, 0))
                .estado("pendiente")
                .turnosPorDelante(5)
                .tiempoEstimadoMinutos(61.0)
                .build();

        given(turnoService.solicitarTurno()).willReturn(response);

        mockMvc.perform(post("/api/turnos/solicitar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.numero").value(11))
                .andExpect(jsonPath("$.estado").value("pendiente"))
                .andExpect(jsonPath("$.turnosPorDelante").value(5))
                .andExpect(jsonPath("$.tiempoEstimadoMinutos").value(61.0));
    }

    @Test
    @DisplayName("GET /api/turnos/{numero}/estado debe retornar 200 con estado del turno")
    void debeConsultarEstado() throws Exception {
        EstadoTurnoResponse response = EstadoTurnoResponse.builder()
                .numero(11)
                .estado("pendiente")
                .turnoActual(5)
                .turnosPorDelante(4)
                .tiempoEstimadoMinutos(48.8)
                .build();

        given(turnoService.consultarEstado(11)).willReturn(response);

        mockMvc.perform(get("/api/turnos/11/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(11))
                .andExpect(jsonPath("$.estado").value("pendiente"))
                .andExpect(jsonPath("$.turnoActual").value(5))
                .andExpect(jsonPath("$.turnosPorDelante").value(4));
    }

    @Test
    @DisplayName("POST /api/turnos/llamar-siguiente debe retornar 200 cuando hay turnos pendientes")
    void debeLlamarSiguiente() throws Exception {
        LlamarTurnoResponse response = LlamarTurnoResponse.builder()
                .id(6L)
                .numero(6)
                .horaAtencion(LocalTime.of(8, 52, 10))
                .estado("atendido")
                .build();

        given(turnoService.llamarSiguiente()).willReturn(response);

        mockMvc.perform(post("/api/turnos/llamar-siguiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(6))
                .andExpect(jsonPath("$.estado").value("atendido"))
                .andExpect(jsonPath("$.horaAtencion").value("08:52:10"));
    }

    @Test
    @DisplayName("POST /api/turnos/llamar-siguiente debe retornar 409 Conflict si la fila está vacía")
    void debeRetornar409CuandoFilaVacia() throws Exception {
        given(turnoService.llamarSiguiente()).willThrow(new FilaVaciaException("Fila vacía"));

        mockMvc.perform(post("/api/turnos/llamar-siguiente"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/turnos/metricas-fila debe retornar métricas de ventanilla")
    void debeObtenerMetricas() throws Exception {
        MetricasFilaResponse response = MetricasFilaResponse.builder()
                .personasEsperando(5)
                .totalAtendidos(5)
                .totalCancelados(1)
                .turnoActual(5)
                .promedioEsperaMinutos(12.2)
                .build();

        given(turnoService.obtenerMetricasFila()).willReturn(response);

        mockMvc.perform(get("/api/turnos/metricas-fila"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personasEsperando").value(5))
                .andExpect(jsonPath("$.totalAtendidos").value(5))
                .andExpect(jsonPath("$.totalCancelados").value(1))
                .andExpect(jsonPath("$.turnoActual").value(5))
                .andExpect(jsonPath("$.promedioEsperaMinutos").value(12.2));
    }

    @Test
    @DisplayName("PUT /api/turnos/{numero}/cancelar debe retornar 200 con turno cancelado")
    void debeCancelarTurno() throws Exception {
        CancelarTurnoResponse response = CancelarTurnoResponse.builder()
                .numero(11)
                .estado("cancelado")
                .mensaje("Turno cancelado exitosamente")
                .build();

        given(turnoService.cancelarTurno(11)).willReturn(response);

        mockMvc.perform(put("/api/turnos/11/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(11))
                .andExpect(jsonPath("$.estado").value("cancelado"))
                .andExpect(jsonPath("$.mensaje").value("Turno cancelado exitosamente"));
    }

    @Test
    @DisplayName("GET /api/turnos debe listar todos los turnos")
    void debeListarTurnos() throws Exception {
        Turno t1 = Turno.builder().id(1L).numero(1).estado("atendido").build();
        Turno t2 = Turno.builder().id(2L).numero(2).estado("pendiente").build();

        given(turnoService.listarTurnos()).willReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/turnos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
