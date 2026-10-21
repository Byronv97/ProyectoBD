package clases;

import java.sql.SQLException;

/** Convierte errores frecuentes de Oracle en mensajes comprensibles. */
public final class MensajesSQL {

    private MensajesSQL() { }

    public static String describir(String accion, SQLException ex) {
        return accion + ": " + switch (ex.getErrorCode()) {
            case 1 -> "ya existe un registro con ese valor (clave duplicada).";
            case 2290 -> "el valor no cumple una restriccion de la base de datos.";
            case 2291 -> "la relacion seleccionada no existe (clave foranea).";
            case 2292 -> "no se puede eliminar porque existen registros relacionados.";
            case 1400 -> "hay un campo obligatorio sin valor.";
            case 12899 -> "uno de los valores supera la longitud permitida.";
            default -> ex.getMessage();
        };
    }
}
