package com.turnos.service;

import com.turnos.dto.CancelarTurnoResponse;
import com.turnos.dto.EstadoTurnoResponse;
import com.turnos.dto.LlamarTurnoResponse;
import com.turnos.dto.MetricasFilaResponse;
import com.turnos.dto.SolicitarTurnoResponse;
import com.turnos.exception.FilaVaciaException;
import com.turnos.exception.TurnoNoEncontradoException;
import com.turnos.model.Turno;
import com.turnos.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para la gestión de turnos, fila virtual FIFO,
 * estimación de tiempos de espera y cálculo de métricas en ventanilla.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoService {

    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_ATENDIDO = "atendido";
    public static final String ESTADO_CANCELADO = "cancelado";
    public static final double PROMEDIO_HISTORICO_DEFECTO = 12.2;

    private final TurnoRepository turnoRepository;

    /**
     * Solicita un nuevo turno asignando el número secuencial (1-999) y calculando
     * posición en fila y tiempo estimado de espera.
     */
    @Transactional
    public synchronized SolicitarTurnoResponse solicitarTurno() {
        int nuevoNumero = calcularSiguienteNumero();
        int turnosPorDelante = (int) turnoRepository.countByEstado(ESTADO_PENDIENTE);
        double promedioEspera = calcularPromedioEsperaMinutos();
        double tiempoEstimado = Math.round(turnosPorDelante * promedioEspera * 10.0) / 10.0;

        LocalTime horaSolicitud = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);

        Turno turno = Turno.builder()
                .numero(nuevoNumero)
                .horaSolicitud(horaSolicitud)
                .estado(ESTADO_PENDIENTE)
                .build();

        turno = turnoRepository.save(turno);
        log.info("Turno solicitado exitosamente: ID {}, Número {}", turno.getId(), turno.getNumero());

        return SolicitarTurnoResponse.builder()
                .id(turno.getId())
                .numero(turno.getNumero())
                .horaSolicitud(turno.getHoraSolicitud())
                .estado(turno.getEstado())
                .turnosPorDelante(turnosPorDelante)
                .tiempoEstimadoMinutos(tiempoEstimado)
                .build();
    }

    /**
     * Consulta el estado de un turno por su número visible, indicando personas por delante y tiempo restante.
     */
    @Transactional(readOnly = true)
    public EstadoTurnoResponse consultarEstado(int numero) {
        // Se busca el turno pendiente activo más reciente; si no hay pendiente, se busca el último registrado
        Turno turno = turnoRepository.findTopByNumeroAndEstadoOrderByIdDesc(numero, ESTADO_PENDIENTE)
                .or(() -> turnoRepository.findTopByNumeroOrderByIdDesc(numero))
                .orElseThrow(() -> new TurnoNoEncontradoException("No se encontró ningún turno con número: " + numero));

        int turnoActual = obtenerTurnoActualEnVentanilla();
        int turnosPorDelante = 0;
        double tiempoEstimado = 0.0;

        if (ESTADO_PENDIENTE.equals(turno.getEstado())) {
            List<Turno> pendientes = turnoRepository.findByEstadoOrderByHoraSolicitudAsc(ESTADO_PENDIENTE);
            for (Turno p : pendientes) {
                if (p.getId().equals(turno.getId())) {
                    break;
                }
                turnosPorDelante++;
            }
            double promedio = calcularPromedioEsperaMinutos();
            tiempoEstimado = Math.round(turnosPorDelante * promedio * 10.0) / 10.0;
        }

        return EstadoTurnoResponse.builder()
                .numero(turno.getNumero())
                .estado(turno.getEstado())
                .turnoActual(turnoActual)
                .turnosPorDelante(turnosPorDelante)
                .tiempoEstimadoMinutos(tiempoEstimado)
                .build();
    }

    /**
     * Llama al siguiente turno pendiente según política FIFO estricta (horaSolicitud ASC).
     * El método es synchronized para prevenir condiciones de carrera entre múltiples asesores concurrentes.
     */
    @Transactional
    public synchronized LlamarTurnoResponse llamarSiguiente() {
        List<Turno> pendientes = turnoRepository.findByEstadoOrderByHoraSolicitudAsc(ESTADO_PENDIENTE);
        if (pendientes.isEmpty()) {
            log.warn("Intento de llamar turno con fila vacía.");
            throw new FilaVaciaException("No hay turnos pendientes en la fila");
        }

        Turno siguiente = pendientes.get(0);
        siguiente.setEstado(ESTADO_ATENDIDO);
        siguiente.setHoraAtencion(LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
        siguiente = turnoRepository.save(siguiente);

        log.info("Turno llamado: Número {}, Hora Atención {}", siguiente.getNumero(), siguiente.getHoraAtencion());

        return LlamarTurnoResponse.builder()
                .id(siguiente.getId())
                .numero(siguiente.getNumero())
                .horaAtencion(siguiente.getHoraAtencion())
                .estado(siguiente.getEstado())
                .build();
    }

    /**
     * Obtiene las métricas en tiempo real de la fila (KPIs de ventanilla).
     */
    @Transactional(readOnly = true)
    public MetricasFilaResponse obtenerMetricasFila() {
        int personasEsperando = (int) turnoRepository.countByEstado(ESTADO_PENDIENTE);
        int totalAtendidos = (int) turnoRepository.countByEstado(ESTADO_ATENDIDO);
        int totalCancelados = (int) turnoRepository.countByEstado(ESTADO_CANCELADO);
        int turnoActual = obtenerTurnoActualEnVentanilla();
        double promedioEspera = calcularPromedioEsperaMinutos();

        return MetricasFilaResponse.builder()
                .personasEsperando(personasEsperando)
                .totalAtendidos(totalAtendidos)
                .totalCancelados(totalCancelados)
                .turnoActual(turnoActual)
                .promedioEsperaMinutos(promedioEspera)
                .build();
    }

    /**
     * Cancela el turno activo en estado pendiente del usuario.
     */
    @Transactional
    public synchronized CancelarTurnoResponse cancelarTurno(int numero) {
        Turno turno = turnoRepository.findTopByNumeroAndEstadoOrderByIdDesc(numero, ESTADO_PENDIENTE)
                .orElseThrow(() -> new TurnoNoEncontradoException("No se encontró turno pendiente para el número: " + numero));

        turno.setEstado(ESTADO_CANCELADO);
        turnoRepository.save(turno);
        log.info("Turno {} cancelado exitosamente", numero);

        return CancelarTurnoResponse.builder()
                .numero(numero)
                .estado(ESTADO_CANCELADO)
                .mensaje("Turno cancelado exitosamente")
                .build();
    }

    /**
     * Retorna todos los turnos registrados.
     */
    @Transactional(readOnly = true)
    public List<Turno> listarTurnos() {
        return turnoRepository.findAll();
    }

    /**
     * Calcula la espera promedio en minutos a partir de los turnos atendidos.
     */
    public double calcularPromedioEsperaMinutos() {
        List<Turno> atendidos = turnoRepository.findByEstado(ESTADO_ATENDIDO);
        if (atendidos.isEmpty()) {
            return PROMEDIO_HISTORICO_DEFECTO;
        }

        long totalSegundos = 0;
        int validos = 0;
        for (Turno t : atendidos) {
            if (t.getHoraSolicitud() != null && t.getHoraAtencion() != null) {
                long seg = Duration.between(t.getHoraSolicitud(), t.getHoraAtencion()).getSeconds();
                if (seg >= 0) {
                    totalSegundos += seg;
                    validos++;
                }
            }
        }

        if (validos == 0) {
            return PROMEDIO_HISTORICO_DEFECTO;
        }

        double promedioMinutos = (totalSegundos / (double) validos) / 60.0;
        return Math.round(promedioMinutos * 10.0) / 10.0;
    }

    /**
     * Obtiene el número del turno que actualmente está siendo atendido o fue atendido más recientemente.
     */
    public int obtenerTurnoActualEnVentanilla() {
        List<Turno> atendidos = turnoRepository.findByEstado(ESTADO_ATENDIDO);
        if (atendidos.isEmpty()) {
            return 0;
        }

        atendidos.sort((t1, t2) -> {
            if (t1.getHoraAtencion() != null && t2.getHoraAtencion() != null) {
                int comp = t2.getHoraAtencion().compareTo(t1.getHoraAtencion());
                if (comp != 0) return comp;
            }
            return Long.compare(
                    t2.getId() != null ? t2.getId() : 0L,
                    t1.getId() != null ? t1.getId() : 0L
            );
        });

        return atendidos.get(0).getNumero();
    }

    private int calcularSiguienteNumero() {
        Optional<Turno> ultimo = turnoRepository.findTopByOrderByIdDesc();
        if (ultimo.isEmpty()) {
            return 1;
        }
        int ultimoNumero = ultimo.get().getNumero();
        return (ultimoNumero >= 999) ? 1 : ultimoNumero + 1;
    }
}
