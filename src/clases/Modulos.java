package clases;

import static clases.CrudConfig.Tipo.DECIMAL;
import static clases.CrudConfig.Tipo.FECHA;
import static clases.CrudConfig.Tipo.NUMERO;
import static clases.CrudConfig.Tipo.TEXTO;

/** Catalogo central de las pantallas CRUD de la aplicacion. */
public final class Modulos {

    private Modulos() { }

    public static CrudConfig proveedores() {
        return new CrudConfig("Proveedores", "PROVEEDOR", "ID_PROVEEDOR",
                new String[]{"ID_PROVEEDOR", "RAZON_SOCIAL", "TELEFONO", "EMAIL", "DIRECCION"},
                new String[]{"ID", "Razon social", "Telefono", "Email", "Direccion"},
                new CrudConfig.Campo[]{
                    new CrudConfig.Campo("RAZON_SOCIAL", "Razon social", TEXTO, true),
                    new CrudConfig.Campo("TELEFONO", "Telefono", TEXTO, false),
                    new CrudConfig.Campo("EMAIL", "Email", TEXTO, false),
                    new CrudConfig.Campo("DIRECCION", "Direccion", TEXTO, false)});
    }

    public static CrudConfig departamentos() {
        return new CrudConfig("Departamentos", "DEPARTAMENTO", "ID_DEPARTAMENTO",
                new String[]{"ID_DEPARTAMENTO", "NOMBRE", "DESCRIPCION"},
                new String[]{"ID", "Nombre", "Descripcion"},
                new CrudConfig.Campo[]{
                    new CrudConfig.Campo("NOMBRE", "Nombre", TEXTO, true),
                    new CrudConfig.Campo("DESCRIPCION", "Descripcion", TEXTO, false)});
    }

    public static CrudConfig productos() {
        return new CrudConfig("Productos", "PRODUCTO", "ID_PRODUCTO",
                new String[]{"ID_PRODUCTO", "CODIGO", "NOMBRE", "DESCRIPCION", "PRECIO_VENTA", "COSTO_REFERENCIA", "STOCK"},
                new String[]{"ID", "Codigo", "Nombre", "Descripcion", "Precio venta", "Costo", "Stock"},
                new CrudConfig.Campo[]{
                    new CrudConfig.Campo("CODIGO", "Codigo", TEXTO, true),
                    new CrudConfig.Campo("NOMBRE", "Nombre", TEXTO, true),
                    new CrudConfig.Campo("DESCRIPCION", "Descripcion", TEXTO, false),
                    new CrudConfig.Campo("PRECIO_VENTA", "Precio venta", DECIMAL, true),
                    new CrudConfig.Campo("COSTO_REFERENCIA", "Costo referencia", DECIMAL, true),
                    new CrudConfig.Campo("STOCK", "Stock", DECIMAL, true)});
    }

    public static CrudConfig empleados() {
        return new CrudConfig("Empleados", "EMPLEADO", "ID_EMPLEADO",
                new String[]{"ID_EMPLEADO", "ID_DEPARTAMENTO", "NOMBRES", "APELLIDOS", "CARGO", "EMAIL", "TELEFONO", "FECHA_INGRESO", "SALARIO"},
                new String[]{"ID", "Depto.", "Nombres", "Apellidos", "Cargo", "Email", "Telefono", "Ingreso", "Salario"},
                new CrudConfig.Campo[]{
                    new CrudConfig.Campo("ID_DEPARTAMENTO", "Departamento", NUMERO, true,
                            "DEPARTAMENTO", "NOMBRE"),
                    new CrudConfig.Campo("NOMBRES", "Nombres", TEXTO, true),
                    new CrudConfig.Campo("APELLIDOS", "Apellidos", TEXTO, true),
                    new CrudConfig.Campo("CARGO", "Cargo", TEXTO, true),
                    new CrudConfig.Campo("EMAIL", "Email", TEXTO, false),
                    new CrudConfig.Campo("TELEFONO", "Telefono", TEXTO, false),
                    new CrudConfig.Campo("FECHA_INGRESO", "Ingreso (yyyy-mm-dd)", FECHA, true),
                    new CrudConfig.Campo("SALARIO", "Salario", DECIMAL, false)});
    }

