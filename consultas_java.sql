-- Consultas SELECT utilizadas por la aplicacion Java.
-- Ejecutar conectado como Proyecto03 en FREEPDB1.
-- Las consultas con :ID_* representan parametros PreparedStatement.

-- =============================================================
-- CLIENTES - ClienteDAO.java
-- =============================================================
SELECT ID_CLIENTE, NOMBRES, APELLIDOS,
       TELEFONO, EMAIL, DIRECCION, FECHA_REGISTRO
FROM CLIENTE
ORDER BY ID_CLIENTE;

-- =============================================================
-- OPCIONES PARA COMBOS - CrudDAO.listarOpciones(...)
-- =============================================================
SELECT ID_CLIENTE, NOMBRES
FROM CLIENTE
ORDER BY NOMBRES;

SELECT ID_PROVEEDOR, RAZON_SOCIAL
FROM PROVEEDOR
ORDER BY RAZON_SOCIAL;

SELECT ID_DEPARTAMENTO, NOMBRE
FROM DEPARTAMENTO
ORDER BY NOMBRE;

SELECT ID_EMPLEADO, NOMBRES
FROM EMPLEADO
ORDER BY NOMBRES;

-- =============================================================
-- PRODUCTOS PARA VENTAS Y COMPRAS - CrudDAO.listarProductos()
-- =============================================================
SELECT ID_PRODUCTO, NOMBRE, PRECIO_VENTA, STOCK
FROM PRODUCTO
ORDER BY NOMBRE;

-- =============================================================
-- LISTADOS CRUD - CrudDAO.listar(...)
-- =============================================================
SELECT ID_DEPARTAMENTO, NOMBRE, DESCRIPCION
FROM DEPARTAMENTO
ORDER BY ID_DEPARTAMENTO;

SELECT ID_EMPLEADO, ID_DEPARTAMENTO, NOMBRES, APELLIDOS,
       CARGO, EMAIL, TELEFONO, FECHA_INGRESO, SALARIO
FROM EMPLEADO
ORDER BY ID_EMPLEADO;

SELECT ID_PROVEEDOR, RAZON_SOCIAL, TELEFONO, EMAIL, DIRECCION
FROM PROVEEDOR
ORDER BY ID_PROVEEDOR;

SELECT ID_PRODUCTO, CODIGO, NOMBRE, DESCRIPCION,
       PRECIO_VENTA, COSTO_REFERENCIA, STOCK
FROM PRODUCTO
ORDER BY ID_PRODUCTO;

-- =============================================================
-- VENTAS - VentaFormularioPanel.java
-- =============================================================
SELECT v.ID_VENTA,
       c.NOMBRES || ' ' || c.APELLIDOS AS CLIENTE,
       e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO,
       p.NOMBRE AS PRODUCTO,
       dv.CANTIDAD,
       v.FECHA_VENTA,
       v.TOTAL
FROM VENTA v
JOIN CLIENTE c ON c.ID_CLIENTE = v.ID_CLIENTE
JOIN EMPLEADO e ON e.ID_EMPLEADO = v.ID_EMPLEADO
JOIN DETALLE_VENTA dv ON dv.ID_VENTA = v.ID_VENTA
JOIN PRODUCTO p ON p.ID_PRODUCTO = dv.ID_PRODUCTO
ORDER BY v.ID_VENTA, dv.ID_DETALLE_VENTA;

-- Consulta el stock antes de registrar una venta.
-- FOR UPDATE bloquea el producto durante la transaccion.
SELECT STOCK
FROM PRODUCTO
WHERE ID_PRODUCTO = :ID_PRODUCTO
FOR UPDATE;

-- Productos y cantidades de una venta que se va a eliminar.
SELECT ID_PRODUCTO, CANTIDAD
FROM DETALLE_VENTA
WHERE ID_VENTA = :ID_VENTA;

-- =============================================================
-- COMPRAS - CompraFormularioPanel.java
-- =============================================================
SELECT co.ID_COMPRA,
       p.RAZON_SOCIAL AS PROVEEDOR,
       e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO,
       co.NUMERO_FACTURA,
       co.FECHA_COMPRA,
       co.TOTAL
FROM COMPRA co
JOIN PROVEEDOR p ON p.ID_PROVEEDOR = co.ID_PROVEEDOR
JOIN EMPLEADO e ON e.ID_EMPLEADO = co.ID_EMPLEADO
ORDER BY co.ID_COMPRA;

-- Productos y cantidades de una compra que se va a eliminar.
SELECT ID_PRODUCTO, CANTIDAD
FROM DETALLE_COMPRA
WHERE ID_COMPRA = :ID_COMPRA;

-- =============================================================
-- MOVIMIENTOS DE INVENTARIO - MovimientoInventarioPanel.java
-- =============================================================
SELECT m.ID_MOVIMIENTO,
       p.NOMBRE AS PRODUCTO,
       e.NOMBRES || ' ' || e.APELLIDOS AS EMPLEADO,
       m.TIPO_MOVIMIENTO,
       m.CANTIDAD,
       m.FECHA_MOVIMIENTO,
       CASE
           WHEN m.ID_COMPRA IS NOT NULL THEN 'COMPRA ' || m.ID_COMPRA
           WHEN m.ID_VENTA IS NOT NULL THEN 'VENTA ' || m.ID_VENTA
           ELSE 'AJUSTE'
       END AS ORIGEN,
       m.OBSERVACION
FROM MOVIMIENTO_INVENTARIO m
JOIN PRODUCTO p ON p.ID_PRODUCTO = m.ID_PRODUCTO
JOIN EMPLEADO e ON e.ID_EMPLEADO = m.ID_EMPLEADO
ORDER BY m.ID_MOVIMIENTO;
