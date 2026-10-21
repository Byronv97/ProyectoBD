package vistas;

import clases.MensajesSQL;
import conexion.ConexionOracle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Consulta el historial de entradas y salidas generado por compras y ventas. */
public class MovimientoInventarioPanel extends JPanel {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Producto", "Empleado", "Tipo", "Cantidad", "Fecha", "Origen", "Observacion"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final javax.swing.JTextField busqueda = new javax.swing.JTextField(18);

    public MovimientoInventarioPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
        setBackground(new Color(247, 249, 252));
        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        JLabel titulo = new JLabel("Movimientos de inventario");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        FiltroTabla.configurar(tabla, busqueda);
        superior.add(titulo, BorderLayout.WEST);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        acciones.add(new JLabel("Buscar:"));
        acciones.add(busqueda);
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> recargar());
        acciones.add(actualizar);
        superior.add(acciones, BorderLayout.EAST);
        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        recargar();
    }

    private void recargar() {
        String sql = "SELECT m.ID_MOVIMIENTO, p.NOMBRE, e.NOMBRES || ' ' || e.APELLIDOS, "
                + "m.TIPO_MOVIMIENTO, m.CANTIDAD, m.FECHA_MOVIMIENTO, "
                + "CASE WHEN m.ID_COMPRA IS NOT NULL THEN 'COMPRA ' || m.ID_COMPRA "
                + "WHEN m.ID_VENTA IS NOT NULL THEN 'VENTA ' || m.ID_VENTA ELSE 'AJUSTE' END, "
                + "m.OBSERVACION FROM MOVIMIENTO_INVENTARIO m "
                + "JOIN PRODUCTO p ON p.ID_PRODUCTO=m.ID_PRODUCTO "
                + "JOIN EMPLEADO e ON e.ID_EMPLEADO=m.ID_EMPLEADO "
                + "ORDER BY m.ID_MOVIMIENTO";
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            modelo.setRowCount(0);
            while (resultado.next()) {
                modelo.addRow(new Object[]{resultado.getLong(1), resultado.getString(2),
                    resultado.getString(3), resultado.getString(4), resultado.getBigDecimal(5),
                    resultado.getDate(6), resultado.getString(7), resultado.getString(8)});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    MensajesSQL.describir("No se pudieron cargar los movimientos", ex),
                    "Proyecto03", JOptionPane.ERROR_MESSAGE);
        }
    }
}
