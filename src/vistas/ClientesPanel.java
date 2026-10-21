package vistas;

import clases.Cliente;
import clases.ClienteDAO;
import clases.MensajesSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.util.regex.Pattern;

/** Pantalla CRUD inicial de clientes. */
public class ClientesPanel extends JPanel {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombres", "Apellidos", "Telefono", "Email", "Direccion"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField nombres = new JTextField(15);
    private final JTextField apellidos = new JTextField(15);
    private final JTextField telefono = new JTextField(15);
    private final JTextField email = new JTextField(15);
    private final JTextField direccion = new JTextField(15);
    private final JTextField busqueda = new JTextField(18);

    public ClientesPanel() {
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        setBackground(new Color(247, 249, 252));
        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.add(crearEncabezado(), BorderLayout.NORTH);
        superior.add(crearFormulario(), BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
        busqueda.setToolTipText("Buscar en todos los campos visibles");
        busqueda.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarBusqueda(); }
            public void removeUpdate(DocumentEvent e) { aplicarBusqueda(); }
            public void changedUpdate(DocumentEvent e) { aplicarBusqueda(); }
        });
        recargar();
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel titulo = new JLabel("Clientes");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        panel.add(titulo, BorderLayout.WEST);
        JButton recargar = new JButton("Recargar lista");
        recargar.addActionListener(e -> recargar());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(new JLabel("Buscar:"));
        acciones.add(busqueda);
        acciones.add(recargar);
        panel.add(acciones, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearFormulario() {
        JPanel envoltura = new JPanel(new BorderLayout(12, 8));
        envoltura.setBackground(Color.WHITE);
        envoltura.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 230, 238)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        agregarCampo(campos, "Nombres *", nombres, 0, 0);
        agregarCampo(campos, "Apellidos *", apellidos, 2, 0);
        agregarCampo(campos, "Telefono", telefono, 0, 1);
        agregarCampo(campos, "Email", email, 2, 1);
        agregarCampo(campos, "Direccion", direccion, 0, 2);
        envoltura.add(campos, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        JButton nuevo = new JButton("Limpiar");
        nuevo.addActionListener(e -> limpiar());
        JButton guardar = new JButton("Guardar");
        guardar.addActionListener(e -> guardar());
        JButton actualizar = new JButton("Actualizar cliente");
        actualizar.addActionListener(e -> actualizarSeleccionado());
        JButton eliminar = new JButton("Eliminar");
        eliminar.addActionListener(e -> eliminar());
        acciones.add(nuevo);
        acciones.add(guardar);
        acciones.add(actualizar);
        acciones.add(eliminar);
        envoltura.add(acciones, BorderLayout.SOUTH);
        return envoltura;
    }

    private void agregarCampo(JPanel panel, String etiqueta, JTextField campo, int columna, int fila) {
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(3, 6, 3, 6);
        restricciones.anchor = GridBagConstraints.WEST;
        restricciones.gridx = columna;
        restricciones.gridy = fila;
        panel.add(new JLabel(etiqueta), restricciones);
        restricciones.gridx = columna + 1;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.weightx = 1;
        panel.add(campo, restricciones);
    }

    private void recargar() {
        try {
            List<Cliente> clientes = clienteDAO.listar();
            modeloTabla.setRowCount(0);
            for (Cliente cliente : clientes) {
                modeloTabla.addRow(new Object[]{cliente.getIdCliente(), cliente.getNombres(),
                    cliente.getApellidos(), cliente.getTelefono(), cliente.getEmail(), cliente.getDireccion()});
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo consultar CLIENTE. Verifique el driver Oracle y la conexion.\n"
                    + MensajesSQL.describir("Detalle", ex));
        }
    }

    private void guardar() {
        if (nombres.getText().isBlank() || apellidos.getText().isBlank()) {
            mostrarError("Nombres y apellidos son obligatorios.");
            return;
        }
        try {
            Cliente cliente = new Cliente(nombres.getText().trim(), apellidos.getText().trim(),
                    telefono.getText(), email.getText(), direccion.getText());
            int filaVista = tabla.getSelectedRow();
            if (filaVista >= 0) {
                int fila = tabla.convertRowIndexToModel(filaVista);
                cliente.setIdCliente(((Number) modeloTabla.getValueAt(fila, 0)).longValue());
                clienteDAO.actualizar(cliente);
            } else {
                clienteDAO.insertar(cliente);
            }
            limpiar();
            recargar();
        } catch (SQLException ex) {
            mostrarError(MensajesSQL.describir("No se pudo guardar el cliente", ex));
        }
    }

    private void eliminar() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarError("Seleccione un cliente.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "Desea eliminar definitivamente el cliente seleccionado?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) return;
        int fila = tabla.convertRowIndexToModel(filaVista);
        long id = ((Number) modeloTabla.getValueAt(fila, 0)).longValue();
        try {
            clienteDAO.eliminarFisico(id);
            limpiar();
            recargar();
        } catch (SQLException ex) {
            mostrarError(MensajesSQL.describir(
                    "No se pudo eliminar el cliente. Puede tener ventas relacionadas", ex));
        }
    }

    private void actualizarSeleccionado() {
        if (tabla.getSelectedRow() < 0) {
            mostrarError("Seleccione un cliente para actualizar.");
            return;
        }
        guardar();
    }

    private void cargarSeleccion() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista >= 0) {
            int fila = tabla.convertRowIndexToModel(filaVista);
            nombres.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
            apellidos.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
            telefono.setText(valorTexto(fila, 3));
            email.setText(valorTexto(fila, 4));
            direccion.setText(valorTexto(fila, 5));
        }
    }

    private String valorTexto(int fila, int columna) {
        Object valor = modeloTabla.getValueAt(fila, columna);
        return valor == null ? "" : String.valueOf(valor);
    }

    private void limpiar() {
        tabla.clearSelection();
        nombres.setText("");
        apellidos.setText("");
        telefono.setText("");
        email.setText("");
        direccion.setText("");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Proyecto03", JOptionPane.ERROR_MESSAGE);
    }

    private void aplicarBusqueda() {
        String texto = busqueda.getText().trim();
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
        sorter.setRowFilter(texto.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }
}
