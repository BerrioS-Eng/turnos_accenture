package com.turnos.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Entidad JPA para la representación de un Turno en la fila virtual.
 * Se utiliza un ID interno autoincremental como clave primaria para permitir
 * números de turno repetidos a lo largo de la jornada (ej. turno #7 cancelado y re-solicitado).
 */
@Entity
@Table(name = "turno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @JsonFormat(pattern = "HH:mm:ss")
    @Convert(converter = LocalTimeStringConverter.class)
    @Column(name = "hora_solicitud", nullable = false)
    private LocalTime horaSolicitud;

    @JsonFormat(pattern = "HH:mm:ss")
    @Convert(converter = LocalTimeStringConverter.class)
    @Column(name = "hora_atencion")
    private LocalTime horaAtencion;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Turno turno = (Turno) o;
        return Objects.equals(id, turno.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
