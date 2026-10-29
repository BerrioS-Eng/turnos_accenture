package com.turnos.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TurnoTest {

    @Test
    @DisplayName("Debe construir un turno correctamente mediante el Builder")
    void debeConstruirTurnoConBuilder() {
        LocalTime solicitud = LocalTime.of(8, 25, 0);
        LocalTime atencion = LocalTime.of(8, 30, 0);

        Turno turno = Turno.builder()
                .id(1L)
                .numero(6)
                .horaSolicitud(solicitud)
                .horaAtencion(atencion)
                .estado("atendido")
                .build();

        assertThat(turno.getId()).isEqualTo(1L);
        assertThat(turno.getNumero()).isEqualTo(6);
        assertThat(turno.getHoraSolicitud()).isEqualTo(solicitud);
        assertThat(turno.getHoraAtencion()).isEqualTo(atencion);
        assertThat(turno.getEstado()).isEqualTo("atendido");
    }

    @Test
    @DisplayName("Dos turnos con mismo id deben ser iguales según equals()")
    void turnosConMismoIdSonIguales() {
        Turno turno1 = Turno.builder().id(7L).numero(7).estado("cancelado").build();
        Turno turno2 = Turno.builder().id(7L).numero(7).estado("pendiente").build();

        assertThat(turno1).isEqualTo(turno2);
        assertThat(turno1.hashCode()).isEqualTo(turno2.hashCode());
    }

    @Test
    @DisplayName("Dos turnos con diferente id deben ser diferentes aunque tengan el mismo número visible")
    void turnosConDiferenteIdSonDiferentes() {
        Turno turnoCancelado = Turno.builder().id(8L).numero(7).estado("cancelado").build();
        Turno turnoPendiente = Turno.builder().id(9L).numero(7).estado("pendiente").build();

        assertThat(turnoCancelado).isNotEqualTo(turnoPendiente);
    }
}
