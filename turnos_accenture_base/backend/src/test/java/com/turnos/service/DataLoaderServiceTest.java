package com.turnos.service;

import com.turnos.model.Turno;
import com.turnos.repository.TurnoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.StringReader;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DataLoaderServiceTest {

    @Autowired
    private DataLoaderService dataLoaderService;

    @Autowired
    private TurnoRepository turnoRepository;

    @BeforeEach
    void setUp() {
        turnoRepository.deleteAll();
    }

    @Test
    @DisplayName("[CP-POS] Happy Path - Carga de turnos_hoy.csv registra 11 turnos, 5 atendidos, 5 pendientes, 1 cancelado")
    void debeCargarTurnosDesdeCsvCorrectamente() {
        dataLoaderService.cargarTurnosDesdeRuta("classpath:data/turnos_hoy.csv");

        assertThat(turnoRepository.count()).isEqualTo(11);
        assertThat(turnoRepository.countByEstado("atendido")).isEqualTo(5);
        assertThat(turnoRepository.countByEstado("pendiente")).isEqualTo(5);
        assertThat(turnoRepository.countByEstado("cancelado")).isEqualTo(1);

        // Validar que el turno 7 cancelado y el turno 7 pendiente coexisten con IDs diferentes
        List<Turno> turnosSiete = turnoRepository.findByNumero(7);
        assertThat(turnosSiete).hasSize(2);
        Turno cancelado = turnosSiete.stream().filter(t -> "cancelado".equals(t.getEstado())).findFirst().orElseThrow();
        Turno pendiente = turnosSiete.stream().filter(t -> "pendiente".equals(t.getEstado())).findFirst().orElseThrow();

        assertThat(cancelado.getId()).isNotEqualTo(pendiente.getId());
        assertThat(cancelado.getHoraSolicitud()).isEqualTo(LocalTime.of(8, 30));
        assertThat(pendiente.getHoraSolicitud()).isEqualTo(LocalTime.of(8, 34));
    }

    @Test
    @DisplayName("[CP-NEG] Resiliencia ante campos vacíos - No lanza NullPointerException ni error de parseo si hora_atencion está vacía")
    void debeSerResilienteAnteCamposVacios() {
        String csvConCamposVacios = """
                numero,hora_solicitud,hora_atencion,estado
                6,08:25,,pendiente
                9,08:41,   ,pendiente
                """;

        assertThatCode(() -> {
            List<Turno> cargados = dataLoaderService.cargarTurnosDesdeReader(new StringReader(csvConCamposVacios));
            assertThat(cargados).hasSize(2);
            assertThat(cargados.get(0).getHoraAtencion()).isNull();
            assertThat(cargados.get(1).getHoraAtencion()).isNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("parseHora debe tolerar HH:mm, HH:mm:ss, espacios en blanco y valores nulos sin fallar")
    void debeParsearHorasCorrectamente() {
        assertThat(DataLoaderService.parseHora(null)).isNull();
        assertThat(DataLoaderService.parseHora("")).isNull();
        assertThat(DataLoaderService.parseHora("   ")).isNull();
        assertThat(DataLoaderService.parseHora("08:02")).isEqualTo(LocalTime.of(8, 2, 0));
        assertThat(DataLoaderService.parseHora("08:02:45")).isEqualTo(LocalTime.of(8, 2, 45));
        assertThat(DataLoaderService.parseHora("8:15:30")).isEqualTo(LocalTime.of(8, 15, 30));
    }

    @Test
    @DisplayName("Idempotencia: run() no debe duplicar datos si la base de datos ya tiene registros")
    void noDebeDuplicarSiYaExistenRegistros() {
        turnoRepository.save(Turno.builder()
                .numero(1)
                .horaSolicitud(LocalTime.of(8, 0))
                .estado("pendiente")
                .build());

        dataLoaderService.run();

        // Debe conservar solo el registro preexistente
        assertThat(turnoRepository.count()).isEqualTo(1);
    }
}