    public static CrudConfig ventas() {
        return new CrudConfig("Ventas", "VENTA", "ID_VENTA",
                new String[]{"ID_VENTA", "ID_CLIENTE", "ID_EMPLEADO", "FECHA_VENTA", "TOTAL"},
                new String[]{"ID", "Cliente", "Empleado", "Fecha", "Total"},
                new CrudConfig.Campo[]{
                    new CrudConfig.Campo("ID_CLIENTE", "Cliente", NUMERO, true,
                            "CLIENTE", "NOMBRES"),
                    new CrudConfig.Campo("ID_EMPLEADO", "Empleado", NUMERO, true,
                            "EMPLEADO", "NOMBRES"),
                    new CrudConfig.Campo("FECHA_VENTA", "Fecha (yyyy-mm-dd)", FECHA, true),
                    new CrudConfig.Campo("TOTAL", "Total", DECIMAL, true)});
    }

    public static CrudConfig detalleVentas() {
        return new CrudConfig("Detalle de ventas", "DETALLE_VENTA", "ID_DETALLE_VENTA",
                new String[]{"ID_DETALLE_VENTA", "ID_VENTA", "ID_PRODUCTO", "CANTIDAD", "PRECIO_UNITARIO", "DESCUENTO"},
                new String[]{"ID", "Venta", "Producto", "Cantidad", "Precio", "Descuento"},
                new CrudConfig.Campo[]{
                    new CrudConfig.Campo("ID_VENTA", "ID venta", NUMERO, true),
                    new CrudConfig.Campo("ID_PRODUCTO", "Producto", NUMERO, true,
                            "PRODUCTO", "NOMBRE"),
                    new CrudConfig.Campo("CANTIDAD", "Cantidad", DECIMAL, true),
                    new CrudConfig.Campo("PRECIO_UNITARIO", "Precio", DECIMAL, true),
                    new CrudConfig.Campo("DESCUENTO", "Descuento", DECIMAL, true)});
    }

    public static CrudConfig compras() {
        return new CrudConfig("Compras", "COMPRA", "ID_COMPRA",
                new String[]{"ID_COMPRA", "ID_PROVEEDOR", "ID_EMPLEADO", "NUMERO_FACTURA", "FECHA_COMPRA", "TOTAL"},
                new String[]{"ID", "Proveedor", "Empleado", "Factura", "Fecha", "Total"},
                new CrudConfig.Campo[]{
                    new CrudConfig.Campo("ID_PROVEEDOR", "Proveedor", NUMERO, true,
                            "PROVEEDOR", "RAZON_SOCIAL"),
                    new CrudConfig.Campo("ID_EMPLEADO", "Empleado", NUMERO, true,
                            "EMPLEADO", "NOMBRES"),
                    new CrudConfig.Campo("NUMERO_FACTURA", "Factura", TEXTO, true),
                    new CrudConfig.Campo("FECHA_COMPRA", "Fecha (yyyy-mm-dd)", FECHA, true),
                    new CrudConfig.Campo("TOTAL", "Total", DECIMAL, true)});
    }

    public static CrudConfig detalleCompras() {
        return new CrudConfig("Detalle de compras", "DETALLE_COMPRA", "ID_DETALLE_COMPRA",
                new String[]{"ID_DETALLE_COMPRA", "ID_COMPRA", "ID_PRODUCTO", "CANTIDAD", "COSTO_UNITARIO", "DESCUENTO"},
                new String[]{"ID", "Compra", "Producto", "Cantidad", "Costo", "Descuento"},
                new CrudConfig.Campo[]{
                    new CrudConfig.Campo("ID_COMPRA", "ID compra", NUMERO, true),
                    new CrudConfig.Campo("ID_PRODUCTO", "Producto", NUMERO, true,
                            "PRODUCTO", "NOMBRE"),
                    new CrudConfig.Campo("CANTIDAD", "Cantidad", DECIMAL, true),
                    new CrudConfig.Campo("COSTO_UNITARIO", "Costo", DECIMAL, true),
                    new CrudConfig.Campo("DESCUENTO", "Descuento", DECIMAL, true)});
    }
}
