package vistas;

import clases.CrudDAO;
import clases.MensajesSQL;
import conexion.ConexionOracle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Registro de venta completo en una sola pantalla. */
public class VentaFormularioPanel extends JPanel {

    private final CrudDAO dao = new CrudDAO();
    private final JComboBox<CrudDAO.Opcion> clientes = new JComboBox<>();
    private final JComboBox<CrudDAO.Opcion> empleados = new JComboBox<>();
    private final JComboBox<CrudDAO.ProductoOpcion> productos = new JComboBox<>();
    private final JTextField fecha = new JTextField(LocalDate.now().toString(), 12);
    private final JTextField cantidad = new JTextField("1", 12);
    private final JTextField precio = new JTextField(12);
    private final JTextField descuento = new JTextField("0", 12);
    private final JTextField busqueda = new JTextField(18);
    private long idVentaSeleccionada;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Cliente", "Empleado", "Producto", "Cantidad", "Fecha", "Total"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    public VentaFormularioPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
        setBackground(new Color(247, 249, 252));
        JLabel titulo = new JLabel("Ventas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
        FiltroTabla.configurar(tabla, busqueda);
        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.add(titulo, BorderLayout.NORTH);
        superior.add(crearFormulario(), BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        refrescarListas();
        recargar();
        productos.addActionListener(e -> actualizarPrecio());
    }

    private JPanel crearFormulario() {
        JPanel envoltura = new JPanel(new BorderLayout(8, 8));
        envoltura.setBackground(Color.WHITE);
        envoltura.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        agregar(campos, "Cliente *", clientes, 0, 0);
        agregar(campos, "Producto *", productoPanel(), 2, 0);
        agregar(campos, "Empleado *", empleados, 0, 1);
        agregar(campos, "Fecha (yyyy-mm-dd) *", fecha, 2, 1);
        agregar(campos, "Cantidad *", cantidad, 0, 2);
        agregar(campos, "Precio *", precio, 2, 2);
        agregar(campos, "Descuento", descuento, 0, 3);
        envoltura.add(campos, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        acciones.add(new JLabel("Buscar:"));
        acciones.add(busqueda);
        JButton limpiar = new JButton("Limpiar");
        limpiar.addActionListener(e -> limpiar());
        JButton guardar = new JButton("Guardar venta");
        guardar.addActionListener(e -> guardar());
        JButton modificar = new JButton("Actualizar venta");
        modificar.addActionListener(e -> actualizarSeleccionada());
        JButton eliminar = new JButton("Eliminar venta");
        eliminar.addActionListener(e -> eliminar());
        JButton actualizar = new JButton("Recargar lista");
        actualizar.addActionListener(e -> recargar());
        acciones.add(limpiar); acciones.add(guardar); acciones.add(modificar);
        acciones.add(eliminar); acciones.add(actualizar);
        envoltura.add(acciones, BorderLayout.SOUTH);
        return envoltura;
    }

    private void agregar(JPanel panel, String etiqueta, java.awt.Component componente,
            int columna, int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = columna; c.gridy = fila;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = columna + 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        panel.add(componente, c);
    }

    public void refrescarListas() {
        try {
            clientes.removeAllItems();
            empleados.removeAllItems();
            productos.removeAllItems();
            cargarOpciones(clientes, dao.listarOpciones("CLIENTE", "ID_CLIENTE", "NOMBRES"));
            cargarOpciones(empleados, dao.listarOpciones("EMPLEADO", "ID_EMPLEADO", "NOMBRES"));
            for (CrudDAO.ProductoOpcion producto : dao.listarProductos()) productos.addItem(producto);
        } catch (SQLException ex) {
            error("No se pudieron cargar las listas de venta:\n" + ex.getMessage());
        }
    }

    private void cargarOpciones(JComboBox<CrudDAO.Opcion> combo, List<CrudDAO.Opcion> opciones) {
        for (CrudDAO.Opcion opcion : opciones) combo.addItem(opcion);
    }

    private JPanel productoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JButton nuevo = new JButton("+ Producto");
        nuevo.setToolTipText("Crear un producto sin salir de la venta");
        nuevo.addActionListener(e -> new ProductoDialog(SwingUtilities.getWindowAncestor(this),
                this::refrescarListas).setVisible(true));
        panel.add(productos, BorderLayout.CENTER);
        panel.add(nuevo, BorderLayout.EAST);
        return panel;
    }

    private void actualizarPrecio() {
        CrudDAO.ProductoOpcion producto = (CrudDAO.ProductoOpcion) productos.getSelectedItem();
        if (producto != null) precio.setText(producto.precio().toPlainString());
    }

    private void cargarSeleccion() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) return;
        int fila = tabla.convertRowIndexToModel(filaVista);
        long idVenta = ((Number) modelo.getValueAt(fila, 0)).longValue();
        idVentaSeleccionada = idVenta;
        String sql = "SELECT v.ID_CLIENTE, v.ID_EMPLEADO, d.ID_PRODUCTO, d.CANTIDAD, "
                + "d.PRECIO_UNITARIO, d.DESCUENTO, v.FECHA_VENTA "
                + "FROM VENTA v JOIN DETALLE_VENTA d ON d.ID_VENTA=v.ID_VENTA "
                + "WHERE v.ID_VENTA=?";
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idVenta);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    seleccionar(clientes, resultado.getLong("ID_CLIENTE"));
                    seleccionar(empleados, resultado.getLong("ID_EMPLEADO"));
                    seleccionarProducto(resultado.getLong("ID_PRODUCTO"));
                    cantidad.setText(resultado.getBigDecimal("CANTIDAD").toPlainString());
                    precio.setText(resultado.getBigDecimal("PRECIO_UNITARIO").toPlainString());
                    descuento.setText(resultado.getBigDecimal("DESCUENTO").toPlainString());
                    fecha.setText(resultado.getDate("FECHA_VENTA").toString());
                }
            }
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudo cargar la venta seleccionada", ex));
        }
    }

    private void seleccionar(JComboBox<CrudDAO.Opcion> combo, long id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarProducto(long id) {
        for (int i = 0; i < productos.getItemCount(); i++) {
            if (productos.getItemAt(i).id() == id) {
                productos.setSelectedIndex(i);
                return;
            }
        }
    }

    private void guardar() {
        CrudDAO.Opcion cliente = (CrudDAO.Opcion) clientes.getSelectedItem();
        CrudDAO.Opcion empleado = (CrudDAO.Opcion) empleados.getSelectedItem();
        CrudDAO.ProductoOpcion producto = (CrudDAO.ProductoOpcion) productos.getSelectedItem();
        if (cliente == null || empleado == null || producto == null) {
            error("Seleccione cliente, empleado y producto."); return;
        }
        try {
            BigDecimal qty = decimal(cantidad, "Cantidad");
            BigDecimal unitario = decimal(precio, "Precio");
            BigDecimal rebaja = decimal(descuento, "Descuento");
            if (qty.signum() <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            if (unitario.signum() < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");
            if (rebaja.signum() < 0) throw new IllegalArgumentException("El descuento no puede ser negativo.");
            if (rebaja.compareTo(qty.multiply(unitario)) > 0) {
                throw new IllegalArgumentException("El descuento no puede superar el subtotal.");
            }
            Date fechaVenta = Date.valueOf(fecha.getText().trim());
            BigDecimal total = qty.multiply(unitario).subtract(rebaja);
            long idVenta = idVentaSeleccionada;
            int filaVista = tabla.getSelectedRow();
            if (filaVista >= 0) {
                int fila = tabla.convertRowIndexToModel(filaVista);
                idVenta = ((Number) modelo.getValueAt(fila, 0)).longValue();
            }
            if (idVenta == 0) {
                registrar(cliente.id(), empleado.id(), producto.id(), fechaVenta,
                        qty, unitario, rebaja, total);
            } else {
                actualizarVenta(idVenta, cliente.id(), empleado.id(), producto.id(),
                        fechaVenta, qty, unitario, rebaja, total);
            }
            limpiar();
            recargar();
        } catch (IllegalArgumentException | SQLException ex) {
            error(ex instanceof SQLException sql
                    ? MensajesSQL.describir("No se pudo registrar la venta", sql)
                    : "No se pudo registrar la venta:\n" + ex.getMessage());
        }
    }

    private void actualizarSeleccionada() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            error("Seleccione una venta para actualizar.");
            return;
        }
        int fila = tabla.convertRowIndexToModel(filaVista);
        idVentaSeleccionada = ((Number) modelo.getValueAt(fila, 0)).longValue();
        guardar();
    }

    private void registrar(long idCliente, long idEmpleado, long idProducto, Date fechaVenta,
            BigDecimal qty, BigDecimal unitario, BigDecimal rebaja, BigDecimal total) throws SQLException {
        try (Connection conexion = ConexionOracle.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                BigDecimal stock;
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT STOCK FROM PRODUCTO WHERE ID_PRODUCTO=? FOR UPDATE")) {
                    consulta.setLong(1, idProducto);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        if (!resultado.next()) throw new SQLException("El producto no existe.");
                        stock = resultado.getBigDecimal(1);
                    }
                }
                if (stock.compareTo(qty) < 0) {
                    throw new SQLException("Stock insuficiente. Disponible: " + stock);
                }
                long idVenta;
                String insertarVenta = "INSERT INTO VENTA "
                        + "(ID_CLIENTE, ID_EMPLEADO, FECHA_VENTA, TOTAL) "
                        + "VALUES (?, ?, ?, ?)";
                try (PreparedStatement venta = conexion.prepareStatement(insertarVenta,
                        new String[]{"ID_VENTA"})) {
                    venta.setLong(1, idCliente); venta.setLong(2, idEmpleado);
                    venta.setDate(3, fechaVenta); venta.setBigDecimal(4, total);
                    venta.executeUpdate();
                    try (ResultSet claves = venta.getGeneratedKeys()) {
                        if (!claves.next()) throw new SQLException("No se obtuvo el ID de la venta.");
                        idVenta = claves.getLong(1);
                    }
                }
                try (PreparedStatement detalle = conexion.prepareStatement(
                        "INSERT INTO DETALLE_VENTA "
                        + "(ID_VENTA, ID_PRODUCTO, CANTIDAD, PRECIO_UNITARIO, DESCUENTO) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
                    detalle.setLong(1, idVenta); detalle.setLong(2, idProducto);
                    detalle.setBigDecimal(3, qty); detalle.setBigDecimal(4, unitario);
                    detalle.setBigDecimal(5, rebaja); detalle.executeUpdate();
                }
                try (PreparedStatement stockUpdate = conexion.prepareStatement(
                        "UPDATE PRODUCTO SET STOCK=STOCK-? WHERE ID_PRODUCTO=?")) {
                    stockUpdate.setBigDecimal(1, qty); stockUpdate.setLong(2, idProducto);
                    stockUpdate.executeUpdate();
                }
                try (PreparedStatement movimiento = conexion.prepareStatement(
                        "INSERT INTO MOVIMIENTO_INVENTARIO "
                        + "(ID_PRODUCTO, ID_EMPLEADO, TIPO_MOVIMIENTO, CANTIDAD, ID_VENTA, OBSERVACION) "
                        + "VALUES (?, ?, 'SALIDA', ?, ?, ?)")) {
                    movimiento.setLong(1, idProducto); movimiento.setLong(2, idEmpleado);
                    movimiento.setBigDecimal(3, qty); movimiento.setLong(4, idVenta);
                    movimiento.setString(5, "Venta registrada");
                    movimiento.executeUpdate();
                }
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    private void actualizarVenta(long idVenta, long idCliente, long idEmpleado, long idProducto,
            Date fechaVenta, BigDecimal qty, BigDecimal unitario, BigDecimal rebaja,
            BigDecimal total) throws SQLException {
        try (Connection conexion = ConexionOracle.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                long idDetalle = 0;
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT ID_DETALLE_VENTA, ID_PRODUCTO, CANTIDAD "
                        + "FROM DETALLE_VENTA WHERE ID_VENTA=? ORDER BY ID_DETALLE_VENTA")) {
                    consulta.setLong(1, idVenta);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        while (resultado.next()) {
                            if (idDetalle == 0) idDetalle = resultado.getLong("ID_DETALLE_VENTA");
                            try (PreparedStatement stock = conexion.prepareStatement(
                                    "UPDATE PRODUCTO SET STOCK=STOCK+? WHERE ID_PRODUCTO=?")) {
                                stock.setBigDecimal(1, resultado.getBigDecimal("CANTIDAD"));
                                stock.setLong(2, resultado.getLong("ID_PRODUCTO"));
                                stock.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement movimiento = conexion.prepareStatement(
                        "DELETE FROM MOVIMIENTO_INVENTARIO WHERE ID_VENTA=?")) {
                    movimiento.setLong(1, idVenta);
                    movimiento.executeUpdate();
                }
                try (PreparedStatement venta = conexion.prepareStatement(
                        "UPDATE VENTA SET ID_CLIENTE=?, ID_EMPLEADO=?, FECHA_VENTA=?, TOTAL=? WHERE ID_VENTA=?")) {
                    venta.setLong(1, idCliente);
                    venta.setLong(2, idEmpleado);
                    venta.setDate(3, fechaVenta);
                    venta.setBigDecimal(4, total);
                    venta.setLong(5, idVenta);
                    venta.executeUpdate();
                }
                BigDecimal stock;
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT STOCK FROM PRODUCTO WHERE ID_PRODUCTO=? FOR UPDATE")) {
                    consulta.setLong(1, idProducto);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        if (!resultado.next()) throw new SQLException("El producto no existe.");
                        stock = resultado.getBigDecimal(1);
                    }
                }
                if (stock.compareTo(qty) < 0) {
                    throw new SQLException("Stock insuficiente. Disponible: " + stock);
                }
                String detalleSql = idDetalle == 0
                        ? "INSERT INTO DETALLE_VENTA "
                        + "(ID_VENTA, ID_PRODUCTO, CANTIDAD, PRECIO_UNITARIO, DESCUENTO) "
                        + "VALUES (?, ?, ?, ?, ?)"
                        : "UPDATE DETALLE_VENTA SET ID_PRODUCTO=?, CANTIDAD=?, "
                        + "PRECIO_UNITARIO=?, DESCUENTO=? WHERE ID_DETALLE_VENTA=?";
                try (PreparedStatement detalle = conexion.prepareStatement(detalleSql)) {
                    if (idDetalle == 0) {
                        detalle.setLong(1, idVenta);
                        detalle.setLong(2, idProducto);
                        detalle.setBigDecimal(3, qty);
                        detalle.setBigDecimal(4, unitario);
                        detalle.setBigDecimal(5, rebaja);
                    } else {
                        detalle.setLong(1, idProducto);
                        detalle.setBigDecimal(2, qty);
                        detalle.setBigDecimal(3, unitario);
                        detalle.setBigDecimal(4, rebaja);
                        detalle.setLong(5, idDetalle);
                    }
                    detalle.executeUpdate();
                }
                try (PreparedStatement stockUpdate = conexion.prepareStatement(
                        "UPDATE PRODUCTO SET STOCK=STOCK-? WHERE ID_PRODUCTO=?")) {
                    stockUpdate.setBigDecimal(1, qty);
                    stockUpdate.setLong(2, idProducto);
                    stockUpdate.executeUpdate();
                }
                try (PreparedStatement movimiento = conexion.prepareStatement(
                        "INSERT INTO MOVIMIENTO_INVENTARIO "
                        + "(ID_PRODUCTO, ID_EMPLEADO, TIPO_MOVIMIENTO, CANTIDAD, ID_VENTA, OBSERVACION) "
                        + "VALUES (?, ?, 'SALIDA', ?, ?, ?)")) {
                    movimiento.setLong(1, idProducto);
                    movimiento.setLong(2, idEmpleado);
                    movimiento.setBigDecimal(3, qty);
                    movimiento.setLong(4, idVenta);
                    movimiento.setString(5, "Venta actualizada");
                    movimiento.executeUpdate();
                }
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    private void recargar() {
        String sql = "SELECT v.ID_VENTA, c.NOMBRES || ' ' || c.APELLIDOS, "
                + "e.NOMBRES || ' ' || e.APELLIDOS, p.NOMBRE, dv.CANTIDAD, "
                + "v.FECHA_VENTA, v.TOTAL "
                + "FROM VENTA v JOIN CLIENTE c ON c.ID_CLIENTE=v.ID_CLIENTE "
                + "JOIN EMPLEADO e ON e.ID_EMPLEADO=v.ID_EMPLEADO "
                + "JOIN DETALLE_VENTA dv ON dv.ID_VENTA=v.ID_VENTA "
                + "JOIN PRODUCTO p ON p.ID_PRODUCTO=dv.ID_PRODUCTO "
                + "ORDER BY v.ID_VENTA, dv.ID_DETALLE_VENTA";
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            modelo.setRowCount(0);
            while (resultado.next()) {
                modelo.addRow(new Object[]{resultado.getLong(1), resultado.getString(2),
                    resultado.getString(3), resultado.getString(4), resultado.getBigDecimal(5),
                    resultado.getDate(6), resultado.getBigDecimal(7)});
            }
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudieron cargar las ventas", ex));
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { error("Seleccione una venta."); return; }
        if (JOptionPane.showConfirmDialog(this, "La venta sera eliminada y se restaurara el stock.",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        fila = tabla.convertRowIndexToModel(fila);
        long idVenta = ((Number) modelo.getValueAt(fila, 0)).longValue();
        try (Connection conexion = ConexionOracle.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT ID_PRODUCTO, CANTIDAD FROM DETALLE_VENTA WHERE ID_VENTA=?")) {
                    consulta.setLong(1, idVenta);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        while (resultado.next()) {
                            try (PreparedStatement stock = conexion.prepareStatement(
                                    "UPDATE PRODUCTO SET STOCK=STOCK+? WHERE ID_PRODUCTO=?")) {
                                stock.setBigDecimal(1, resultado.getBigDecimal(2));
                                stock.setLong(2, resultado.getLong(1)); stock.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement movimiento = conexion.prepareStatement(
                        "DELETE FROM MOVIMIENTO_INVENTARIO WHERE ID_VENTA=?")) {
                    movimiento.setLong(1, idVenta); movimiento.executeUpdate();
                }
                try (PreparedStatement borrar = conexion.prepareStatement("DELETE FROM VENTA WHERE ID_VENTA=?")) {
                    borrar.setLong(1, idVenta); borrar.executeUpdate();
                }
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
            recargar();
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudo eliminar la venta", ex));
        }
    }

    private BigDecimal decimal(JTextField campo, String nombre) {
        try { return new BigDecimal(campo.getText().trim()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException(nombre + " invalido."); }
    }

    private void limpiar() {
        idVentaSeleccionada = 0;
        tabla.clearSelection();
        clientes.setSelectedItem(null);
        empleados.setSelectedItem(null);
        productos.setSelectedItem(null);
        fecha.setText(LocalDate.now().toString()); cantidad.setText("1"); descuento.setText("0");
        precio.setText("");
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Proyecto03", JOptionPane.ERROR_MESSAGE);
    }
}
