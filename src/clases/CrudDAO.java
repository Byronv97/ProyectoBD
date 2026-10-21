package clases;

import conexion.ConexionOracle;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** DAO reutilizable para las configuraciones CRUD definidas por la aplicacion. */
public class CrudDAO {

    public List<Opcion> listarOpciones(String tabla, String columnaId, String columnaTexto)
            throws SQLException {
        String sql = "SELECT " + columnaId + ", " + columnaTexto + " FROM " + tabla
                + " ORDER BY " + columnaTexto;
        List<Opcion> opciones = new ArrayList<>();
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                opciones.add(new Opcion(resultado.getLong(1), resultado.getString(2)));
            }
        }
        return opciones;
    }

    public record Opcion(long id, String texto) {
        @Override public String toString() { return id + " - " + texto; }
    }

    public List<ProductoOpcion> listarProductos() throws SQLException {
        String sql = "SELECT ID_PRODUCTO, NOMBRE, PRECIO_VENTA, STOCK FROM PRODUCTO "
                + "ORDER BY NOMBRE";
        List<ProductoOpcion> productos = new ArrayList<>();
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                productos.add(new ProductoOpcion(resultado.getLong("ID_PRODUCTO"),
                        resultado.getString("NOMBRE"), resultado.getBigDecimal("PRECIO_VENTA"),
                        resultado.getBigDecimal("STOCK")));
            }
        }
        return productos;
    }

    public record ProductoOpcion(long id, String nombre, BigDecimal precio, BigDecimal stock) {
        @Override public String toString() {
            return id + " - " + nombre + " | precio: " + precio + " | stock: " + stock;
        }
    }

    public List<Object[]> listar(CrudConfig config) throws SQLException {
        return listar(config, null, null);
    }

    public List<Object[]> listar(CrudConfig config, String filtroColumna, Long filtroValor) throws SQLException {
        String sql = "SELECT " + String.join(", ", config.columnas()) + " FROM "
                + config.tabla();
        if (filtroColumna != null) sql += " WHERE " + filtroColumna + "=?";
        sql += " ORDER BY " + config.id();
        List<Object[]> filas = new ArrayList<>();
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = ejecutarConsulta(sentencia, filtroValor)) {
            while (resultado.next()) {
                Object[] fila = new Object[config.columnas().length];
                for (int i = 0; i < fila.length; i++) {
                    fila[i] = resultado.getObject(i + 1);
                }
                filas.add(fila);
            }
        }
        return filas;
    }

    private ResultSet ejecutarConsulta(PreparedStatement sentencia, Long filtroValor) throws SQLException {
        if (filtroValor != null) sentencia.setLong(1, filtroValor);
        return sentencia.executeQuery();
    }

    public void insertar(CrudConfig config, Object[] valores) throws SQLException {
        String columnas = String.join(", ", nombresCampos(config));
        String parametros = "?, ".repeat(config.campos().length);
        parametros = parametros.substring(0, parametros.length() - 2);
        String sql = "INSERT INTO " + config.tabla() + " (" + columnas + ") VALUES (" + parametros + ")";
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignar(sentencia, config, valores);
            sentencia.executeUpdate();
        }
    }

    public void actualizar(CrudConfig config, long id, Object[] valores) throws SQLException {
        StringBuilder cambios = new StringBuilder();
        for (CrudConfig.Campo campo : config.campos()) {
            if (cambios.length() > 0) cambios.append(", ");
            cambios.append(campo.columna()).append("=?");
        }
        String sql = "UPDATE " + config.tabla() + " SET " + cambios
                + " WHERE " + config.id() + "=?";
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignar(sentencia, config, valores);
            sentencia.setLong(config.campos().length + 1, id);
            sentencia.executeUpdate();
        }
    }

    public void eliminarFisico(CrudConfig config, long id) throws SQLException {
        String sql = "DELETE FROM " + config.tabla() + " WHERE " + config.id() + "=?";
        try (Connection conexion = ConexionOracle.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setLong(1, id);
                sentencia.executeUpdate();
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    private String[] nombresCampos(CrudConfig config) {
        String[] nombres = new String[config.campos().length];
        for (int i = 0; i < nombres.length; i++) nombres[i] = config.campos()[i].columna();
        return nombres;
    }

    private void asignar(PreparedStatement sentencia, CrudConfig config, Object[] valores) throws SQLException {
        for (int i = 0; i < config.campos().length; i++) {
            Object valor = valores[i];
            if (valor == null) {
                int tipo = switch (config.campos()[i].tipo()) {
                    case NUMERO -> Types.NUMERIC;
                    case DECIMAL -> Types.DECIMAL;
                    case FECHA -> Types.DATE;
                    default -> Types.VARCHAR;
                };
                sentencia.setNull(i + 1, tipo);
                continue;
            }
            switch (config.campos()[i].tipo()) {
                case NUMERO -> sentencia.setLong(i + 1, ((Number) valor).longValue());
                case DECIMAL -> sentencia.setBigDecimal(i + 1, (BigDecimal) valor);
                case FECHA -> sentencia.setDate(i + 1, (Date) valor);
                default -> sentencia.setString(i + 1, (String) valor);
            }
        }
    }
}
