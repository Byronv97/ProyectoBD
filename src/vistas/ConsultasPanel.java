package vistas;

import clases.CrudDAO;
import clases.MensajesSQL;
import conexion.ConexionOracle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Panel de consultas parametrizadas, agrupadas segun el utilitario. */
public class ConsultasPanel extends JPanel {

    private enum TipoParametro { DEPARTAMENTO, CLIENTE, EMPLEADO, PROVEEDOR, NUMERO, FECHA }

    private record Parametro(String etiqueta, TipoParametro tipo) { }

    private record Consulta(String nombre, String sql, Parametro... parametros) {
        @Override public String toString() { return nombre; }
    }

    private final CrudDAO dao = new CrudDAO();
    private final JComboBox<Consulta> consultas = new JComboBox<>();
    private final JPanel parametros = new JPanel(new GridLayout(0, 2, 8, 6));
    private final DefaultTableModel modelo = new DefaultTableModel() {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final List<JComponent> entradas = new ArrayList<>();

    public ConsultasPanel(UtilitarioFrame.Tipo modulo) {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
        setBackground(new Color(247, 249, 252));

        JLabel titulo = new JLabel("Consultas de " + nombreModulo(modulo));
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.setOpaque(false);
        superior.add(titulo, BorderLayout.NORTH);
        superior.add(crearControles(), BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        Consulta[] definiciones = definiciones(modulo);
        for (Consulta consulta : definiciones) consultas.addItem(consulta);
        consultas.addActionListener(e -> mostrarParametros());
        if (consultas.getItemCount() > 0) {
            consultas.setSelectedIndex(0);
            mostrarParametros();
        }
    }

    private JPanel crearControles() {
        JPanel envoltura = new JPanel(new BorderLayout(8, 8));
        envoltura.setBackground(Color.WHITE);
        envoltura.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel seleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        seleccion.setOpaque(false);
        seleccion.add(new JLabel("Consulta:"));
        consultas.setPreferredSize(new java.awt.Dimension(360, 26));
        seleccion.add(consultas);
        JButton ejecutar = new JButton("Ejecutar consulta");
        ejecutar.addActionListener(e -> ejecutar());
        seleccion.add(ejecutar);
        envoltura.add(seleccion, BorderLayout.NORTH);

        parametros.setOpaque(false);
        envoltura.add(parametros, BorderLayout.CENTER);
        return envoltura;
    }

    private void mostrarParametros() {
        parametros.removeAll();
        entradas.clear();
        Consulta consulta = (Consulta) consultas.getSelectedItem();
        if (consulta != null) {
            for (Parametro parametro : consulta.parametros()) {
                parametros.add(new JLabel(parametro.etiqueta()));
                JComponent entrada = crearEntrada(parametro);
                entradas.add(entrada);
                parametros.add(entrada);
            }
        }
        parametros.revalidate();
        parametros.repaint();
    }

    private JComponent crearEntrada(Parametro parametro) {
        if (parametro.tipo() == TipoParametro.DEPARTAMENTO) {
            return combo("DEPARTAMENTO", "ID_DEPARTAMENTO", "NOMBRE");
        }
        if (parametro.tipo() == TipoParametro.CLIENTE) {
            return combo("CLIENTE", "ID_CLIENTE", "NOMBRES");
        }
        if (parametro.tipo() == TipoParametro.EMPLEADO) {
            return combo("EMPLEADO", "ID_EMPLEADO", "NOMBRES");
        }
        if (parametro.tipo() == TipoParametro.PROVEEDOR) {
            return combo("PROVEEDOR", "ID_PROVEEDOR", "RAZON_SOCIAL");
        }
        if (parametro.tipo() == TipoParametro.FECHA) {
            String fecha = parametro.etiqueta().toLowerCase().contains("final")
                    ? LocalDate.now().toString()
                    : "2000-01-01";
            return new JTextField(fecha, 12);
        }
        return new JTextField("0", 12);
    }

    private JComboBox<CrudDAO.Opcion> combo(String tabla, String id, String texto) {
        JComboBox<CrudDAO.Opcion> combo = new JComboBox<>();
        try {
            for (CrudDAO.Opcion opcion : dao.listarOpciones(tabla, id, texto)) combo.addItem(opcion);
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudo cargar el filtro", ex));
        }
        return combo;
    }

    private void ejecutar() {
        Consulta consulta = (Consulta) consultas.getSelectedItem();
        if (consulta == null) return;
        try (Connection conexion = ConexionOracle.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(consulta.sql())) {
            for (int i = 0; i < consulta.parametros().length; i++) {
                asignar(sentencia, i + 1, consulta.parametros()[i].tipo(), entradas.get(i));
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                cargarResultado(resultado);
            }
        } catch (IllegalArgumentException | SQLException ex) {
            error(ex instanceof SQLException sql
                    ? MensajesSQL.describir("No se pudo ejecutar la consulta", sql)
                    : ex.getMessage());
        }
    }

    private void asignar(PreparedStatement sentencia, int indice, TipoParametro tipo,
            JComponent entrada) throws SQLException {
        if (entrada instanceof JComboBox<?> combo) {
            Object seleccionado = combo.getSelectedItem();
            if (seleccionado == null) throw new IllegalArgumentException("Seleccione un filtro.");
            sentencia.setLong(indice, ((CrudDAO.Opcion) seleccionado).id());
            return;
        }
        String valor = ((JTextField) entrada).getText().trim();
        if (tipo == TipoParametro.FECHA) {
            try {
                sentencia.setDate(indice, Date.valueOf(valor));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("La fecha debe tener formato yyyy-mm-dd.");
            }
            return;
        }
        try {
            BigDecimal numero = new BigDecimal(valor);
            if (numero.signum() < 0) throw new IllegalArgumentException("El numero no puede ser negativo.");
            sentencia.setBigDecimal(indice, numero);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingrese un numero valido.");
        }
    }

    private void cargarResultado(ResultSet resultado) throws SQLException {
        ResultSetMetaData metadata = resultado.getMetaData();
        String[] columnas = new String[metadata.getColumnCount()];
        for (int i = 0; i < columnas.length; i++) columnas[i] = metadata.getColumnLabel(i + 1);
        modelo.setColumnIdentifiers(columnas);
        modelo.setRowCount(0);
        while (resultado.next()) {
            Object[] fila = new Object[columnas.length];
            for (int i = 0; i < fila.length; i++) fila[i] = resultado.getObject(i + 1);
            modelo.addRow(fila);
        }
    }

    private String nombreModulo(UtilitarioFrame.Tipo modulo) {
        return switch (modulo) {
            case EMPLEADOS -> "empleados";
            case INVENTARIO -> "inventario";
            case VENTAS -> "ventas";
            case COMPRAS -> "compras";
        };
    }

    private Consulta[] definiciones(UtilitarioFrame.Tipo modulo) {
        return switch (modulo) {
            case EMPLEADOS -> empleados();
            case INVENTARIO -> inventario();
            case VENTAS -> ventas();
            case COMPRAS -> compras();
        };
    }

    private Consulta[] empleados() {
        return new Consulta[]{
            q("Listado general de empleados", "SELECT e.ID_EMPLEADO, e.NOMBRES, e.APELLIDOS, d.NOMBRE AS DEPARTAMENTO, e.CARGO, e.EMAIL, e.TELEFONO, e.FECHA_INGRESO, e.SALARIO FROM EMPLEADO e JOIN DEPARTAMENTO d ON d.ID_DEPARTAMENTO=e.ID_DEPARTAMENTO ORDER BY e.ID_EMPLEADO"),
            q("Empleados por departamento", "SELECT e.ID_EMPLEADO, e.NOMBRES, e.APELLIDOS, d.NOMBRE AS DEPARTAMENTO, e.CARGO, e.SALARIO FROM EMPLEADO e JOIN DEPARTAMENTO d ON d.ID_DEPARTAMENTO=e.ID_DEPARTAMENTO WHERE e.ID_DEPARTAMENTO=? ORDER BY e.APELLIDOS", p("Departamento", TipoParametro.DEPARTAMENTO)),
            q("Cantidad de empleados por departamento", "SELECT d.NOMBRE AS DEPARTAMENTO, COUNT(e.ID_EMPLEADO) AS CANTIDAD FROM DEPARTAMENTO d LEFT JOIN EMPLEADO e ON e.ID_DEPARTAMENTO=d.ID_DEPARTAMENTO GROUP BY d.NOMBRE ORDER BY CANTIDAD DESC"),
            q("Salario promedio por departamento", "SELECT d.NOMBRE AS DEPARTAMENTO, AVG(e.SALARIO) AS SALARIO_PROMEDIO FROM DEPARTAMENTO d LEFT JOIN EMPLEADO e ON e.ID_DEPARTAMENTO=d.ID_DEPARTAMENTO GROUP BY d.NOMBRE ORDER BY d.NOMBRE"),
            q("Empleados con salario superior al promedio", "SELECT e.ID_EMPLEADO, e.NOMBRES, e.APELLIDOS, e.SALARIO FROM EMPLEADO e WHERE e.SALARIO > (SELECT AVG(SALARIO) FROM EMPLEADO WHERE SALARIO IS NOT NULL) ORDER BY e.SALARIO DESC"),
            q("Gasto total en salarios", "SELECT COALESCE(SUM(SALARIO), 0) AS GASTO_TOTAL_SALARIOS FROM EMPLEADO"),
            q("Empleados por fecha de ingreso", "SELECT e.ID_EMPLEADO, e.NOMBRES, e.APELLIDOS, e.FECHA_INGRESO, d.NOMBRE AS DEPARTAMENTO FROM EMPLEADO e JOIN DEPARTAMENTO d ON d.ID_DEPARTAMENTO=e.ID_DEPARTAMENTO WHERE e.FECHA_INGRESO BETWEEN ? AND ? ORDER BY e.FECHA_INGRESO", p("Fecha inicial", TipoParametro.FECHA), p("Fecha final", TipoParametro.FECHA)),
            q("Empleados con mayor y menor salario", "SELECT 'MAYOR' AS TIPO, ID_EMPLEADO, NOMBRES, APELLIDOS, SALARIO FROM EMPLEADO WHERE SALARIO=(SELECT MAX(SALARIO) FROM EMPLEADO) UNION ALL SELECT 'MENOR', ID_EMPLEADO, NOMBRES, APELLIDOS, SALARIO FROM EMPLEADO WHERE SALARIO=(SELECT MIN(SALARIO) FROM EMPLEADO) ORDER BY TIPO")
        };
    }

    private Consulta[] inventario() {
        return new Consulta[]{
            q("Listado general de productos", "SELECT ID_PRODUCTO, CODIGO, NOMBRE, DESCRIPCION, PRECIO_VENTA, COSTO_REFERENCIA, STOCK FROM PRODUCTO ORDER BY NOMBRE"),
            q("Productos con bajo stock", "SELECT ID_PRODUCTO, CODIGO, NOMBRE, STOCK FROM PRODUCTO WHERE STOCK <= ? ORDER BY STOCK", p("Stock maximo", TipoParametro.NUMERO)),
            q("Valor total del inventario", "SELECT COALESCE(SUM(STOCK * COSTO_REFERENCIA), 0) AS VALOR_TOTAL_INVENTARIO FROM PRODUCTO"),
            q("Productos mas vendidos", "SELECT p.ID_PRODUCTO, p.NOMBRE, SUM(dv.CANTIDAD) AS CANTIDAD_VENDIDA FROM PRODUCTO p JOIN DETALLE_VENTA dv ON dv.ID_PRODUCTO=p.ID_PRODUCTO GROUP BY p.ID_PRODUCTO, p.NOMBRE ORDER BY CANTIDAD_VENDIDA DESC"),
            q("Historial de movimientos de inventario", "SELECT m.ID_MOVIMIENTO, p.NOMBRE AS PRODUCTO, e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO, m.TIPO_MOVIMIENTO, m.CANTIDAD, m.FECHA_MOVIMIENTO, m.ID_COMPRA, m.ID_VENTA, m.OBSERVACION FROM MOVIMIENTO_INVENTARIO m JOIN PRODUCTO p ON p.ID_PRODUCTO=m.ID_PRODUCTO JOIN EMPLEADO e ON e.ID_EMPLEADO=m.ID_EMPLEADO ORDER BY m.FECHA_MOVIMIENTO DESC"),
            q("Productos sin movimientos", "SELECT p.ID_PRODUCTO, p.CODIGO, p.NOMBRE, p.STOCK FROM PRODUCTO p WHERE NOT EXISTS (SELECT 1 FROM MOVIMIENTO_INVENTARIO m WHERE m.ID_PRODUCTO=p.ID_PRODUCTO) ORDER BY p.NOMBRE"),
            q("Productos con mayor cantidad de existencias", "SELECT ID_PRODUCTO, CODIGO, NOMBRE, STOCK FROM PRODUCTO WHERE STOCK=(SELECT MAX(STOCK) FROM PRODUCTO)"),
            q("Entradas y salidas por producto", "SELECT p.ID_PRODUCTO, p.NOMBRE, COALESCE(SUM(CASE WHEN m.TIPO_MOVIMIENTO='ENTRADA' THEN m.CANTIDAD ELSE 0 END), 0) AS ENTRADAS, COALESCE(SUM(CASE WHEN m.TIPO_MOVIMIENTO='SALIDA' THEN m.CANTIDAD ELSE 0 END), 0) AS SALIDAS FROM PRODUCTO p LEFT JOIN MOVIMIENTO_INVENTARIO m ON m.ID_PRODUCTO=p.ID_PRODUCTO GROUP BY p.ID_PRODUCTO, p.NOMBRE ORDER BY p.NOMBRE")
        };
    }

    private Consulta[] ventas() {
        return new Consulta[]{
            q("Historial general de ventas", "SELECT v.ID_VENTA, c.NOMBRES || ' ' || c.APELLIDOS AS CLIENTE, e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO, p.NOMBRE AS PRODUCTO, dv.CANTIDAD, v.FECHA_VENTA, v.TOTAL FROM VENTA v JOIN CLIENTE c ON c.ID_CLIENTE=v.ID_CLIENTE JOIN EMPLEADO e ON e.ID_EMPLEADO=v.ID_EMPLEADO JOIN DETALLE_VENTA dv ON dv.ID_VENTA=v.ID_VENTA JOIN PRODUCTO p ON p.ID_PRODUCTO=dv.ID_PRODUCTO ORDER BY v.FECHA_VENTA DESC"),
            q("Ventas por cliente", "SELECT v.ID_VENTA, c.NOMBRES || ' ' || c.APELLIDOS AS CLIENTE, v.FECHA_VENTA, v.TOTAL FROM VENTA v JOIN CLIENTE c ON c.ID_CLIENTE=v.ID_CLIENTE WHERE v.ID_CLIENTE=? ORDER BY v.FECHA_VENTA DESC", p("Cliente", TipoParametro.CLIENTE)),
            q("Productos mas vendidos", "SELECT p.ID_PRODUCTO, p.NOMBRE, SUM(dv.CANTIDAD) AS CANTIDAD_VENDIDA FROM PRODUCTO p JOIN DETALLE_VENTA dv ON dv.ID_PRODUCTO=p.ID_PRODUCTO GROUP BY p.ID_PRODUCTO, p.NOMBRE ORDER BY CANTIDAD_VENDIDA DESC"),
            q("Ventas por empleado", "SELECT v.ID_VENTA, e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO, v.FECHA_VENTA, v.TOTAL FROM VENTA v JOIN EMPLEADO e ON e.ID_EMPLEADO=v.ID_EMPLEADO WHERE v.ID_EMPLEADO=? ORDER BY v.FECHA_VENTA DESC", p("Empleado", TipoParametro.EMPLEADO)),
            q("Ventas mensuales", "SELECT TRUNC(FECHA_VENTA, 'MM') AS MES, COUNT(*) AS CANTIDAD_VENTAS, SUM(TOTAL) AS TOTAL_VENTAS FROM VENTA GROUP BY TRUNC(FECHA_VENTA, 'MM') ORDER BY MES"),
            q("Clientes sin compras", "SELECT c.ID_CLIENTE, c.NOMBRES, c.APELLIDOS, c.EMAIL FROM CLIENTE c WHERE NOT EXISTS (SELECT 1 FROM VENTA v WHERE v.ID_CLIENTE=c.ID_CLIENTE) ORDER BY c.APELLIDOS"),
            q("Clientes con mayor cantidad de compras", "SELECT c.ID_CLIENTE, c.NOMBRES || ' ' || c.APELLIDOS AS CLIENTE, COUNT(v.ID_VENTA) AS CANTIDAD_COMPRAS FROM CLIENTE c JOIN VENTA v ON v.ID_CLIENTE=c.ID_CLIENTE GROUP BY c.ID_CLIENTE, c.NOMBRES, c.APELLIDOS ORDER BY CANTIDAD_COMPRAS DESC"),
            q("Total de ventas por periodo", "SELECT COUNT(*) AS CANTIDAD_VENTAS, COALESCE(SUM(TOTAL), 0) AS TOTAL_VENTAS FROM VENTA WHERE FECHA_VENTA BETWEEN ? AND ?", p("Fecha inicial", TipoParametro.FECHA), p("Fecha final", TipoParametro.FECHA)),
            q("Empleado con mayor cantidad de ventas", "SELECT e.ID_EMPLEADO, e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO, COUNT(v.ID_VENTA) AS CANTIDAD_VENTAS FROM EMPLEADO e JOIN VENTA v ON v.ID_EMPLEADO=e.ID_EMPLEADO GROUP BY e.ID_EMPLEADO, e.NOMBRES, e.APELLIDOS ORDER BY CANTIDAD_VENTAS DESC FETCH FIRST 1 ROWS WITH TIES"),
            q("Productos con mayores ingresos", "SELECT p.ID_PRODUCTO, p.NOMBRE, SUM(dv.CANTIDAD * dv.PRECIO_UNITARIO - dv.DESCUENTO) AS INGRESOS FROM PRODUCTO p JOIN DETALLE_VENTA dv ON dv.ID_PRODUCTO=p.ID_PRODUCTO GROUP BY p.ID_PRODUCTO, p.NOMBRE ORDER BY INGRESOS DESC")
        };
    }

    private Consulta[] compras() {
        return new Consulta[]{
            q("Historial general de compras", "SELECT co.ID_COMPRA, p.RAZON_SOCIAL AS PROVEEDOR, e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO, co.NUMERO_FACTURA, co.FECHA_COMPRA, co.TOTAL FROM COMPRA co JOIN PROVEEDOR p ON p.ID_PROVEEDOR=co.ID_PROVEEDOR JOIN EMPLEADO e ON e.ID_EMPLEADO=co.ID_EMPLEADO ORDER BY co.FECHA_COMPRA DESC"),
            q("Compras por proveedor", "SELECT co.ID_COMPRA, p.RAZON_SOCIAL AS PROVEEDOR, co.NUMERO_FACTURA, co.FECHA_COMPRA, co.TOTAL FROM COMPRA co JOIN PROVEEDOR p ON p.ID_PROVEEDOR=co.ID_PROVEEDOR WHERE co.ID_PROVEEDOR=? ORDER BY co.FECHA_COMPRA DESC", p("Proveedor", TipoParametro.PROVEEDOR)),
            q("Productos mas comprados", "SELECT p.ID_PRODUCTO, p.NOMBRE, SUM(dc.CANTIDAD) AS CANTIDAD_COMPRADA FROM PRODUCTO p JOIN DETALLE_COMPRA dc ON dc.ID_PRODUCTO=p.ID_PRODUCTO GROUP BY p.ID_PRODUCTO, p.NOMBRE ORDER BY CANTIDAD_COMPRADA DESC"),
            q("Compras mensuales", "SELECT TRUNC(FECHA_COMPRA, 'MM') AS MES, COUNT(*) AS CANTIDAD_COMPRAS, SUM(TOTAL) AS TOTAL_COMPRAS FROM COMPRA GROUP BY TRUNC(FECHA_COMPRA, 'MM') ORDER BY MES"),
            q("Proveedores sin compras", "SELECT p.ID_PROVEEDOR, p.RAZON_SOCIAL, p.EMAIL FROM PROVEEDOR p WHERE NOT EXISTS (SELECT 1 FROM COMPRA co WHERE co.ID_PROVEEDOR=p.ID_PROVEEDOR) ORDER BY p.RAZON_SOCIAL"),
            q("Compras por empleado", "SELECT co.ID_COMPRA, e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO, co.NUMERO_FACTURA, co.FECHA_COMPRA, co.TOTAL FROM COMPRA co JOIN EMPLEADO e ON e.ID_EMPLEADO=co.ID_EMPLEADO WHERE co.ID_EMPLEADO=? ORDER BY co.FECHA_COMPRA DESC", p("Empleado", TipoParametro.EMPLEADO)),
            q("Proveedores con mayor volumen de compras", "SELECT p.ID_PROVEEDOR, p.RAZON_SOCIAL, COUNT(co.ID_COMPRA) AS CANTIDAD_COMPRAS, SUM(co.TOTAL) AS VOLUMEN_COMPRAS FROM PROVEEDOR p JOIN COMPRA co ON co.ID_PROVEEDOR=p.ID_PROVEEDOR GROUP BY p.ID_PROVEEDOR, p.RAZON_SOCIAL ORDER BY VOLUMEN_COMPRAS DESC"),
            q("Total de compras por periodo", "SELECT COUNT(*) AS CANTIDAD_COMPRAS, COALESCE(SUM(TOTAL), 0) AS TOTAL_COMPRAS FROM COMPRA WHERE FECHA_COMPRA BETWEEN ? AND ?", p("Fecha inicial", TipoParametro.FECHA), p("Fecha final", TipoParametro.FECHA)),
            q("Historial de facturas de compras", "SELECT co.NUMERO_FACTURA, p.RAZON_SOCIAL AS PROVEEDOR, co.FECHA_COMPRA, co.TOTAL FROM COMPRA co JOIN PROVEEDOR p ON p.ID_PROVEEDOR=co.ID_PROVEEDOR ORDER BY co.FECHA_COMPRA DESC"),
            q("Productos con mayor inversion en compras", "SELECT p.ID_PRODUCTO, p.NOMBRE, SUM(dc.CANTIDAD * dc.COSTO_UNITARIO - dc.DESCUENTO) AS INVERSION FROM PRODUCTO p JOIN DETALLE_COMPRA dc ON dc.ID_PRODUCTO=p.ID_PRODUCTO GROUP BY p.ID_PRODUCTO, p.NOMBRE ORDER BY INVERSION DESC")
        };
    }

    private Consulta q(String nombre, String sql, Parametro... parametros) {
        return new Consulta(nombre, sql, parametros);
    }

    private Parametro p(String etiqueta, TipoParametro tipo) {
        return new Parametro(etiqueta, tipo);
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Proyecto03", JOptionPane.ERROR_MESSAGE);
    }
}
