package com.turnos.qa.support;

import java.io.InputStream;
import java.util.Properties;

/** Lee qa/qa.properties; cualquier valor se puede sobrescribir con -Dclave=valor. */
public final class QaConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = QaConfig.class.getResourceAsStream("/qa/qa.properties")) {
            PROPS.load(in);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer qa/qa.properties", e);
        }
    }

    private QaConfig() {}

    private static String get(String clave) {
        return System.getProperty(clave, PROPS.getProperty(clave));
    }

    public static String frontUrl()  { return get("front.url"); }
    public static boolean headless() { return Boolean.parseBoolean(get("headless")); }
}
