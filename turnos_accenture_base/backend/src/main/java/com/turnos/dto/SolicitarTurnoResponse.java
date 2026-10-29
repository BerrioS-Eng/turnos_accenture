package com.turnos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitarTurnoResponse {
    private Long id;
    private Integer numero;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaSolicitud;

    private String estado;
    private Integer turnosPorDelante;
    private Double tiempoEstimadoMinutos;
}
