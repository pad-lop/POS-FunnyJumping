package com.example.posfunnyjumping;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnectionBackup2 {

    private static final String URL = "jdbc:sqlite:funnyJumping.db";
    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(URL);
            Statement stmt = connection.createStatement();
            stmt.execute("PRAGMA busy_timeout = 5000"); // Wait up to 5 seconds
            stmt.close();
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void createTableProductos() {
        String sql = """
                CREATE TABLE IF NOT EXISTS productos (
                 clave integer PRIMARY KEY AUTOINCREMENT,
                 descripcion text NOT NULL,
                 precio real NOT NULL DEFAULT 0,
                 existencia REAL NOT NULL DEFAULT 0
                );""";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createTableTiempos() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tiempos (
                 clave integer PRIMARY KEY AUTOINCREMENT,
                 minutos real NOT NULL,
                 precio real NOT NULL DEFAULT 0
                );""";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createTableVentas() {
        String sql = """
                CREATE TABLE IF NOT EXISTS ventas (
                 clave_venta INTEGER PRIMARY KEY AUTOINCREMENT,
                 fecha_venta DATE NOT NULL,
                 total REAL NOT NULL DEFAULT 0,
                 pago REAL NOT NULL DEFAULT 0,
                 cambio REAL NOT NULL DEFAULT 0
                 
                 
                 
                );""";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createTablePartidasVentas() {
        String sql = """
                CREATE TABLE IF NOT EXISTS partidas_ventas (
                                
                clave_partida INTEGER PRIMARY KEY AUTOINCREMENT,
                clave_venta INTEGER NOT NULL,
                                
                clave_producto INTEGER,
                descripcion STRING text,
   
                isTrampolinTiempo BOOL NOT NULL DEFAULT FALSE,
                clave_tiempo INTEGER,
                nombre_trampolin STRING text,
                minutos_trampolin INTEGER,
                                
                cantidad REAL NOT NULL,
                precio_unitario REAL NOT NULL,
                subtotal REAL NOT NULL,
                                
                FOREIGN KEY (clave_venta) REFERENCES ventas(clave_venta),
                FOREIGN KEY (clave_producto) REFERENCES productos(clave),
                FOREIGN KEY (clave_tiempo) REFERENCES tiempos(clave)
                );""";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void insertVenta(Date fechaVenta, double total) {
        String sql = "INSERT INTO ventas(fecha_venta, total) VALUES(?,?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(fechaVenta.getTime()));
            pstmt.setDouble(2, total);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertProductoPartidaVenta(int claveVenta, int claveProducto, double cantidad, double precioUnitario, double subtotal, String descripcion) {
        String sql = "INSERT INTO partidas_ventas(clave_venta, clave_producto, cantidad, precio_unitario, subtotal, descripcion) VALUES(?,?,?,?,?,?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, claveVenta);
            pstmt.setInt(2, claveProducto);
            pstmt.setDouble(3, cantidad);
            pstmt.setDouble(4, precioUnitario);
            pstmt.setDouble(5, subtotal);
            pstmt.setString(6, descripcion);
            pstmt.executeUpdate();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertTiempoPartidaVenta(int claveVenta, int claveTiempo, int minutosTrampolin, String nombreTrampolin, double precioUnitario, String descripcion) {
        String sql = "INSERT INTO partidas_ventas(clave_venta, clave_tiempo, isTrampolinTiempo, minutos_trampolin, nombre_trampolin, cantidad, precio_unitario, subtotal, descripcion) VALUES(?,?,?,?,?,?,?,?,?)";


        boolean isTrampolinTiempo = true;
        int cantidad = 1;

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, claveVenta);
            pstmt.setInt(2, claveTiempo);
            pstmt.setBoolean(3, isTrampolinTiempo);
            pstmt.setInt(4, minutosTrampolin);
            pstmt.setString(5, nombreTrampolin);
            pstmt.setInt(6, cantidad);
            pstmt.setDouble(7, precioUnitario);
            pstmt.setDouble(8, precioUnitario);
            pstmt.setString(9, descripcion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int getLastInsertedId(String tableName) {
        String sql = "SELECT last_insert_rowid() AS last_id FROM " + tableName;
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("last_id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public static List<Venta> getAllVentas() {
        String sql = "SELECT * FROM ventas";
        List<Venta> ventas = new ArrayList<>();

        try (
                Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                int claveVenta = rs.getInt("clave_venta");
                Date fechaVenta = rs.getDate("fecha_venta");
                double total = rs.getDouble("total");

                Venta venta = new Venta(claveVenta, fechaVenta, total);
                ventas.add(venta);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return ventas;
    }

    public static List<PartidaVenta> getPartidasVentaByClaveVenta(int claveVenta) {
        String sql = "SELECT * FROM partidas_ventas WHERE clave_venta = ?";
        List<PartidaVenta> partidas = new ArrayList<>();

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, claveVenta);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int clavePartida = rs.getInt("clave_partida");
                int claveProducto = rs.getInt("clave_producto");
                double cantidad = rs.getDouble("cantidad");
                double precioUnitario = rs.getDouble("precio_unitario");
                double subtotal = rs.getDouble("subtotal");
                String descripcion = rs.getString("descripcion");

                PartidaVenta partida = new PartidaVenta(clavePartida, claveVenta, claveProducto, cantidad, precioUnitario, subtotal, descripcion);
                partidas.add(partida);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return partidas;
    }

    public static class Venta {
        private final int claveVenta;
        private final Date fechaVenta;
        private final double total;

        public Venta(int claveVenta, Date fechaVenta, double total) {
            this.claveVenta = claveVenta;
            this.fechaVenta = fechaVenta;
            this.total = total;
        }

        // Getter methods
        public int getClaveVenta() {
            return claveVenta;
        }

        public Date getFechaVenta() {
            return fechaVenta;
        }

        public double getTotal() {
            return total;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Venta'{'claveVenta={0}, fechaVenta={1}, total={2}'}'", claveVenta, fechaVenta, total);
        }
    }

    public static class PartidaVenta {
        private final int clavePartida;
        private final int claveVenta;
        private final int claveProducto;
        private final double cantidad;
        private final double precioUnitario;
        private final double subtotal;
        private final String descripcion;

        public PartidaVenta(int clavePartida, int claveVenta, int claveProducto, double cantidad, double precioUnitario, double subtotal, String descripcion) {
            this.clavePartida = clavePartida;
            this.claveVenta = claveVenta;
            this.claveProducto = claveProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
            this.descripcion = descripcion;
        }

        // Getter methods
        public int getClavePartida() {
            return clavePartida;
        }
        public String getDescripcion() {
            return descripcion;
        }

        public int getClaveVenta() {
            return claveVenta;
        }

        public int getClaveProducto() {
            return claveProducto;
        }

        public double getCantidad() {
            return cantidad;
        }

        public double getPrecioUnitario() {
            return precioUnitario;
        }

        public double getSubtotal() {
            return subtotal;
        }

        @Override
        public String toString() {
            return MessageFormat.format("PartidaVenta'{'clavePartida={0}, claveVenta={1}, claveProducto={2}, cantidad={3}, precioUnitario={4}, subtotal={5}'}'",
                    clavePartida, claveVenta, claveProducto, cantidad, precioUnitario, subtotal);
        }
    }


    public static List<Producto> getAllProductos() {
        String sql = "SELECT * FROM productos";
        List<Producto> productos = new ArrayList<>();

        try (
                Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int clave = rs.getInt("clave");
                String descripcion = rs.getString("descripcion");
                double precio = rs.getDouble("precio");
                double existencia = rs.getDouble("existencia");

                Producto producto = new Producto(clave, descripcion, precio, existencia);
                productos.add(producto);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return productos;
    }

    public static List<Tiempo> getAllTiempos() {
        String sql = "SELECT * FROM tiempos";
        List<Tiempo> tiempos = new ArrayList<>();

        try (
                Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                int clave = rs.getInt("clave");
                int minutos = rs.getInt("minutos");
                double precio = rs.getDouble("precio");

                Tiempo tiempo = new Tiempo(clave, minutos, precio);
                tiempos.add(tiempo);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return tiempos;

    }

    public static void updateProducto(int clave, String descripcion, double precio, double existencia) {
        String sql = "UPDATE productos SET descripcion = ?, precio = ?, existencia = ? WHERE clave = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, descripcion);
            pstmt.setDouble(2, precio);
            pstmt.setDouble(3, existencia);
            pstmt.setInt(4, clave);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void updateTiempo(int clave, int minutos, double precio) {
        String sql = "UPDATE tiempos SET minutos = ?, precio = ? WHERE clave = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, minutos);
            pstmt.setDouble(2, precio);
            pstmt.setInt(3, clave);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteProducto(int clave) {
        String sql = "DELETE FROM productos WHERE clave = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, clave);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteTiempo(int clave) {
        String sql = "DELETE FROM tiempos WHERE clave = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, clave);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void insertProducto(String descripcion, double precio, double existencia) {
        String sql = "INSERT INTO productos(descripcion, precio, existencia) VALUES(?,?,?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, descripcion);
            pstmt.setDouble(2, precio);
            pstmt.setDouble(3, existencia);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertTiempo(int minutos, double precio) {
        String sql = "INSERT INTO tiempos(minutos, precio) VALUES (?,?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, minutos);
            pstmt.setDouble(2, precio);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static class Producto {
        private final int clave;
        private final String descripcion;
        private final double precio;
        private final double existencia;

        public Producto(int clave, String descripcion, double precio, double existencia) {
            this.clave = clave;
            this.descripcion = descripcion;
            this.precio = precio;
            this.existencia = existencia;
        }

        // Getter methods
        public int getClave() {
            return clave;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public double getPrecio() {
            return precio;
        }

        public double getExistencia() {
            return existencia;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Producto'{'clave={0}, descripcion=''{1}'', precio={2}, existencia={3}'}'", clave, descripcion, precio, existencia);
        }
    }

    public static class Tiempo {
        private final int clave;
        private final int minutos;
        private final double precio;

        public Tiempo(int clave, int minutos, double precio) {
            this.clave = clave;
            this.minutos = minutos;
            this.precio = precio;
        }

        public int getClave() {
            return clave;
        }

        public int getMinutos() {
            return minutos;
        }

        public double getPrecio() {
            return precio;
        }
    }

}
