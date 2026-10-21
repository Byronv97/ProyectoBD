package clases;

import conexion.ConexionOracle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso JDBC de CLIENTE. Cada operación administra su propia conexión. */
public class ClienteDAO {

    private static final String LISTAR = "SELECT ID_CLIENTE, NOMBRES, APELLIDOS, "
            + "TELEFONO, EMAIL, DIRECCION, FECHA_REGISTRO "
            + "FROM CLIENTE ORDER BY ID_CLIENTE";
    private static final String INSERTAR = "INSERT INTO CLIENTE "
            + "(NOMBRES, APELLIDOS, TELEFONO, EMAIL, DIRECCION) "
            + "VALUES (?, ?, ?, ?, ?)";
    private static final String ACTUALIZAR = "UPDATE CLIENTE SET NOMBRES=?, APELLIDOS=?, "
            + "TELEFONO=?, EMAIL=?, DIRECCION=? WHERE ID_CLIENTE=?";
    private static final String ELIMINAR = "DELETE FROM CLIENTE WHERE ID_CLIENTE=?";

    public List<Cliente> listar() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(LISTAR);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(resultado.getLong("ID_CLIENTE"));
                cliente.setNombres(resultado.getString("NOMBRES"));
                cliente.setApellidos(resultado.getString("APELLIDOS"));
                cliente.setTelefono(resultado.getString("TELEFONO"));
                cliente.setEmail(resultado.getString("EMAIL"));
                cliente.setDireccion(resultado.getString("DIRECCION"));
                cliente.setFechaRegistro(resultado.getDate("FECHA_REGISTRO"));
                clientes.add(cliente);
            }
        }
        return clientes;
    }

    public void insertar(Cliente cliente) throws SQLException {
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
            asignarCampos(sentencia, cliente);
            sentencia.executeUpdate();
        }
    }

    public void actualizar(Cliente cliente) throws SQLException {
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
            asignarCampos(sentencia, cliente);
            sentencia.setLong(7, cliente.getIdCliente());
            sentencia.executeUpdate();
        }
    }

    public void eliminarFisico(long idCliente) throws SQLException {
        try (Connection conexion = ConexionOracle.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement sentencia = conexion.prepareStatement(ELIMINAR)) {
                sentencia.setLong(1, idCliente);
                sentencia.executeUpdate();
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    private void asignarCampos(PreparedStatement sentencia, Cliente cliente) throws SQLException {
        sentencia.setString(1, cliente.getNombres());
        sentencia.setString(2, cliente.getApellidos());
        sentencia.setString(3, vacioANulo(cliente.getTelefono()));
        sentencia.setString(4, vacioANulo(cliente.getEmail()));
        sentencia.setString(5, vacioANulo(cliente.getDireccion()));
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
