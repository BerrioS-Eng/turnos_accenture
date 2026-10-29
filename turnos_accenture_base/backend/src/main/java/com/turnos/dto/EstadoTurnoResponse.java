package com.turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoTurnoResponse {
    private Integer numero;
    private String estado;
    private Integer turnoActual;
    private Integer turnosPorDelante;
    private Double tiempoEstimadoMinutos;
}
