package vistas;

import clases.CrudConfig;
import clases.CrudDAO;
import clases.MensajesSQL;
import clases.Modulos;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Dialogo compacto para crear productos desde ventas o compras. */
public class ProductoDialog extends JDialog {

    private final CrudDAO dao = new CrudDAO();
    private final JTextField codigo = new JTextField();
    private final JTextField nombre = new JTextField();
    private final JTextField descripcion = new JTextField();
    private final JTextField precio = new JTextField();
    private final JTextField costo = new JTextField("0");
    private final JTextField stock = new JTextField("0");
    private final Runnable despuesDeGuardar;

    public ProductoDialog(java.awt.Window propietario, Runnable despuesDeGuardar) {
        super(propietario, "Nuevo producto", Dialog.ModalityType.APPLICATION_MODAL);
        this.despuesDeGuardar = despuesDeGuardar;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(430, 330);
        setLocationRelativeTo(propietario);
        construir();
    }

    private void construir() {
        setLayout(new BorderLayout(10, 10));
        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 8));
        campos.setBorder(BorderFactory.createEmptyBorder(14, 14, 4, 14));
        agregar(campos, "Codigo *", codigo);
        agregar(campos, "Nombre *", nombre);
        agregar(campos, "Descripcion", descripcion);
        agregar(campos, "Precio venta *", precio);
        agregar(campos, "Costo referencia *", costo);
        agregar(campos, "Stock inicial *", stock);
        add(campos, BorderLayout.CENTER);
        JPanel acciones = new JPanel();
        JButton cancelar = new JButton("Cancelar");
        cancelar.addActionListener(e -> dispose());
        JButton guardar = new JButton("Guardar producto");
        guardar.addActionListener(e -> guardar());
        acciones.add(cancelar); acciones.add(guardar);
        add(acciones, BorderLayout.SOUTH);
    }

    private void agregar(JPanel panel, String etiqueta, JTextField campo) {
        panel.add(new JLabel(etiqueta));
        panel.add(campo);
    }

    private void guardar() {
        if (codigo.getText().isBlank() || nombre.getText().isBlank() || precio.getText().isBlank()) {
            error("Codigo, nombre y precio son obligatorios."); return;
        }
        try {
            BigDecimal precioValor = decimal(precio, "Precio de venta");
            BigDecimal costoValor = decimal(costo, "Costo de referencia");
            BigDecimal stockValor = decimal(stock, "Stock inicial");
            if (precioValor.signum() < 0 || costoValor.signum() < 0 || stockValor.signum() < 0) {
                throw new IllegalArgumentException("Precio, costo y stock no pueden ser negativos.");
            }
            Object[] valores = {codigo.getText().trim(), nombre.getText().trim(),
                descripcion.getText().trim(), precioValor, costoValor, stockValor};
            CrudConfig config = Modulos.productos();
            dao.insertar(config, valores);
            if (despuesDeGuardar != null) despuesDeGuardar.run();
            dispose();
        } catch (IllegalArgumentException | java.sql.SQLException ex) {
            error(ex instanceof java.sql.SQLException sql
                    ? MensajesSQL.describir("No se pudo guardar el producto", sql)
                    : "No se pudo guardar el producto:\n" + ex.getMessage());
        }
    }

    private BigDecimal decimal(JTextField campo, String nombre) {
        try { return new BigDecimal(campo.getText().trim()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException(nombre + " invalido."); }
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Proyecto03", JOptionPane.ERROR_MESSAGE);
    }
}
