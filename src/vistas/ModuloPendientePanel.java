package vistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ModuloPendientePanel extends JPanel {

    public ModuloPendientePanel(String titulo, String detalle) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(45, 50, 45, 50));
        setBackground(new Color(247, 249, 252));
        JLabel contenido = new JLabel("<html><h1>" + titulo + "</h1><p>" + detalle
                + "</p><p>La pantalla ya esta separada y lista para implementar.</p></html>");
        contenido.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(contenido, BorderLayout.NORTH);
    }
}
