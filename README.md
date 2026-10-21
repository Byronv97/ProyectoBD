# Proyecto03

Aplicacion de escritorio Java Swing para Oracle, organizada en los paquetes:

- `conexion`: conexion JDBC a Oracle.
- `main`: punto de entrada.
- `clases`: modelos y acceso a datos inicial.
- `vistas`: ventanas y pantallas Swing.

## Ejecutar en NetBeans

1. El proyecto ya incluye `lib/ojdbc11.jar` y lo agrega automáticamente al classpath.
   Si NetBeans no lo muestra, clic derecho en `Libraries` > `Add JAR/Folder` y selecciona ese archivo.
2. Verificar que Oracle este iniciado y que exista el servicio `FREEPDB1` en el puerto `1521`.
3. Ejecutar la clase `proyecto2.Proyecto2`.

La conexion usa por defecto:

```text
jdbc:oracle:thin:@//localhost:1521/FREEPDB1
usuario: Proyecto03
password: se proporciona mediante `-Ddb.password` o `DB_PASSWORD`
```

Para cambiar la configuracion sin modificar codigo, ejecutar la JVM con estas propiedades:

```text
-Ddb.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
-Ddb.user=Proyecto03
-Ddb.password=tu_password
```

Tambien puede configurarse la variable de entorno `DB_PASSWORD`.

## Modulos disponibles

La pantalla principal abre cuatro utilitarios. Cada utilitario es una sola ventana
con pestañas para sus tablas relacionadas y permite crear, editar, consultar y
eliminar:

- **Gestion de ventas:** Clientes y Ventas.
- **Gestion de compras:** Proveedores y Compras.
- **Gestion de inventario:** Productos y movimientos de inventario.
- **Gestion de empleados:** Departamentos y Empleados.

La nueva estructura incluye `PRODUCTO`, con codigo, precio, costo y stock, y
`MOVIMIENTO_INVENTARIO` para consultar entradas y salidas.

La base de datos no contiene triggers ni procesos de negocio. Solo mantiene
tablas, relaciones, restricciones de integridad y valores persistidos. La
seleccion de registros, calculo de totales, validacion de stock y actualizacion
de inventario deben ejecutarse desde la aplicacion Java.

Las ventas y compras se registran en una sola pantalla: se seleccionan cliente o
proveedor, empleado y producto, se ingresa cantidad y descuento, y Java crea la
cabecera y su detalle en una transaccion. El precio/costo se toma del producto y
el total se calcula en Java.

El inventario se actualiza desde Java al registrar compras y ventas, y los
movimientos quedan disponibles para consulta.
Las fechas deben escribirse como `yyyy-mm-dd` cuando se edita un registro.
