package com.turnos.service;

import com.turnos.model.Turno;
import com.turnos.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de la ingesta de datos iniciales desde el archivo turnos_hoy.csv hacia SQLite.
 * Ejecuta la carga al arrancar la aplicación si la base de datos se encuentra vacía.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataLoaderService implements CommandLineRunner {

    private final TurnoRepository turnoRepository;
    private final ResourceLoader resourceLoader;

    @Value("${turnos.archivo-inicial:classpath:data/turnos_hoy.csv}")
    private String rutaArchivoInicial;

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalEnd()
            .toFormatter();

    @Override
    public void run(String... args) {
        if (turnoRepository.count() == 0) {
            log.info("Base de datos SQLite vacía. Iniciando carga inicial desde: {}", rutaArchivoInicial);
            List<Turno> cargados = cargarTurnosDesdeRuta(rutaArchivoInicial);
            log.info("Carga inicial exitosa: {} turnos cargados en la base de datos.", cargados.size());
        } else {
            log.info("Base de datos ya cuenta con {} registros. Se omite carga inicial desde CSV.", turnoRepository.count());
        }
    }

    /**
     * Carga turnos desde la ruta de recurso especificada (ej. classpath:data/turnos_hoy.csv).
     */
    @Transactional
    public List<Turno> cargarTurnosDesdeRuta(String ruta) {
        try {
            Resource resource = resourceLoader.getResource(ruta);
            if (!resource.exists()) {
                log.warn("El archivo de turnos no existe en la ruta: {}", ruta);
                return List.of();
            }
            try (Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return cargarTurnosDesdeReader(reader);
            }
        } catch (Exception e) {
            log.error("Error al leer el archivo de turnos desde {}: {}", ruta, e.getMessage(), e);
            throw new IllegalStateException("Error al cargar turnos desde CSV: " + ruta, e);
        }
    }

    /**
     * Parsea e inserta los turnos leídos a través de un Reader utilizando Apache Commons CSV.
     * Garantiza resiliencia ante valores nulos/vacíos en hora_atencion y duplicidad de números visibles.
     */
    @Transactional
    public List<Turno> cargarTurnosDesdeReader(Reader reader) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build();

        List<Turno> turnosAGuardar = new ArrayList<>();

        try (CSVParser parser = format.parse(reader)) {
            for (CSVRecord record : parser) {
                String numeroStr = obtenerValorColumna(record, "numero");
                if (numeroStr == null || numeroStr.isBlank()) {
                    continue;
                }

                Integer numero = Integer.parseInt(numeroStr.trim());
                LocalTime horaSolicitud = parseHora(obtenerValorColumna(record, "hora_solicitud", "horaSolicitud"));
                LocalTime horaAtencion = parseHora(obtenerValorColumna(record, "hora_atencion", "horaAtencion"));
                String estado = obtenerValorColumna(record, "estado");
                if (estado != null) {
                    estado = estado.trim().toLowerCase();
                }

                Turno turno = Turno.builder()
                        .numero(numero)
                        .horaSolicitud(horaSolicitud)
                        .horaAtencion(horaAtencion)
                        .estado(estado)
                        .build();

                turnosAGuardar.add(turno);
            }

            return turnoRepository.saveAll(turnosAGuardar);
        } catch (Exception e) {
            log.error("Error al procesar registros del CSV: {}", e.getMessage(), e);
            throw new IllegalStateException("Error al parsear el contenido CSV", e);
        }
    }

    /**
     * Parsea una cadena de texto a LocalTime tolerando formatos HH:mm y HH:mm:ss.
     * Retorna null de forma segura ante entradas nulas o vacías.
     */
    public static LocalTime parseHora(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return LocalTime.parse(valor.trim(), TIME_FORMATTER);
    }

    private String obtenerValorColumna(CSVRecord record, String... posiblesNombres) {
        for (String nombre : posiblesNombres) {
            if (record.isMapped(nombre)) {
                String valor = record.get(nombre);
                if (valor != null && !valor.trim().isEmpty()) {
                    return valor.trim();
                }
                return null;
            }
        }
        return null;
    }
}
