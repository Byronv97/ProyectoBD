package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import vistas.PrincipalFrame;

public final class Aplicacion {

    private Aplicacion() {
    }

    public static void main(String[] args) {
        iniciar();
    }

    public static void iniciar() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Se utiliza el estilo Swing por defecto si el sistema no lo permite.
            }
            new PrincipalFrame().setVisible(true);
        });
    }
}
