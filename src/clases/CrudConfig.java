package clases;

/** Configuracion interna de una pantalla CRUD. No recibe nombres SQL desde el usuario. */
public final class CrudConfig {

    public enum Tipo { TEXTO, NUMERO, DECIMAL, FECHA }

    public record Campo(String columna, String etiqueta, Tipo tipo, boolean obligatorio,
            String tablaReferencia, String columnaEtiqueta) {
        public Campo(String columna, String etiqueta, Tipo tipo, boolean obligatorio) {
            this(columna, etiqueta, tipo, obligatorio, null, null);
        }

        public boolean esRelacion() {
            return tablaReferencia != null && columnaEtiqueta != null;
        }
    }

    private final String titulo;
    private final String tabla;
    private final String id;
    private final String[] columnas;
    private final String[] encabezados;
    private final Campo[] campos;

    public CrudConfig(String titulo, String tabla, String id, String[] columnas,
            String[] encabezados, Campo[] campos) {
        this.titulo = titulo;
        this.tabla = tabla;
        this.id = id;
        this.columnas = columnas;
        this.encabezados = encabezados;
        this.campos = campos;
    }

    public String titulo() { return titulo; }
    public String tabla() { return tabla; }
    public String id() { return id; }
    public String[] columnas() { return columnas; }
    public String[] encabezados() { return encabezados; }
    public Campo[] campos() { return campos; }
}
