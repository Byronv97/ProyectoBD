package vistas;

import clases.Modulos;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Component;

/** Ventana de trabajo que agrupa las tablas pertenecientes a un utilitario. */
public class UtilitarioFrame extends JFrame {

    public enum Tipo {
        VENTAS, COMPRAS, INVENTARIO, EMPLEADOS
    }

    public UtilitarioFrame(Tipo tipo) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1250, 760);
        setLocationRelativeTo(null);
        construir(tipo);
    }

    private void construir(Tipo tipo) {
        setTitle("Proyecto03 | " + titulo(tipo));
        setLayout(new BorderLayout());
        add(encabezado(tipo), BorderLayout.NORTH);

        JTabbedPane pestañas = new JTabbedPane();
        switch (tipo) {
            case VENTAS -> {
                pestañas.addTab("Clientes", new ClientesPanel());
                pestañas.addTab("Ventas", new VentaFormularioPanel());
            }
            case COMPRAS -> {
                pestañas.addTab("Proveedores", new CrudPanel(Modulos.proveedores()));
                pestañas.addTab("Compras", new CompraFormularioPanel());
            }
            case INVENTARIO -> {
                pestañas.addTab("Productos", new CrudPanel(Modulos.productos()));
                pestañas.addTab("Movimientos", new MovimientoInventarioPanel());
            }
            case EMPLEADOS -> {
                pestañas.addTab("Departamentos", new CrudPanel(Modulos.departamentos()));
                pestañas.addTab("Empleados", new CrudPanel(Modulos.empleados()));
            }
        }
        pestañas.addTab("Consultas", new ConsultasPanel(tipo));
        pestañas.addChangeListener(e -> refrescarFormularioSeleccionado(pestañas));
        add(pestañas, BorderLayout.CENTER);
    }

    private void refrescarFormularioSeleccionado(JTabbedPane pestañas) {
        Component seleccionado = pestañas.getSelectedComponent();
        if (seleccionado instanceof VentaFormularioPanel ventas) ventas.refrescarListas();
        if (seleccionado instanceof CompraFormularioPanel compras) compras.refrescarListas();
        if (seleccionado instanceof CrudPanel crud) crud.refrescarListas();
    }

    private JPanel encabezado(Tipo tipo) {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(color(tipo));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        JLabel titulo = new JLabel(titulo(tipo));
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(titulo, BorderLayout.WEST);
        JButton cerrar = new JButton("Cerrar ventana");
        cerrar.addActionListener(e -> dispose());
        panel.add(cerrar, BorderLayout.EAST);
        return panel;
    }

    private String titulo(Tipo tipo) {
        return switch (tipo) {
            case VENTAS -> "Utilitario de gestion de ventas";
            case COMPRAS -> "Utilitario de gestion de compras";
            case INVENTARIO -> "Utilitario de gestion de inventario";
            case EMPLEADOS -> "Utilitario de gestion de empleados";
        };
    }

    private Color color(Tipo tipo) {
        return switch (tipo) {
            case VENTAS -> new Color(169, 58, 66);
            case COMPRAS -> new Color(45, 119, 171);
            case INVENTARIO -> new Color(47, 129, 80);
            case EMPLEADOS -> new Color(153, 116, 25);
        };
    }
}
