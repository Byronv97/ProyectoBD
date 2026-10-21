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

/** Registro de compra completo en una sola pantalla. */
public class CompraFormularioPanel extends JPanel {

    private final CrudDAO dao = new CrudDAO();
    private final JComboBox<CrudDAO.Opcion> proveedores = new JComboBox<>();
    private final JComboBox<CrudDAO.Opcion> empleados = new JComboBox<>();
    private final JComboBox<CrudDAO.ProductoOpcion> productos = new JComboBox<>();
    private final JTextField factura = new JTextField(12);
    private final JTextField fecha = new JTextField(LocalDate.now().toString(), 12);
    private final JTextField cantidad = new JTextField("1", 12);
    private final JTextField costo = new JTextField(12);
    private final JTextField descuento = new JTextField("0", 12);
    private final JTextField busqueda = new JTextField(18);
    private long idCompraSeleccionada;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Proveedor", "Empleado", "Factura", "Fecha", "Total"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    public CompraFormularioPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
        setBackground(new Color(247, 249, 252));
        JLabel titulo = new JLabel("Compras");
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
        productos.addActionListener(e -> actualizarCosto());
    }

    private JPanel crearFormulario() {
        JPanel envoltura = new JPanel(new BorderLayout(8, 8));
        envoltura.setBackground(Color.WHITE);
        envoltura.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        agregar(campos, "Proveedor *", proveedores, 0, 0);
        agregar(campos, "Producto *", productoPanel(), 2, 0);
        agregar(campos, "Empleado *", empleados, 0, 1);
        agregar(campos, "Numero de factura *", factura, 2, 1);
        agregar(campos, "Fecha (yyyy-mm-dd) *", fecha, 0, 2);
        agregar(campos, "Cantidad *", cantidad, 2, 2);
        agregar(campos, "Costo unitario *", costo, 0, 3);
        agregar(campos, "Descuento", descuento, 2, 3);
        envoltura.add(campos, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        acciones.add(new JLabel("Buscar:"));
        acciones.add(busqueda);
        JButton limpiar = new JButton("Limpiar");
        limpiar.addActionListener(e -> limpiar());
        JButton guardar = new JButton("Guardar compra");
        guardar.addActionListener(e -> guardar());
        JButton modificar = new JButton("Actualizar compra");
        modificar.addActionListener(e -> actualizarSeleccionada());
        JButton eliminar = new JButton("Eliminar compra");
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
            proveedores.removeAllItems();
            empleados.removeAllItems();
            productos.removeAllItems();
            cargarOpciones(proveedores, dao.listarOpciones("PROVEEDOR", "ID_PROVEEDOR", "RAZON_SOCIAL"));
            cargarOpciones(empleados, dao.listarOpciones("EMPLEADO", "ID_EMPLEADO", "NOMBRES"));
            for (CrudDAO.ProductoOpcion producto : dao.listarProductos()) productos.addItem(producto);
        } catch (SQLException ex) {
            error("No se pudieron cargar las listas de compra:\n" + ex.getMessage());
        }
    }

    private void cargarOpciones(JComboBox<CrudDAO.Opcion> combo, List<CrudDAO.Opcion> opciones) {
        for (CrudDAO.Opcion opcion : opciones) combo.addItem(opcion);
    }

    private JPanel productoPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JButton nuevo = new JButton("+ Producto");
        nuevo.setToolTipText("Crear un producto sin salir de la compra");
        nuevo.addActionListener(e -> new ProductoDialog(SwingUtilities.getWindowAncestor(this),
                this::refrescarListas).setVisible(true));
        panel.add(productos, BorderLayout.CENTER);
        panel.add(nuevo, BorderLayout.EAST);
        return panel;
    }

    private void actualizarCosto() {
        CrudDAO.ProductoOpcion producto = (CrudDAO.ProductoOpcion) productos.getSelectedItem();
        if (producto != null) costo.setText(producto.precio().toPlainString());
    }

    private void cargarSeleccion() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) return;
        int fila = tabla.convertRowIndexToModel(filaVista);
        fila = tabla.convertRowIndexToModel(fila);
        long idCompra = ((Number) modelo.getValueAt(fila, 0)).longValue();
        idCompraSeleccionada = idCompra;
        String sql = "SELECT co.ID_PROVEEDOR, co.ID_EMPLEADO, d.ID_PRODUCTO, d.CANTIDAD, "
                + "d.COSTO_UNITARIO, d.DESCUENTO, co.NUMERO_FACTURA, co.FECHA_COMPRA "
                + "FROM COMPRA co JOIN DETALLE_COMPRA d ON d.ID_COMPRA=co.ID_COMPRA "
                + "WHERE co.ID_COMPRA=?";
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idCompra);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    seleccionar(proveedores, resultado.getLong("ID_PROVEEDOR"));
                    seleccionar(empleados, resultado.getLong("ID_EMPLEADO"));
                    seleccionarProducto(resultado.getLong("ID_PRODUCTO"));
                    factura.setText(resultado.getString("NUMERO_FACTURA"));
                    fecha.setText(resultado.getDate("FECHA_COMPRA").toString());
                    cantidad.setText(resultado.getBigDecimal("CANTIDAD").toPlainString());
                    costo.setText(resultado.getBigDecimal("COSTO_UNITARIO").toPlainString());
                    descuento.setText(resultado.getBigDecimal("DESCUENTO").toPlainString());
                }
            }
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudo cargar la compra seleccionada", ex));
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
        CrudDAO.Opcion proveedor = (CrudDAO.Opcion) proveedores.getSelectedItem();
        CrudDAO.Opcion empleado = (CrudDAO.Opcion) empleados.getSelectedItem();
        CrudDAO.ProductoOpcion producto = (CrudDAO.ProductoOpcion) productos.getSelectedItem();
        if (proveedor == null || empleado == null || producto == null || factura.getText().isBlank()) {
            error("Seleccione proveedor, empleado, producto e ingrese la factura."); return;
        }
        try {
            BigDecimal qty = decimal(cantidad, "Cantidad");
            BigDecimal unitario = decimal(costo, "Costo");
            BigDecimal rebaja = decimal(descuento, "Descuento");
            if (qty.signum() <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            if (unitario.signum() < 0) throw new IllegalArgumentException("El costo no puede ser negativo.");
            if (rebaja.signum() < 0) throw new IllegalArgumentException("El descuento no puede ser negativo.");
            if (rebaja.compareTo(qty.multiply(unitario)) > 0) {
                throw new IllegalArgumentException("El descuento no puede superar el subtotal.");
            }
            Date fechaCompra = Date.valueOf(fecha.getText().trim());
            BigDecimal total = qty.multiply(unitario).subtract(rebaja);
            long idCompra = idCompraSeleccionada;
            int filaVista = tabla.getSelectedRow();
            if (filaVista >= 0) {
                int fila = tabla.convertRowIndexToModel(filaVista);
                idCompra = ((Number) modelo.getValueAt(fila, 0)).longValue();
            }
            if (idCompra == 0) {
                registrar(proveedor.id(), empleado.id(), producto.id(), fechaCompra,
                        qty, unitario, rebaja, total, factura.getText().trim());
            } else {
                actualizarCompra(idCompra, proveedor.id(), empleado.id(), producto.id(),
                        fechaCompra, qty, unitario, rebaja, total, factura.getText().trim());
            }
            limpiar();
            recargar();
        } catch (IllegalArgumentException | SQLException ex) {
            error(ex instanceof SQLException sql
                    ? MensajesSQL.describir("No se pudo registrar la compra", sql)
                    : "No se pudo registrar la compra:\n" + ex.getMessage());
        }
    }

    private void actualizarSeleccionada() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            error("Seleccione una compra para actualizar.");
            return;
        }
        int fila = tabla.convertRowIndexToModel(filaVista);
        idCompraSeleccionada = ((Number) modelo.getValueAt(fila, 0)).longValue();
        guardar();
    }

    private void registrar(long idProveedor, long idEmpleado, long idProducto, Date fechaCompra,
            BigDecimal qty, BigDecimal unitario, BigDecimal rebaja, BigDecimal total,
            String numeroFactura) throws SQLException {
        try (Connection conexion = ConexionOracle.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                long idCompra;
                String sql = "INSERT INTO COMPRA "
                        + "(ID_PROVEEDOR, ID_EMPLEADO, NUMERO_FACTURA, FECHA_COMPRA, TOTAL) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement compra = conexion.prepareStatement(sql,
                        new String[]{"ID_COMPRA"})) {
                    compra.setLong(1, idProveedor); compra.setLong(2, idEmpleado);
                    compra.setString(3, numeroFactura); compra.setDate(4, fechaCompra);
                    compra.setBigDecimal(5, total); compra.executeUpdate();
                    try (ResultSet claves = compra.getGeneratedKeys()) {
                        if (!claves.next()) throw new SQLException("No se obtuvo el ID de la compra.");
                        idCompra = claves.getLong(1);
                    }
                }
                try (PreparedStatement detalle = conexion.prepareStatement(
                        "INSERT INTO DETALLE_COMPRA "
                        + "(ID_COMPRA, ID_PRODUCTO, CANTIDAD, COSTO_UNITARIO, DESCUENTO) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
                    detalle.setLong(1, idCompra); detalle.setLong(2, idProducto);
                    detalle.setBigDecimal(3, qty); detalle.setBigDecimal(4, unitario);
                    detalle.setBigDecimal(5, rebaja); detalle.executeUpdate();
                }
                try (PreparedStatement stock = conexion.prepareStatement(
                        "UPDATE PRODUCTO SET STOCK=STOCK+?, COSTO_REFERENCIA=? WHERE ID_PRODUCTO=?")) {
                    stock.setBigDecimal(1, qty); stock.setBigDecimal(2, unitario);
                    stock.setLong(3, idProducto); stock.executeUpdate();
                }
                try (PreparedStatement movimiento = conexion.prepareStatement(
                        "INSERT INTO MOVIMIENTO_INVENTARIO "
                        + "(ID_PRODUCTO, ID_EMPLEADO, TIPO_MOVIMIENTO, CANTIDAD, ID_COMPRA, OBSERVACION) "
                        + "VALUES (?, ?, 'ENTRADA', ?, ?, ?)")) {
                    movimiento.setLong(1, idProducto); movimiento.setLong(2, idEmpleado);
                    movimiento.setBigDecimal(3, qty); movimiento.setLong(4, idCompra);
                    movimiento.setString(5, "Compra registrada");
                    movimiento.executeUpdate();
                }
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    private void actualizarCompra(long idCompra, long idProveedor, long idEmpleado, long idProducto,
            Date fechaCompra, BigDecimal qty, BigDecimal unitario, BigDecimal rebaja,
            BigDecimal total, String numeroFactura) throws SQLException {
        try (Connection conexion = ConexionOracle.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                long idDetalle = 0;
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT ID_DETALLE_COMPRA, ID_PRODUCTO, CANTIDAD "
                        + "FROM DETALLE_COMPRA WHERE ID_COMPRA=? ORDER BY ID_DETALLE_COMPRA")) {
                    consulta.setLong(1, idCompra);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        while (resultado.next()) {
                            if (idDetalle == 0) idDetalle = resultado.getLong("ID_DETALLE_COMPRA");
                            try (PreparedStatement stock = conexion.prepareStatement(
                                    "UPDATE PRODUCTO SET STOCK=STOCK-? WHERE ID_PRODUCTO=?")) {
                                stock.setBigDecimal(1, resultado.getBigDecimal("CANTIDAD"));
                                stock.setLong(2, resultado.getLong("ID_PRODUCTO"));
                                stock.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement movimiento = conexion.prepareStatement(
                        "DELETE FROM MOVIMIENTO_INVENTARIO WHERE ID_COMPRA=?")) {
                    movimiento.setLong(1, idCompra);
                    movimiento.executeUpdate();
                }
                try (PreparedStatement compra = conexion.prepareStatement(
                        "UPDATE COMPRA SET ID_PROVEEDOR=?, ID_EMPLEADO=?, NUMERO_FACTURA=?, "
                        + "FECHA_COMPRA=?, TOTAL=? WHERE ID_COMPRA=?")) {
                    compra.setLong(1, idProveedor);
                    compra.setLong(2, idEmpleado);
                    compra.setString(3, numeroFactura);
                    compra.setDate(4, fechaCompra);
                    compra.setBigDecimal(5, total);
                    compra.setLong(6, idCompra);
                    compra.executeUpdate();
                }
                String detalleSql = idDetalle == 0
                        ? "INSERT INTO DETALLE_COMPRA "
                        + "(ID_COMPRA, ID_PRODUCTO, CANTIDAD, COSTO_UNITARIO, DESCUENTO) "
                        + "VALUES (?, ?, ?, ?, ?)"
                        : "UPDATE DETALLE_COMPRA SET ID_PRODUCTO=?, CANTIDAD=?, "
                        + "COSTO_UNITARIO=?, DESCUENTO=? WHERE ID_DETALLE_COMPRA=?";
                try (PreparedStatement detalle = conexion.prepareStatement(detalleSql)) {
                    if (idDetalle == 0) {
                        detalle.setLong(1, idCompra);
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
                try (PreparedStatement stock = conexion.prepareStatement(
                        "UPDATE PRODUCTO SET STOCK=STOCK+?, COSTO_REFERENCIA=? WHERE ID_PRODUCTO=?")) {
                    stock.setBigDecimal(1, qty);
                    stock.setBigDecimal(2, unitario);
                    stock.setLong(3, idProducto);
                    stock.executeUpdate();
                }
                try (PreparedStatement movimiento = conexion.prepareStatement(
                        "INSERT INTO MOVIMIENTO_INVENTARIO "
                        + "(ID_PRODUCTO, ID_EMPLEADO, TIPO_MOVIMIENTO, CANTIDAD, ID_COMPRA, OBSERVACION) "
                        + "VALUES (?, ?, 'ENTRADA', ?, ?, ?)")) {
                    movimiento.setLong(1, idProducto);
                    movimiento.setLong(2, idEmpleado);
                    movimiento.setBigDecimal(3, qty);
                    movimiento.setLong(4, idCompra);
                    movimiento.setString(5, "Compra actualizada");
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
        String sql = "SELECT co.ID_COMPRA, p.RAZON_SOCIAL, e.NOMBRES || ' ' || e.APELLIDOS, "
                + "co.NUMERO_FACTURA, co.FECHA_COMPRA, co.TOTAL "
                + "FROM COMPRA co JOIN PROVEEDOR p ON p.ID_PROVEEDOR=co.ID_PROVEEDOR "
                + "JOIN EMPLEADO e ON e.ID_EMPLEADO=co.ID_EMPLEADO ORDER BY co.ID_COMPRA";
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            modelo.setRowCount(0);
            while (resultado.next()) {
                modelo.addRow(new Object[]{resultado.getLong(1), resultado.getString(2),
                    resultado.getString(3), resultado.getString(4), resultado.getDate(5),
                    resultado.getBigDecimal(6)});
            }
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudieron cargar las compras", ex));
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { error("Seleccione una compra."); return; }
        if (JOptionPane.showConfirmDialog(this, "La compra sera eliminada y se revertira el stock.",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        long idCompra = ((Number) modelo.getValueAt(fila, 0)).longValue();
        try (Connection conexion = ConexionOracle.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT ID_PRODUCTO, CANTIDAD FROM DETALLE_COMPRA WHERE ID_COMPRA=?")) {
                    consulta.setLong(1, idCompra);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        while (resultado.next()) {
                            try (PreparedStatement stock = conexion.prepareStatement(
                                    "UPDATE PRODUCTO SET STOCK=STOCK-? WHERE ID_PRODUCTO=?")) {
                                stock.setBigDecimal(1, resultado.getBigDecimal(2));
                                stock.setLong(2, resultado.getLong(1)); stock.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement movimiento = conexion.prepareStatement(
                        "DELETE FROM MOVIMIENTO_INVENTARIO WHERE ID_COMPRA=?")) {
                    movimiento.setLong(1, idCompra); movimiento.executeUpdate();
                }
                try (PreparedStatement borrar = conexion.prepareStatement("DELETE FROM COMPRA WHERE ID_COMPRA=?")) {
                    borrar.setLong(1, idCompra); borrar.executeUpdate();
                }
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
            recargar();
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudo eliminar la compra", ex));
        }
    }

    private BigDecimal decimal(JTextField campo, String nombre) {
        try { return new BigDecimal(campo.getText().trim()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException(nombre + " invalido."); }
    }

    private void limpiar() {
        idCompraSeleccionada = 0;
        factura.setText(""); fecha.setText(LocalDate.now().toString());
        cantidad.setText("1"); descuento.setText("0"); actualizarCosto();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Proyecto03", JOptionPane.ERROR_MESSAGE);
    }
}
