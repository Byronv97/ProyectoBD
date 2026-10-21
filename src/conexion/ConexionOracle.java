package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Crea conexiones JDBC contra la PDB local de Oracle. */
public final class ConexionOracle {

    private static final String URL = System.getProperty(
            "db.url", "jdbc:oracle:thin:@//localhost:1521/FREEPDB1");
    private static final String USUARIO = System.getProperty(
            "db.user", "Proyecto03");
    private static final String PASSWORD = System.getProperty(
            "db.password", System.getenv().getOrDefault("DB_PASSWORD", ""));

    private ConexionOracle() {
    }

    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Falta el driver ojdbc11.jar en el classpath del proyecto.", ex);
        }
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

    public static boolean estaDisponible() {
        try (Connection conexion = obtenerConexion()) {
            return conexion.isValid(3);
        } catch (SQLException ex) {
            return false;
        }
    }

    public static String getUrl() {
        return URL;
    }
}
