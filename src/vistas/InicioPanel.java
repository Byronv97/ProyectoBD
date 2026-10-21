package vistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class InicioPanel extends JPanel {

    public InicioPanel(Runnable abrirEmpleados, Runnable abrirInventario,
            Runnable abrirVentas, Runnable abrirCompras) {
        setLayout(new BorderLayout(24, 24));
        setBorder(BorderFactory.createEmptyBorder(38, 42, 38, 42));
        setBackground(new Color(247, 249, 252));

        JLabel titulo = new JLabel("Panel principal");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 32));
        titulo.setForeground(new Color(28, 40, 58));
        add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(2, 2, 18, 18));
        centro.setOpaque(false);
        centro.add(tarjeta("Gestion de empleados", "Departamentos y empleados", abrirEmpleados));
        centro.add(tarjeta("Gestion de inventario", "Productos, stock y movimientos", abrirInventario));
        centro.add(tarjeta("Gestion de ventas", "Clientes y ventas", abrirVentas));
        centro.add(tarjeta("Gestion de compras", "Proveedores y compras", abrirCompras));
        add(centro, BorderLayout.CENTER);
    }

    private JPanel tarjeta(String titulo, String detalle, Runnable accion) {
        JPanel panel = new JPanel(new BorderLayout(8, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 230, 238)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        JLabel encabezado = new JLabel(titulo);
        encabezado.setFont(new Font("SansSerif", Font.BOLD, 20));
        JLabel descripcion = new JLabel("<html>" + detalle + "</html>");
        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(descripcion, BorderLayout.CENTER);
        if (accion != null) {
            JButton boton = new JButton("Abrir modulo");
            boton.addActionListener(e -> accion.run());
            panel.add(boton, BorderLayout.SOUTH);
        }
        return panel;
    }
}
