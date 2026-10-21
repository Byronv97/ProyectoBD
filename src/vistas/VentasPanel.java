package vistas;

import clases.Modulos;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JPanel;

/** Gestiona una venta y sus detalles en la misma pantalla. */
public class VentasPanel extends JPanel {

    public VentasPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        CrudPanel ventas = new CrudPanel(Modulos.ventas());
        CrudPanel detalles = new CrudPanel(Modulos.detalleVentas());
        ventas.alSeleccionar(idVenta -> {
            detalles.establecerCampo("ID_VENTA", String.valueOf(idVenta));
            detalles.filtrarPor("ID_VENTA", idVenta);
        });

        JSplitPane separador = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ventas, detalles);
        separador.setResizeWeight(0.53);
        separador.setDividerLocation(0.53);
        separador.setBorder(null);
        ventas.setMinimumSize(new Dimension(500, 280));
        detalles.setMinimumSize(new Dimension(500, 280));
        add(separador, BorderLayout.CENTER);
    }
}
