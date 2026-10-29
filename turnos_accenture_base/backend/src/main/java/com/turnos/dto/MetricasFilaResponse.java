package com.turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasFilaResponse {
    private Integer personasEsperando;
    private Integer totalAtendidos;
    private Integer totalCancelados;
    private Integer turnoActual;
    private Double promedioEsperaMinutos;
}
