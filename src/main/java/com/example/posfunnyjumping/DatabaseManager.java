package com.example.posfunnyjumping;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:funnyJumping.db";
    private static HikariDataSource dataSource;

    static {
        try {
            initializeDataSource();
            createTables();
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw new DatabaseException("Error initializing database", e);
        }
    }

    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setMaximumPoolSize(5);
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static void createTables() throws SQLException {
        String[] createTableSQL = {
            "CREATE TABLE IF NOT EXISTS productos (clave integer PRIMARY KEY AUTOINCREMENT, descripcion text NOT NULL, precio real NOT NULL DEFAULT 0, existencia REAL NOT NULL DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS tiempos (clave integer PRIMARY KEY AUTOINCREMENT, minutos real NOT NULL, precio real NOT NULL DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS ventas (clave_venta INTEGER PRIMARY KEY AUTOINCREMENT, fecha_venta DATE NOT NULL, total REAL NOT NULL DEFAULT 0, pago REAL NOT NULL DEFAULT 0, cambio REAL NOT NULL DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS partidas_ventas (clave_partida INTEGER PRIMARY KEY AUTOINCREMENT, clave_venta INTEGER NOT NULL, clave_producto INTEGER, descripcion TEXT, isTrampolinTiempo BOOL NOT NULL DEFAULT FALSE, clave_tiempo INTEGER, nombre_trampolin TEXT, minutos_trampolin INTEGER, cantidad REAL NOT NULL, precio_unitario REAL NOT NULL, subtotal REAL NOT NULL, FOREIGN KEY (clave_venta) REFERENCES ventas(clave_venta), FOREIGN KEY (clave_producto) REFERENCES productos(clave), FOREIGN KEY (clave_tiempo) REFERENCES tiempos(clave))"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : createTableSQL) {
                stmt.execute(sql);
            }
        }
    }

    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }

    public static <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing query: " + sql, e);
            throw new DatabaseException("Error executing query", e);
        }

        return results;
    }

    public static class DatabaseException extends RuntimeException {
        public DatabaseException(String message) {
            super(message);
        }

        public DatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Producto related methods
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

        // Getters
        public int getClave() { return clave; }
        public String getDescripcion() { return descripcion; }
        public double getPrecio() { return precio; }
        public double getExistencia() { return existencia; }
    }

    public static class ProductoDAO {
        private static final String INSERT_PRODUCTO = "INSERT INTO productos(descripcion, precio, existencia) VALUES(?,?,?)";
        private static final String UPDATE_PRODUCTO = "UPDATE productos SET descripcion = ?, precio = ?, existencia = ? WHERE clave = ?";
        private static final String DELETE_PRODUCTO = "DELETE FROM productos WHERE clave = ?";
        private static final String SELECT_ALL_PRODUCTOS = "SELECT * FROM productos";
        private static final String SELECT_PRODUCTO_BY_ID = "SELECT * FROM productos WHERE clave = ?";

        public static void insert(Producto producto) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(INSERT_PRODUCTO)) {
                pstmt.setString(1, producto.getDescripcion());
                pstmt.setDouble(2, producto.getPrecio());
                pstmt.setDouble(3, producto.getExistencia());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error inserting producto", e);
                throw new DatabaseException("Error inserting producto", e);
            }
        }

        public static void update(Producto producto) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(UPDATE_PRODUCTO)) {
                pstmt.setString(1, producto.getDescripcion());
                pstmt.setDouble(2, producto.getPrecio());
                pstmt.setDouble(3, producto.getExistencia());
                pstmt.setInt(4, producto.getClave());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error updating producto", e);
                throw new DatabaseException("Error updating producto", e);
            }
        }

        public static void delete(int clave) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(DELETE_PRODUCTO)) {
                pstmt.setInt(1, clave);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error deleting producto", e);
                throw new DatabaseException("Error deleting producto", e);
            }
        }

        public static List<Producto> getAll() {
            return queryForList(SELECT_ALL_PRODUCTOS, rs -> new Producto(
                rs.getInt("clave"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getDouble("existencia")
            ));
        }

        public static Optional<Producto> getById(int clave) {
            List<Producto> productos = queryForList(SELECT_PRODUCTO_BY_ID, rs -> new Producto(
                rs.getInt("clave"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getDouble("existencia")
            ), clave);
            return productos.isEmpty() ? Optional.empty() : Optional.of(productos.get(0));
        }
    }

    // Tiempo related methods
    public static class Tiempo {
        private final int clave;
        private final int minutos;
        private final double precio;

        public Tiempo(int clave, int minutos, double precio) {
            this.clave = clave;
            this.minutos = minutos;
            this.precio = precio;
        }

        // Getters
        public int getClave() { return clave; }
        public int getMinutos() { return minutos; }
        public double getPrecio() { return precio; }
    }

    public static class TiempoDAO {
        private static final String INSERT_TIEMPO = "INSERT INTO tiempos(minutos, precio) VALUES(?,?)";
        private static final String UPDATE_TIEMPO = "UPDATE tiempos SET minutos = ?, precio = ? WHERE clave = ?";
        private static final String DELETE_TIEMPO = "DELETE FROM tiempos WHERE clave = ?";
        private static final String SELECT_ALL_TIEMPOS = "SELECT * FROM tiempos";

        public static void insert(Tiempo tiempo) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(INSERT_TIEMPO)) {
                pstmt.setInt(1, tiempo.getMinutos());
                pstmt.setDouble(2, tiempo.getPrecio());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error inserting tiempo", e);
                throw new DatabaseException("Error inserting tiempo", e);
            }
        }

        public static void update(Tiempo tiempo) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(UPDATE_TIEMPO)) {
                pstmt.setInt(1, tiempo.getMinutos());
                pstmt.setDouble(2, tiempo.getPrecio());
                pstmt.setInt(3, tiempo.getClave());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error updating tiempo", e);
                throw new DatabaseException("Error updating tiempo", e);
            }
        }

        public static void delete(int clave) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(DELETE_TIEMPO)) {
                pstmt.setInt(1, clave);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error deleting tiempo", e);
                throw new DatabaseException("Error deleting tiempo", e);
            }
        }

        public static List<Tiempo> getAll() {
            return queryForList(SELECT_ALL_TIEMPOS, rs -> new Tiempo(
                rs.getInt("clave"),
                rs.getInt("minutos"),
                rs.getDouble("precio")
            ));
        }
    }

    // Venta related methods
    public static class Venta {
        private final int claveVenta;
        private final LocalDate fechaVenta;
        private final double total;

        public Venta(int claveVenta, LocalDate fechaVenta, double total) {
            this.claveVenta = claveVenta;
            this.fechaVenta = fechaVenta;
            this.total = total;
        }

        // Getters
        public int getClaveVenta() { return claveVenta; }
        public LocalDate getFechaVenta() { return fechaVenta; }
        public double getTotal() { return total; }
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

        // Getters
        public int getClavePartida() { return clavePartida; }
        public int getClaveVenta() { return claveVenta; }
        public int getClaveProducto() { return claveProducto; }
        public double getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return subtotal; }
        public String getDescripcion() { return descripcion; }
    }

    public static class VentaDAO {
        private static final String INSERT_VENTA = "INSERT INTO ventas(fecha_venta, total) VALUES(?,?)";
        private static final String INSERT_PARTIDA_VENTA = "INSERT INTO partidas_ventas(clave_venta, clave_producto, cantidad, precio_unitario, subtotal, descripcion) VALUES(?,?,?,?,?,?)";
        private static final String SELECT_ALL_VENTAS = "SELECT * FROM ventas";
        private static final String SELECT_PARTIDAS_BY_VENTA = "SELECT * FROM partidas_ventas WHERE clave_venta = ?";

        public static void insertVentaWithPartidas(Venta venta, List<PartidaVenta> partidas) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ventaStmt = conn.prepareStatement(INSERT_VENTA, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement partidaStmt = conn.prepareStatement(INSERT_PARTIDA_VENTA)) {

                    // Insert venta
                    ventaStmt.setObject(1, venta.getFechaVenta());
                    ventaStmt.setDouble(2, venta.getTotal());
                    ventaStmt.executeUpdate();

                    // Get the generated venta ID
                    try (ResultSet generatedKeys = ventaStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int ventaId = generatedKeys.getInt(1);

                            // Insert partidas
                            for (PartidaVenta partida : partidas) {
                                partidaStmt.setInt(1, ventaId);
                                partidaStmt.setInt(2, partida.getClaveProducto());
                                partidaStmt.setDouble(3, partida.getCantidad());
                                partidaStmt.setDouble(4, partida.getPrecioUnitario());
                                partidaStmt.setDouble(5, partida.getSubtotal());
                                partidaStmt.setString(6, partida.getDescripcion());
                                partidaStmt.addBatch();
                            }
                            partidaStmt.executeBatch();
                        }
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.error("Error creating venta with partidas", e);
                throw new DatabaseException("Error creating venta with partidas", e);
            }
        }

        public static List<Venta> getAllVentas() {
            return queryForList(SELECT_ALL_VENTAS, rs -> new Venta(
                rs.getInt("clave_venta"),
                rs.getObject("fecha_venta", LocalDate.class),
                rs.getDouble("total")
            ));
        }

       public static List<PartidaVenta> getPartidasByVenta(int claveVenta) {
        return queryForList(SELECT_PARTIDAS_BY_VENTA, rs -> new PartidaVenta(
            rs.getInt("clave_partida"),
            rs.getInt("clave_venta"),
            rs.getInt("clave_producto"),
            rs.getDouble("cantidad"),
            rs.getDouble("precio_unitario"),
            rs.getDouble("subtotal"),
            rs.getString("descripcion")
        ), claveVenta);
    }
}

    // Utility methods
    public static int getLastInsertedId(String tableName) {
        String sql = "SELECT last_insert_rowid() AS last_id FROM " + tableName;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("last_id");
            }
        } catch (SQLException e) {
            logger.error("Error getting last inserted id", e);
            throw new DatabaseException("Error getting last inserted id", e);
        }
        return -1;
    }

    // Batch operations example
    public static void insertProductos(List<Producto> productos) {
        String sql = "INSERT INTO productos(descripcion, precio, existencia) VALUES(?,?,?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Producto producto : productos) {
                pstmt.setString(1, producto.getDescripcion());
                pstmt.setDouble(2, producto.getPrecio());
                pstmt.setDouble(3, producto.getExistencia());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        } catch (SQLException e) {
            logger.error("Error inserting multiple productos", e);
            throw new DatabaseException("Error inserting multiple productos", e);
        }
    }
}