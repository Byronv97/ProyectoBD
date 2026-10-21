package vistas;

import clases.Modulos;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JPanel;

/** Gestiona una compra y sus productos en la misma pantalla. */
public class ComprasPanel extends JPanel {

    public ComprasPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        CrudPanel compras = new CrudPanel(Modulos.compras());
        CrudPanel detalles = new CrudPanel(Modulos.detalleCompras());
        compras.alSeleccionar(idCompra -> {
            detalles.establecerCampo("ID_COMPRA", String.valueOf(idCompra));
            detalles.filtrarPor("ID_COMPRA", idCompra);
        });

        JSplitPane separador = new JSplitPane(JSplitPane.VERTICAL_SPLIT, compras, detalles);
        separador.setResizeWeight(0.53);
        separador.setDividerLocation(0.53);
        separador.setBorder(null);
        compras.setMinimumSize(new Dimension(500, 280));
        detalles.setMinimumSize(new Dimension(500, 280));
        add(separador, BorderLayout.CENTER);
    }
}
