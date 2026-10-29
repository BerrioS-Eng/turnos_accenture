package com.turnos.repository;

import com.turnos.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Turno sobre base de datos SQLite.
 */
@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    /**
     * Busca turnos filtrados por su estado ("pendiente", "atendido", "cancelado").
     */
    List<Turno> findByEstado(String estado);

    /**
     * Recupera turnos en el estado especificado ordenados cronológicamente por hora de solicitud (FIFO).
     */
    List<Turno> findByEstadoOrderByHoraSolicitudAsc(String estado);

    /**
     * Obtiene el turno con el número más alto emitido.
     */
    Optional<Turno> findTopByOrderByNumeroDesc();

    /**
     * Obtiene el último turno registrado en la base de datos por ID descendente.
     */
    Optional<Turno> findTopByOrderByIdDesc();

    /**
     * Conteo rápido de turnos por estado.
     */
    long countByEstado(String estado);

    /**
     * Busca todos los turnos que tengan un determinado número visible.
     */
    List<Turno> findByNumero(Integer numero);

    /**
     * Obtiene el turno más reciente con un número específico.
     */
    Optional<Turno> findTopByNumeroOrderByIdDesc(Integer numero);

    /**
     * Obtiene el turno más reciente con un número y estado específico.
     */
    Optional<Turno> findTopByNumeroAndEstadoOrderByIdDesc(Integer numero, String estado);

    /**
     * Obtiene el último turno atendido por fecha/hora de atención.
     */
    Optional<Turno> findTopByEstadoOrderByHoraAtencionDesc(String estado);

    /**
     * Obtiene el último turno con el estado especificado por ID descendente.
     */
    Optional<Turno> findTopByEstadoOrderByIdDesc(String estado);
}
