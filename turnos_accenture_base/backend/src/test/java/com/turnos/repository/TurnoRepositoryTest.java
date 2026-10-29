package com.turnos.repository;

import com.turnos.model.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TurnoRepositoryTest {

    @Autowired
    private TurnoRepository turnoRepository;

    @BeforeEach
    void setUp() {
        turnoRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe persistir y recuperar un turno con ID generado automáticamente")
    void debeGuardarYRecuperarTurno() {
        Turno turno = Turno.builder()
                .numero(1)
                .horaSolicitud(LocalTime.of(8, 2, 0))
                .horaAtencion(LocalTime.of(8, 5, 0))
                .estado("atendido")
                .build();

        Turno guardado = turnoRepository.save(turno);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNumero()).isEqualTo(1);

        Optional<Turno> encontrado = turnoRepository.findById(guardado.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getHoraSolicitud()).isEqualTo(LocalTime.of(8, 2, 0));
    }

    @Test
    @DisplayName("Debe soportar números repetidos con distintos IDs (caso turno #7)")
    void debeSoportarNumerosRepetidos() {
        Turno turnoCancelado = Turno.builder()
                .numero(7)
                .horaSolicitud(LocalTime.of(8, 30, 0))
                .horaAtencion(null)
                .estado("cancelado")
                .build();

        Turno turnoPendiente = Turno.builder()
                .numero(7)
                .horaSolicitud(LocalTime.of(8, 34, 0))
                .horaAtencion(null)
                .estado("pendiente")
                .build();

        Turno g1 = turnoRepository.save(turnoCancelado);
        Turno g2 = turnoRepository.save(turnoPendiente);

        assertThat(g1.getId()).isNotEqualTo(g2.getId());
        assertThat(turnoRepository.count()).isEqualTo(2);

        List<Turno> turnosSiete = turnoRepository.findByNumero(7);
        assertThat(turnosSiete).hasSize(2);
    }

    @Test
    @DisplayName("findByEstadoOrderByHoraSolicitudAsc debe devolver los turnos estrictamente en orden FIFO")
    void debeOrdenarPorHoraSolicitudAscendente() {
        // Se insertan en orden cronológicamente desordenado
        turnoRepository.save(Turno.builder()
                .numero(9)
                .horaSolicitud(LocalTime.of(8, 41, 0))
                .estado("pendiente")
                .build());

        turnoRepository.save(Turno.builder()
                .numero(6)
                .horaSolicitud(LocalTime.of(8, 25, 0))
                .estado("pendiente")
                .build());

        turnoRepository.save(Turno.builder()
                .numero(8)
                .horaSolicitud(LocalTime.of(8, 38, 0))
                .estado("pendiente")
                .build());

        turnoRepository.save(Turno.builder()
                .numero(7)
                .horaSolicitud(LocalTime.of(8, 34, 0))
                .estado("pendiente")
                .build());

        List<Turno> pendientes = turnoRepository.findByEstadoOrderByHoraSolicitudAsc("pendiente");

        assertThat(pendientes).hasSize(4);
        assertThat(pendientes.stream().map(Turno::getNumero).toList())
                .containsExactly(6, 7, 8, 9);
    }

    @Test
    @DisplayName("findTopByOrderByNumeroDesc debe devolver el turno con mayor número emitido")
    void debeObtenerTurnoConMayorNumero() {
        turnoRepository.save(Turno.builder().numero(5).horaSolicitud(LocalTime.of(8, 20)).estado("atendido").build());
        turnoRepository.save(Turno.builder().numero(10).horaSolicitud(LocalTime.of(8, 45)).estado("pendiente").build());
        turnoRepository.save(Turno.builder().numero(7).horaSolicitud(LocalTime.of(8, 30)).estado("cancelado").build());

        Optional<Turno> top = turnoRepository.findTopByOrderByNumeroDesc();

        assertThat(top).isPresent();
        assertThat(top.get().getNumero()).isEqualTo(10);
    }

    @Test
    @DisplayName("findByEstado debe filtrar turnos según el estado indicado")
    void debeFiltrarPorEstado() {
        turnoRepository.save(Turno.builder().numero(1).horaSolicitud(LocalTime.of(8, 0)).estado("atendido").build());
        turnoRepository.save(Turno.builder().numero(2).horaSolicitud(LocalTime.of(8, 1)).estado("atendido").build());
        turnoRepository.save(Turno.builder().numero(3).horaSolicitud(LocalTime.of(8, 2)).estado("pendiente").build());
        turnoRepository.save(Turno.builder().numero(4).horaSolicitud(LocalTime.of(8, 3)).estado("cancelado").build());

        assertThat(turnoRepository.findByEstado("atendido")).hasSize(2);
        assertThat(turnoRepository.findByEstado("pendiente")).hasSize(1);
        assertThat(turnoRepository.findByEstado("cancelado")).hasSize(1);
        assertThat(turnoRepository.countByEstado("atendido")).isEqualTo(2);
    }
}
