package vistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Ventana contenedora. Cada módulo se muestra como una pantalla independiente. */
public class PrincipalFrame extends JFrame {

    public PrincipalFrame() {
        setTitle("Proyecto03 | Gestion administrativa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        construirInterfaz();
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout());
        add(crearMenu(), BorderLayout.WEST);
        add(new InicioPanel(
                () -> abrir(UtilitarioFrame.Tipo.EMPLEADOS),
                () -> abrir(UtilitarioFrame.Tipo.INVENTARIO),
                () -> abrir(UtilitarioFrame.Tipo.VENTAS),
                () -> abrir(UtilitarioFrame.Tipo.COMPRAS)), BorderLayout.CENTER);
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel(new BorderLayout(0, 20));
        menu.setPreferredSize(new Dimension(205, 0));
        menu.setBackground(new Color(28, 40, 58));
        menu.setBorder(BorderFactory.createEmptyBorder(24, 14, 18, 14));

        JLabel marca = new JLabel("PROYECTO03");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font("SansSerif", Font.BOLD, 20));
        menu.add(marca, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(boton("Gestion de empleados", () -> abrir(UtilitarioFrame.Tipo.EMPLEADOS)));
        botones.add(boton("Gestion de inventario", () -> abrir(UtilitarioFrame.Tipo.INVENTARIO)));
        botones.add(boton("Gestion de ventas", () -> abrir(UtilitarioFrame.Tipo.VENTAS)));
        botones.add(boton("Gestion de compras", () -> abrir(UtilitarioFrame.Tipo.COMPRAS)));
        menu.add(botones, BorderLayout.CENTER);

        JLabel conexion = new JLabel("Oracle | FREEPDB1");
        conexion.setForeground(new Color(184, 197, 214));
        menu.add(conexion, BorderLayout.SOUTH);
        return menu;
    }

    private JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setHorizontalAlignment(JButton.LEFT);
        boton.setForeground(new Color(28, 40, 58));
        boton.setBackground(new Color(235, 241, 248));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(128, 151, 177)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private void abrir(UtilitarioFrame.Tipo tipo) {
        new UtilitarioFrame(tipo).setVisible(true);
    }
}
