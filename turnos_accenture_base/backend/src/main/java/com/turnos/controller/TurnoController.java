package com.turnos.controller;

import com.turnos.dto.CancelarTurnoResponse;
import com.turnos.dto.EstadoTurnoResponse;
import com.turnos.dto.LlamarTurnoResponse;
import com.turnos.dto.MetricasFilaResponse;
import com.turnos.dto.SolicitarTurnoResponse;
import com.turnos.model.Turno;
import com.turnos.service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de turnos de la fila virtual.
 * Proporciona endpoints para clientes móviles y para el atendedor de ventanilla.
 */
@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Turnos", description = "Endpoints de la fila virtual de turnos")
public class TurnoController {

    private final TurnoService turnoService;

    @Operation(summary = "Solicita un nuevo turno en la fila")
    @PostMapping("/solicitar")
    public ResponseEntity<SolicitarTurnoResponse> solicitarTurno() {
        return ResponseEntity.ok(turnoService.solicitarTurno());
    }

    @Operation(summary = "Consulta el estado y tiempo estimado de un turno")
    @GetMapping("/{numero}/estado")
    public ResponseEntity<EstadoTurnoResponse> consultarEstado(@PathVariable int numero) {
        return ResponseEntity.ok(turnoService.consultarEstado(numero));
    }

    @Operation(summary = "Llama al siguiente turno en la fila según política FIFO")
    @PostMapping("/llamar-siguiente")
    public ResponseEntity<LlamarTurnoResponse> llamarSiguiente() {
        return ResponseEntity.ok(turnoService.llamarSiguiente());
    }

    @Operation(summary = "Obtiene las métricas en tiempo real de la fila")
    @GetMapping("/metricas-fila")
    public ResponseEntity<MetricasFilaResponse> obtenerMetricas() {
        return ResponseEntity.ok(turnoService.obtenerMetricasFila());
    }

    @Operation(summary = "Cancela el turno activo del cliente")
    @PutMapping("/{numero}/cancelar")
    public ResponseEntity<CancelarTurnoResponse> cancelarTurno(@PathVariable int numero) {
        return ResponseEntity.ok(turnoService.cancelarTurno(numero));
    }

    @Operation(summary = "Lista todos los turnos registrados")
    @GetMapping
    public ResponseEntity<List<Turno>> listarTurnos() {
        return ResponseEntity.ok(turnoService.listarTurnos());
    }
}
