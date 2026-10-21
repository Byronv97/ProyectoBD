package vistas;

import java.util.regex.Pattern;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

/** Configura una busqueda textual sobre todas las columnas visibles de una tabla. */
public final class FiltroTabla {

    private FiltroTabla() { }

    public static void configurar(JTable tabla, JTextField campo) {
        tabla.setAutoCreateRowSorter(true);
        campo.setToolTipText("Buscar en todos los campos visibles");
        campo.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicar(tabla, campo); }
            public void removeUpdate(DocumentEvent e) { aplicar(tabla, campo); }
            public void changedUpdate(DocumentEvent e) { aplicar(tabla, campo); }
        });
    }

    private static void aplicar(JTable tabla, JTextField campo) {
        String texto = campo.getText().trim();
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
        sorter.setRowFilter(texto.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }
}
