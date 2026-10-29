package com.turnos.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Convierte LocalTime a String (HH:mm:ss) para SQLite.
 * Previene el error de sqlite-jdbc FastDateParser al invocar rs.getTime() en columnas de solo hora.
 */
@Converter(autoApply = true)
public class LocalTimeStringConverter implements AttributeConverter<LocalTime, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SHORT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalTime attribute) {
        return attribute != null ? attribute.format(FORMATTER) : null;
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        String trimmed = dbData.trim();
        if (trimmed.length() == 5) {
            return LocalTime.parse(trimmed, SHORT_FORMATTER);
        }
        return LocalTime.parse(trimmed, FORMATTER);
    }
}
