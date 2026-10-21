package vistas;

import clases.CrudConfig;
import clases.CrudDAO;
import clases.MensajesSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.function.LongConsumer;
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
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Pantalla CRUD generica para maestros y movimientos simples. */
public class CrudPanel extends JPanel {

    private final CrudConfig config;
    private final CrudDAO dao = new CrudDAO();
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JComponent[] entradas;
    private long idSeleccionado;
    private LongConsumer alSeleccionar;
    private String filtroColumna;
    private Long filtroValor;
    private final JTextField busqueda = new JTextField(18);

    public CrudPanel(CrudConfig config) {
        this.config = config;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        setBackground(new Color(247, 249, 252));
        JLabel titulo = new JLabel(config.titulo());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 27));
        modelo = new DefaultTableModel(config.encabezados(), 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> seleccionarFila());
        entradas = new JComponent[config.campos().length];
        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.setOpaque(false);
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);
        cabecera.add(titulo, BorderLayout.WEST);
        busqueda.setToolTipText("Buscar en todos los campos visibles");
        busqueda.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarBusqueda(); }
            public void removeUpdate(DocumentEvent e) { aplicarBusqueda(); }
            public void changedUpdate(DocumentEvent e) { aplicarBusqueda(); }
        });
        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buscar.setOpaque(false);
        buscar.add(new JLabel("Buscar:"));
        buscar.add(busqueda);
        cabecera.add(buscar, BorderLayout.EAST);
        superior.add(cabecera, BorderLayout.NORTH);
        superior.add(crearFormulario(), BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        establecerValoresIniciales();
        recargar();
    }

    private JPanel crearFormulario() {
        JPanel contenedor = new JPanel(new BorderLayout(8, 8));
        contenedor.setBackground(Color.WHITE);
        contenedor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel campos = new JPanel(new GridLayout(0, 4, 8, 6));
        campos.setOpaque(false);
        for (int i = 0; i < config.campos().length; i++) {
            CrudConfig.Campo campo = config.campos()[i];
            if (campo.esRelacion()) {
                JComboBox<CrudDAO.Opcion> combo = new JComboBox<>();
                try {
                    for (CrudDAO.Opcion opcion : dao.listarOpciones(campo.tablaReferencia(),
                            campo.columna(), campo.columnaEtiqueta())) combo.addItem(opcion);
                } catch (SQLException ex) {
                    error(MensajesSQL.describir("No se pudo cargar la lista de "
                            + campo.etiqueta(), ex));
                }
                entradas[i] = combo;
            } else {
                entradas[i] = new JTextField();
            }
            campos.add(new JLabel(campo.etiqueta() + (campo.obligatorio() ? " *" : "")));
            campos.add(entradas[i]);
        }
        contenedor.add(campos, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        JButton limpiar = new JButton("Nuevo / Limpiar");
        limpiar.addActionListener(e -> limpiar());
        JButton guardar = new JButton("Guardar");
        guardar.addActionListener(e -> guardar());
        JButton modificar = new JButton("Actualizar registro");
        modificar.addActionListener(e -> actualizarSeleccionado());
        JButton eliminar = new JButton("Eliminar");
        eliminar.addActionListener(e -> eliminarFisico());
        JButton actualizar = new JButton("Recargar lista");
        actualizar.addActionListener(e -> recargar());
        acciones.add(limpiar); acciones.add(guardar); acciones.add(modificar);
        acciones.add(eliminar); acciones.add(actualizar);
        contenedor.add(acciones, BorderLayout.SOUTH);
        return contenedor;
    }

    private void recargar() {
        try {
            modelo.setRowCount(0);
            List<Object[]> filas = dao.listar(config, filtroColumna, filtroValor);
            for (Object[] fila : filas) modelo.addRow(fila);
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudo cargar " + config.titulo(), ex));
        }
    }

    private void establecerValoresIniciales() {
        String hoy = LocalDate.now().toString();
        for (int i = 0; i < config.campos().length; i++) {
            String columna = config.campos()[i].columna();
            if (columna.startsWith("FECHA_")) establecerEntrada(i, hoy);
            if (columna.equals("DESCUENTO") || columna.equals("TOTAL")
                    || columna.equals("MONTO_PRESUPUESTADO")) establecerEntrada(i, "0");
        }
    }

    private void seleccionarFila() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) return;
        int fila = tabla.convertRowIndexToModel(filaVista);
        idSeleccionado = ((Number) modelo.getValueAt(fila, 0)).longValue();
        if (alSeleccionar != null) alSeleccionar.accept(idSeleccionado);
        Object[] datos = new Object[config.campos().length];
        for (int i = 0; i < datos.length; i++) {
            datos[i] = modelo.getValueAt(fila, i + 1);
            establecerEntrada(i, datos[i]);
        }
    }

    public void alSeleccionar(LongConsumer listener) {
        this.alSeleccionar = listener;
    }

    public void establecerCampo(String columna, String valor) {
        for (int i = 0; i < config.campos().length; i++) {
            if (config.campos()[i].columna().equals(columna)) {
                establecerEntrada(i, valor == null ? null : valor);
                return;
            }
        }
    }

    public void filtrarPor(String columna, long valor) {
        filtroColumna = columna;
        filtroValor = valor;
        recargar();
    }

    @SuppressWarnings("unchecked")
    public void refrescarListas() {
        for (int i = 0; i < config.campos().length; i++) {
            if (!(entradas[i] instanceof JComboBox<?> combo)) continue;
            Object seleccionado = combo.getSelectedItem();
            combo.removeAllItems();
            try {
                for (CrudDAO.Opcion opcion : dao.listarOpciones(
                        config.campos()[i].tablaReferencia(),
                        config.campos()[i].columna(),
                        config.campos()[i].columnaEtiqueta())) {
                    ((JComboBox<CrudDAO.Opcion>) combo).addItem(opcion);
                }
                if (seleccionado != null) combo.setSelectedItem(seleccionado);
            } catch (SQLException ex) {
                error(MensajesSQL.describir("No se pudo actualizar la lista de "
                        + config.campos()[i].etiqueta(), ex));
            }
        }
    }

    private void aplicarBusqueda() {
        String texto = busqueda.getText().trim();
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
        sorter.setRowFilter(texto.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }

    private void guardar() {
        try {
            Object[] valores = convertirEntradas();
            if (idSeleccionado == 0) dao.insertar(config, valores);
            else dao.actualizar(config, idSeleccionado, valores);
            limpiar();
            recargar();
        } catch (IllegalArgumentException | SQLException ex) {
            error(ex instanceof SQLException sql
                    ? MensajesSQL.describir("No se pudo guardar", sql)
                    : ex.getMessage());
        }
    }

    private void actualizarSeleccionado() {
        if (idSeleccionado == 0) {
            error("Seleccione un registro para actualizar.");
            return;
        }
        guardar();
    }

    private Object[] convertirEntradas() {
        Object[] valores = new Object[entradas.length];
        for (int i = 0; i < entradas.length; i++) {
            CrudConfig.Campo campo = config.campos()[i];
            if (entradas[i] instanceof JComboBox<?> combo) {
                Object seleccionado = combo.getSelectedItem();
                if (seleccionado == null && campo.obligatorio()) {
                    throw new IllegalArgumentException("El campo " + campo.etiqueta() + " es obligatorio.");
                }
                valores[i] = seleccionado == null ? null : ((CrudDAO.Opcion) seleccionado).id();
                continue;
            }
            String texto = ((JTextField) entradas[i]).getText().trim();
            if (campo.obligatorio() && texto.isEmpty()) {
                throw new IllegalArgumentException("El campo " + campo.etiqueta() + " es obligatorio.");
            }
            if (texto.isEmpty()) { valores[i] = null; continue; }
            try {
                valores[i] = switch (campo.tipo()) {
                    case NUMERO -> Long.valueOf(texto);
                    case DECIMAL -> new BigDecimal(texto);
                    case FECHA -> Date.valueOf(texto);
                    default -> texto;
                };
                if (valores[i] instanceof Number numero && numero.doubleValue() < 0) {
                    throw new IllegalArgumentException(campo.etiqueta() + " no puede ser negativo.");
                }
            } catch (IllegalArgumentException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("no puede ser negativo")) {
                    throw ex;
                }
                throw new IllegalArgumentException(campo.etiqueta() + " tiene un formato invalido.");
            }
        }
        return valores;
    }

    private void eliminarFisico() {
        if (idSeleccionado == 0) { error("Seleccione un registro primero."); return; }
        if (!confirmar("eliminar definitivamente")) return;
        try {
            dao.eliminarFisico(config, idSeleccionado);
            limpiar();
            recargar();
        } catch (SQLException ex) {
            error(MensajesSQL.describir("No se pudo eliminar", ex));
        }
    }

    private boolean confirmar(String accion) {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Desea " + accion + " el registro seleccionado?",
                "Confirmar accion", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return respuesta == JOptionPane.YES_OPTION;
    }

    private void limpiar() {
        idSeleccionado = 0;
        tabla.clearSelection();
        for (JComponent entrada : entradas) {
            if (entrada instanceof JTextField campo) campo.setText("");
            if (entrada instanceof JComboBox<?> combo) combo.setSelectedItem(null);
        }
        establecerValoresIniciales();
    }

    private void establecerEntrada(int indice, Object valor) {
        if (entradas[indice] instanceof JTextField campo) {
            campo.setText(valor == null ? "" : String.valueOf(valor));
        } else if (entradas[indice] instanceof JComboBox<?> combo) {
            long id = valor instanceof Number ? ((Number) valor).longValue()
                    : Long.parseLong(String.valueOf(valor));
            for (int j = 0; j < combo.getItemCount(); j++) {
                CrudDAO.Opcion opcion = (CrudDAO.Opcion) combo.getItemAt(j);
                if (opcion.id() == id) {
                    combo.setSelectedIndex(j);
                    break;
                }
            }
        }
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Proyecto03", JOptionPane.ERROR_MESSAGE);
    }
}
