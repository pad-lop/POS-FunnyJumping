package com.example.posfunnyjumping;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:funnyJumping.db";
    private static HikariDataSource dataSource;
    private static final String CREATE_TABLE_COMPRAS = "CREATE TABLE IF NOT EXISTS compras (" +
            "clave INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "fecha TIMESTAMP NOT NULL, " +
            "clave_corte INTEGER, " +
            "clave_encargado INTEGER, " +
            "nombre_encargado TEXT, " +
            "FOREIGN KEY (clave_corte) REFERENCES cortes(clave)" +
            ")";
    private static final String CREATE_TABLE_PARTIDAS_COMPRAS = "CREATE TABLE IF NOT EXISTS partidas_compras (" +
            "clave_partida INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "clave_compra INTEGER NOT NULL, " +
            "clave_producto INTEGER NOT NULL, " +
            "descripcion TEXT NOT NULL, " +
            "cantidad REAL NOT NULL, " +
            "FOREIGN KEY (clave_compra) REFERENCES compras(clave), " +
            "FOREIGN KEY (clave_producto) REFERENCES productos(clave)" +
            ")";
    private static final String CREATE_TABLE_USUARIOS = "CREATE TABLE IF NOT EXISTS usuarios (" + "clave INTEGER PRIMARY KEY AUTOINCREMENT, " + "nombre TEXT NOT NULL, " + "contrasena TEXT NOT NULL)";
    private static final String CREATE_TABLE_CORTES = "CREATE TABLE IF NOT EXISTS cortes (" + "clave INTEGER PRIMARY KEY AUTOINCREMENT, " + "estado TEXT NOT NULL, " + "apertura TIMESTAMP NOT NULL, " + "cierre TIMESTAMP, " + "recibo_inicial INTEGER, " + "recibo_final INTEGER, " + "fondo_apertura REAL NOT NULL, " + "ventas REAL, " + "ventas_tarjeta REAL, " + "ventas_efectivo REAL, " + "total_efectivo REAL, " + "total_caja REAL, " + "total_tarjeta REAL, " + "diferencia REAL, " + "clave_encargado INTEGER, " + "nombre_encargado TEXT" + ")";
    private static final String CREATE_TABLE_TEMPORIZADOR = "CREATE TABLE IF NOT EXISTS temporizador (" + "clave INTEGER PRIMARY KEY AUTOINCREMENT, " + "clave_venta INTEGER, " + "nombre TEXT NOT NULL, " + "fecha TIMESTAMP NOT NULL, " + "minutos FLOAT NOT NULL, " + "activo BOOLEAN NOT NULL DEFAULT TRUE, " + "tiempo_restante TEXT, " + "FOREIGN KEY (clave_venta) REFERENCES ventas(clave_venta))";

    private static final String CREATE_TABLE_PRODUCTOS = "CREATE TABLE IF NOT EXISTS productos (" + "clave INTEGER PRIMARY KEY AUTOINCREMENT, " + "descripcion TEXT NOT NULL, " + "precio REAL NOT NULL DEFAULT 0, " + "existencia REAL NOT NULL DEFAULT 0)";

    private static final String CREATE_TABLE_TIEMPOS = "CREATE TABLE IF NOT EXISTS tiempos (" + "clave INTEGER PRIMARY KEY AUTOINCREMENT, " + "minutos REAL NOT NULL, " + "precio REAL NOT NULL DEFAULT 0)";

    private static final String CREATE_TABLE_VENTAS = "CREATE TABLE IF NOT EXISTS ventas (" + "clave_venta INTEGER PRIMARY KEY AUTOINCREMENT, " + "fecha_venta TIMESTAMP NOT NULL, " + "total REAL NOT NULL DEFAULT 0, " + "cambio REAL NOT NULL DEFAULT 0, " + "clave_corte INTEGER, " + "metodo_pago TEXT, " + "monto_pago REAL NOT NULL DEFAULT 0, " + "clave_encargado INTEGER, " + "nombre_encargado TEXT, " +

            "FOREIGN KEY (clave_corte) REFERENCES cortes(clave), " + "FOREIGN KEY (clave_encargado) REFERENCES cortes(clave_encargado))";
    private static final String CREATE_TABLE_PARTIDAS_VENTAS = "CREATE TABLE IF NOT EXISTS partidas_ventas (" + "clave_partida INTEGER PRIMARY KEY AUTOINCREMENT, " + "clave_venta INTEGER NOT NULL, " + "clave_producto INTEGER, " + "descripcion TEXT, " + "isTrampolinTiempo BOOLEAN NOT NULL DEFAULT FALSE, " + "clave_tiempo INTEGER, " + "nombre_trampolin TEXT, " + "minutos_trampolin INTEGER, " + "cantidad INT NOT NULL, " + "precio_unitario REAL NOT NULL, " + "subtotal REAL NOT NULL, " + "FOREIGN KEY (clave_venta) REFERENCES ventas(clave_venta), " + "FOREIGN KEY (clave_producto) REFERENCES productos(clave), " + "FOREIGN KEY (clave_tiempo) REFERENCES tiempos(clave))";


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


        String[] createTableSQL = {CREATE_TABLE_PARTIDAS_COMPRAS, CREATE_TABLE_COMPRAS, CREATE_TABLE_TEMPORIZADOR, CREATE_TABLE_PRODUCTOS, CREATE_TABLE_TIEMPOS, CREATE_TABLE_VENTAS, CREATE_TABLE_PARTIDAS_VENTAS, CREATE_TABLE_CORTES, CREATE_TABLE_USUARIOS};

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
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

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
    }

    public static class ProductoDAO {
        private static final String INSERT_PRODUCTO = "INSERT INTO productos(descripcion, precio, existencia) VALUES(?,?,?)";
        private static final String UPDATE_PRODUCTO = "UPDATE productos SET descripcion = ?, precio = ?, existencia = ? WHERE clave = ?";
        private static final String DELETE_PRODUCTO = "DELETE FROM productos WHERE clave = ?";
        private static final String SELECT_ALL_PRODUCTOS = "SELECT * FROM productos";
        private static final String SELECT_PRODUCTO_BY_ID = "SELECT * FROM productos WHERE clave = ?";

        private static final String UPDATE_STOCK = "UPDATE productos SET existencia = existencia + ? WHERE clave = ?";

        public static void updateStock(int claveProducto, double cantidad) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_STOCK)) {
                pstmt.setDouble(1, cantidad);
                pstmt.setInt(2, claveProducto);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error updating product stock", e);
                throw new DatabaseException("Error updating product stock", e);
            }
        }

        public static void insert(Producto producto) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(INSERT_PRODUCTO)) {
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
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_PRODUCTO)) {
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
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(DELETE_PRODUCTO)) {
                pstmt.setInt(1, clave);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error deleting producto", e);
                throw new DatabaseException("Error deleting producto", e);
            }
        }

        public static List<Producto> getAll() {
            return queryForList(SELECT_ALL_PRODUCTOS, rs -> new Producto(rs.getInt("clave"), rs.getString("descripcion"), rs.getDouble("precio"), rs.getDouble("existencia")));
        }

        public static Optional<Producto> getById(int clave) {
            List<Producto> productos = queryForList(SELECT_PRODUCTO_BY_ID, rs -> new Producto(rs.getInt("clave"), rs.getString("descripcion"), rs.getDouble("precio"), rs.getDouble("existencia")), clave);
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

    public static class TiempoDAO {
        private static final String INSERT_TIEMPO = "INSERT INTO tiempos(minutos, precio) VALUES(?,?)";
        private static final String UPDATE_TIEMPO = "UPDATE tiempos SET minutos = ?, precio = ? WHERE clave = ?";
        private static final String DELETE_TIEMPO = "DELETE FROM tiempos WHERE clave = ?";
        private static final String SELECT_ALL_TIEMPOS = "SELECT * FROM tiempos";

        public static void insert(Tiempo tiempo) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(INSERT_TIEMPO)) {
                pstmt.setInt(1, tiempo.getMinutos());
                pstmt.setDouble(2, tiempo.getPrecio());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error inserting tiempo", e);
                throw new DatabaseException("Error inserting tiempo", e);
            }
        }

        public static void update(Tiempo tiempo) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_TIEMPO)) {
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
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(DELETE_TIEMPO)) {
                pstmt.setInt(1, clave);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error deleting tiempo", e);
                throw new DatabaseException("Error deleting tiempo", e);
            }
        }

        public static List<Tiempo> getAll() {
            return queryForList(SELECT_ALL_TIEMPOS, rs -> new Tiempo(rs.getInt("clave"), rs.getInt("minutos"), rs.getDouble("precio")));
        }
    }

    // Venta related methods
    public static class Venta {
        private int claveVenta;
        private final LocalDateTime fechaVenta;
        private final double total;
        private final int clave_corte; // New attribute

        private String metodoPago;
        private double montoPago;
        private double cambio;
        private int claveEncargado;

        private String nombreEncargado;

        public Venta(int claveVenta, LocalDateTime fechaVenta, double total, int clave_corte, int claveEncargado, String nombreEncargado) {
            this.claveVenta = claveVenta;
            this.fechaVenta = fechaVenta;
            this.total = total;
            this.clave_corte = clave_corte;
            this.claveEncargado = claveEncargado;
            this.nombreEncargado = nombreEncargado;
        }

        public Venta(int claveVenta, LocalDateTime fechaVenta, double total, int clave_corte, int claveEncargado, String nombreEncargado, String metodoPago) {
            this.claveVenta = claveVenta;
            this.fechaVenta = fechaVenta;
            this.total = total;
            this.clave_corte = clave_corte;
            this.claveEncargado = claveEncargado;
            this.nombreEncargado = nombreEncargado;
            this.metodoPago = metodoPago;
        }

        public int getClaveEncargado() {
            return claveEncargado;
        }

        public void setClaveEncargado(int claveEncargado) {
            this.claveEncargado = claveEncargado;
        }

        public void setNombreEncargado(String nombreEncargado) {
            this.nombreEncargado = nombreEncargado;
        }

        public String getNombreEncargado() {
            return nombreEncargado;
        }

        // Add a new getter for clave_corte
        public int getClave_corte() {
            return clave_corte;
        }


        public void setClaveVenta(int claveVenta) {
            this.claveVenta = claveVenta;
        }

        // Getters
        public int getClaveVenta() {
            return claveVenta;
        }

        public LocalDateTime getFechaVenta() {
            return fechaVenta;
        }

        public double getTotal() {
            return total;
        }

        public String getMetodoPago() {
            return metodoPago;
        }

        public void setMetodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
        }

        public double getMontoPago() {
            return montoPago;
        }

        public void setMontoPago(double montoPago) {
            this.montoPago = montoPago;
        }

        public double getCambio() {
            return cambio;
        }

        public void setCambio(double cambio) {
            this.cambio = cambio;
        }
    }


    public static class PartidaVenta {
        private final int clavePartida;
        private final int claveVenta;
        private final int claveProducto;
        private final int claveTiempo;
        private final int cantidad;
        private final double precioUnitario;
        private final double subtotal;
        private final String descripcion;


        private boolean isTrampolinTiempo;
        private String nombreTrampolin;
        private int minutosTrampolin;


        public PartidaVenta(int clavePartida, int claveVenta, int claveProducto, int cantidad, double precioUnitario, double subtotal, String descripcion, int claveTiempo, boolean isTrampolinTiempo, String nombreTrampolin, int minutosTrampolin) {

            this.clavePartida = clavePartida;
            this.claveVenta = claveVenta;
            this.claveProducto = claveProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
            this.descripcion = descripcion;
            this.claveTiempo = claveTiempo;
            this.isTrampolinTiempo = isTrampolinTiempo;
            this.nombreTrampolin = nombreTrampolin;
            this.minutosTrampolin = minutosTrampolin;
        }


        // New setter methods
        public void setIsTrampolinTiempo(boolean isTrampolinTiempo) {
            this.isTrampolinTiempo = isTrampolinTiempo;
        }

        public void setNombreTrampolin(String nombreTrampolin) {
            this.nombreTrampolin = nombreTrampolin;
        }

        public void setMinutosTrampolin(int minutosTrampolin) {
            this.minutosTrampolin = minutosTrampolin;
        }

        public int getClavePartida() {
            return clavePartida;
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

        public String getDescripcion() {
            return descripcion;
        }

        public boolean isTrampolinTiempo() {
            return isTrampolinTiempo;
        }

        public String getNombreTrampolin() {
            return nombreTrampolin;
        }

        public int getMinutosTrampolin() {
            return minutosTrampolin;
        }

        public int getClaveTiempo() {
            return claveTiempo;
        }

    }

    public static class VentaDAO {
        private static final String INSERT_VENTA = "INSERT INTO ventas(fecha_venta, total, metodo_pago, monto_pago, cambio, clave_encargado, nombre_encargado) VALUES(?,?,?,?,?,?,?)";


        private static final String INSERT_PARTIDA_VENTA = "INSERT INTO partidas_ventas(clave_venta, clave_producto,clave_tiempo, cantidad, precio_unitario, subtotal, descripcion, isTrampolinTiempo, nombre_trampolin, minutos_trampolin) VALUES(?,?,?,?,?,?,?,?,?,?)";

        private static final String SELECT_ALL_VENTAS = "SELECT * FROM ventas ORDER BY clave_venta DESC";
        private static final String SELECT_PARTIDAS_BY_VENTA = "SELECT * FROM partidas_ventas WHERE clave_venta = ?";

        private static final String SELECT_VENTAS_SIN_CORTE = "SELECT * FROM ventas WHERE clave_corte IS NULL";

        private static final String UPDATE_VENTA_CORTE = "UPDATE ventas SET clave_corte = ? WHERE clave_venta = ?";
        private static final String SELECT_VENTAS_BY_CORTE = "SELECT * FROM ventas WHERE clave_corte = ?";
        private static final String SELECT_TOTAL_VENTAS_EFECTIVO_SIN_CORTE = "SELECT SUM(total) FROM ventas WHERE clave_corte IS NULL AND metodo_pago = 'Efectivo'";

        private static final String SELECT_TOTAL_VENTAS_TARJETA_SIN_CORTE = "SELECT SUM(total) FROM ventas WHERE clave_corte IS NULL AND metodo_pago = 'Tarjeta'";

        public static double getTotalVentasEfectivoSinCorte() {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(SELECT_TOTAL_VENTAS_EFECTIVO_SIN_CORTE); ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble(1);
                }
            } catch (SQLException e) {
                logger.error("Error getting total ventas efectivo sin corte", e);
                throw new DatabaseException("Error getting total ventas efectivo sin corte", e);
            }
            return 0.0;
        }

        public static double getTotalVentasTarjetaSinCorte() {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(SELECT_TOTAL_VENTAS_TARJETA_SIN_CORTE); ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble(1);
                }
            } catch (SQLException e) {
                logger.error("Error getting total ventas tarjeta sin corte", e);
                throw new DatabaseException("Error getting total ventas tarjeta sin corte", e);
            }
            return 0.0;
        }

        public static List<PartidaVenta> getPartidasByProducto(int claveProducto) {
            String query = "SELECT * FROM partidas_ventas WHERE clave_producto = ?";
            return queryForList(query, rs -> new PartidaVenta(
                    rs.getInt("clave_partida"),
                    rs.getInt("clave_venta"),
                    rs.getInt("clave_producto"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_unitario"),
                    rs.getDouble("subtotal"),
                    rs.getString("descripcion"),
                    rs.getInt("clave_tiempo"),
                    rs.getBoolean("isTrampolinTiempo"),
                    rs.getString("nombre_trampolin"),
                    rs.getInt("minutos_trampolin")
            ), claveProducto);
        }

        public static void insertVentaWithPartidas(Venta venta, List<PartidaVenta> partidas) {
            int retries = 3;
            while (retries > 0) {
                try (Connection conn = getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        // Set busy timeout
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute("PRAGMA busy_timeout = 30000;"); // 30 seconds
                        }

                        // Insert venta
                        try (PreparedStatement ventaStmt = conn.prepareStatement(INSERT_VENTA, Statement.RETURN_GENERATED_KEYS)) {
                            ventaStmt.setTimestamp(1, Timestamp.valueOf(venta.getFechaVenta()));
                            ventaStmt.setDouble(2, venta.getTotal());
                            ventaStmt.setString(3, venta.getMetodoPago());
                            ventaStmt.setDouble(4, venta.getMontoPago());
                            ventaStmt.setDouble(5, venta.getCambio());
                            ventaStmt.setInt(6, venta.getClaveEncargado());
                            ventaStmt.setString(7, venta.getNombreEncargado());
                            ventaStmt.executeUpdate();

                            try (ResultSet generatedKeys = ventaStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    int ventaId = generatedKeys.getInt(1);
                                    venta.setClaveVenta(ventaId);

                                    // Insert partidas
                                    try (PreparedStatement partidaStmt = conn.prepareStatement(INSERT_PARTIDA_VENTA)) {
                                        for (PartidaVenta partida : partidas) {
                                            partidaStmt.setInt(1, ventaId);
                                            partidaStmt.setInt(2, partida.getClaveProducto());
                                            partidaStmt.setInt(3, partida.getClaveTiempo());
                                            partidaStmt.setDouble(4, partida.getCantidad());
                                            partidaStmt.setDouble(5, partida.getPrecioUnitario());
                                            partidaStmt.setDouble(6, partida.getSubtotal());
                                            partidaStmt.setString(7, partida.getDescripcion());
                                            partidaStmt.setBoolean(8, partida.isTrampolinTiempo());
                                            partidaStmt.setString(9, partida.getNombreTrampolin());
                                            partidaStmt.setInt(10, partida.getMinutosTrampolin());
                                            partidaStmt.addBatch();

                                            if (!partida.isTrampolinTiempo()) {
                                                updateStockInTransaction(conn, partida.getClaveProducto(), -partida.getCantidad());
                                            }
                                        }
                                        partidaStmt.executeBatch();
                                    }

                                    // Insert temporizadores if needed
                                    for (PartidaVenta partida : partidas) {
                                        if (partida.isTrampolinTiempo()) {
                                            TemporizadorDAO.Temporizador temporizador = new TemporizadorDAO.Temporizador(0, ventaId, partida.getNombreTrampolin(), venta.getFechaVenta(), partida.getMinutosTrampolin(), true, "");
                                            insertTemporizadorInTransaction(conn, temporizador);
                                        }
                                    }
                                }
                            }
                        }

                        conn.commit();
                        // Print the ticket
                        javafx.application.Platform.runLater(() -> {
                            PrinterVenta.printTicket(venta, partidas);
                        });
                        return; // Success, exit the method
                    } catch (SQLException e) {
                        conn.rollback();
                        if (e.getMessage().contains("database is locked") && retries > 1) {
                            retries--;
                            Thread.sleep(1000); // Wait for 1 second before retrying
                        } else {
                            throw e; // Rethrow if it's not a lock error or we're out of retries
                        }
                    }
                } catch (SQLException | InterruptedException e) {
                    logger.error("Error creating venta with partidas", e);
                    throw new DatabaseException("Error creating venta with partidas", e);
                }
            }
        }

        private static void updateStockInTransaction(Connection conn, int claveProducto, double cantidad) throws SQLException {
            String UPDATE_STOCK = "UPDATE productos SET existencia = existencia + ? WHERE clave = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_STOCK)) {
                pstmt.setDouble(1, cantidad);
                pstmt.setInt(2, claveProducto);
                pstmt.executeUpdate();
            }
        }

        private static void insertTemporizadorInTransaction(Connection conn, TemporizadorDAO.Temporizador temporizador) throws SQLException {
            String INSERT_TEMPORIZADOR = "INSERT INTO temporizador (clave_venta, nombre, fecha, minutos, activo, tiempo_restante) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_TEMPORIZADOR)) {
                pstmt.setInt(1, temporizador.getClaveVenta());
                pstmt.setString(2, temporizador.getNombre());
                pstmt.setTimestamp(3, Timestamp.valueOf(temporizador.getFecha()));
                pstmt.setFloat(4, temporizador.getMinutos());
                pstmt.setBoolean(5, temporizador.isActivo());
                pstmt.setString(6, temporizador.getTiempoRestante());
                pstmt.executeUpdate();
            }
        }

        public static List<Venta> getAllVentas() {
            return queryForList(SELECT_ALL_VENTAS, rs -> {
                Venta venta = new Venta(rs.getInt("clave_venta"), rs.getTimestamp("fecha_venta").toLocalDateTime(), rs.getDouble("total"), rs.getInt("clave_corte"), rs.getInt("clave_encargado"), rs.getString("nombre_encargado"));
                venta.setMetodoPago(rs.getString("metodo_pago"));
                venta.setMontoPago(rs.getDouble("monto_pago"));
                venta.setCambio(rs.getDouble("cambio"));
                return venta;
            });
        }

        public static List<PartidaVenta> getPartidasByVenta(int claveVenta) {
            return queryForList(SELECT_PARTIDAS_BY_VENTA, rs -> new PartidaVenta(rs.getInt("clave_partida"), rs.getInt("clave_venta"), rs.getInt("clave_producto"), rs.getInt("cantidad"), rs.getDouble("precio_unitario"), rs.getDouble("subtotal"), rs.getString("descripcion"), rs.getInt("clave_tiempo"), rs.getBoolean("isTrampolinTiempo"), rs.getString("nombre_trampolin"), rs.getInt("minutos_trampolin")), claveVenta);
        }

        public static List<Venta> getVentasSinCorte() {
            return queryForList(SELECT_VENTAS_SIN_CORTE, rs -> new Venta(rs.getInt("clave_venta"), rs.getTimestamp("fecha_venta").toLocalDateTime(), rs.getDouble("total"), rs.getInt("clave_corte"), rs.getInt("clave_encargado"), rs.getString("nombre_encargado"), rs.getString("metodo_pago")));
        }

        public static double getTotalVentasSinCorte() {
            List<Venta> ventas = getVentasSinCorte();
            return ventas.stream().mapToDouble(Venta::getTotal).sum();
        }

        public static List<Venta> getVentasByCorte(int claveCorte) {
            return queryForList(SELECT_VENTAS_BY_CORTE, rs -> new Venta(rs.getInt("clave_venta"), rs.getTimestamp("fecha_venta").toLocalDateTime(), rs.getDouble("total"), rs.getInt("clave_corte"), rs.getInt("clave_encargado"), rs.getString("nombre_encargado"), rs.getString("metodo_pago")

            ), claveCorte);
        }

        public static void asignarCorteAVentas(int claveCorte, List<Venta> ventas) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_VENTA_CORTE)) {
                for (Venta venta : ventas) {
                    pstmt.setInt(1, claveCorte);
                    pstmt.setInt(2, venta.getClaveVenta());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            } catch (SQLException e) {
                logger.error("Error asignando corte a ventas", e);
                throw new DatabaseException("Error asignando corte a ventas", e);
            }
        }


        public static Optional<Venta> getById(int claveVenta) {
            String SELECT_VENTA_BY_ID = "SELECT * FROM ventas WHERE clave_venta = ?";
            List<Venta> ventas = queryForList(SELECT_VENTA_BY_ID, rs -> {
                Venta venta = new Venta(rs.getInt("clave_venta"), rs.getTimestamp("fecha_venta").toLocalDateTime(), rs.getDouble("total"), rs.getInt("clave_corte"), rs.getInt("clave_encargado"), rs.getString("nombre_encargado"));
                venta.setMetodoPago(rs.getString("metodo_pago"));
                venta.setMontoPago(rs.getDouble("monto_pago"));
                venta.setCambio(rs.getDouble("cambio"));
                return venta;
            }, claveVenta);

            return ventas.isEmpty() ? Optional.empty() : Optional.of(ventas.get(0));
        }


    }


    public static class TemporizadorDAO {
        private static final String INSERT_TEMPORIZADOR = "INSERT INTO temporizador (clave_venta, nombre, fecha, minutos, activo, tiempo_restante) VALUES (?, ?, ?, ?, ?, ?)";
        private static final String STOP_TEMPORIZADOR = "UPDATE temporizador SET activo = FALSE, tiempo_restante = ? WHERE clave = ?";
        private static final String SELECT_ALL_TEMPORIZADORES = "SELECT * FROM temporizador ORDER BY clave DESC";
        private static final String SELECT_TEMPORIZADOR_BY_ID = "SELECT * FROM temporizador WHERE clave = ?";
        private static final String DELETE_ALL_TEMPORIZADORES = "DELETE FROM temporizador";

        public static void deleteAllTemporizadores() {
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(DELETE_ALL_TEMPORIZADORES)) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error deleting all temporizadores", e);
                throw new DatabaseManager.DatabaseException("Error deleting all temporizadores", e);
            }
        }

        public static void insert(Temporizador temporizador) {
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(INSERT_TEMPORIZADOR)) {
                pstmt.setInt(1, temporizador.getClaveVenta());
                pstmt.setString(2, temporizador.getNombre());
                pstmt.setTimestamp(3, Timestamp.valueOf(temporizador.getFecha()));
                pstmt.setFloat(4, temporizador.getMinutos());
                pstmt.setBoolean(5, temporizador.isActivo());
                pstmt.setString(6, temporizador.getTiempoRestante());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error inserting temporizador", e);
                throw new DatabaseManager.DatabaseException("Error inserting temporizador", e);
            }
        }

        public static void stop(int clave, String tiempoRestante) {
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(STOP_TEMPORIZADOR)) {
                pstmt.setString(1, tiempoRestante);
                pstmt.setInt(2, clave);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error updating temporizador", e);
                throw new DatabaseManager.DatabaseException("Error updating temporizador", e);
            }
        }


        public static List<Temporizador> getAll() {
            return DatabaseManager.queryForList(SELECT_ALL_TEMPORIZADORES, rs -> new Temporizador(rs.getInt("clave"), rs.getInt("clave_venta"), rs.getString("nombre"), rs.getTimestamp("fecha").toLocalDateTime(), rs.getFloat("minutos"), rs.getBoolean("activo"), rs.getString("tiempo_restante")));
        }

        public Optional<Temporizador> getById(int clave) {
            List<Temporizador> temporizadores = DatabaseManager.queryForList(SELECT_TEMPORIZADOR_BY_ID, rs -> new Temporizador(rs.getInt("clave"), rs.getInt("clave_venta"), rs.getString("nombre"), rs.getTimestamp("fecha").toLocalDateTime(), rs.getFloat("minutos"), rs.getBoolean("activo"), rs.getString("tiempo_restante")), clave);
            return temporizadores.isEmpty() ? Optional.empty() : Optional.of(temporizadores.get(0));
        }


        public static class Temporizador {
            private int clave;
            private int claveVenta;
            private String nombre;
            private LocalDateTime fecha;
            private float minutos;
            private boolean activo;

            private String tiempoRestante; // New field

            public Temporizador(int clave, int claveVenta, String nombre, LocalDateTime fecha, float minutos, boolean activo, String tiempoRestante) {
                this.clave = clave;
                this.claveVenta = claveVenta;
                this.nombre = nombre;
                this.fecha = fecha;
                this.minutos = minutos;
                this.activo = activo;
                this.tiempoRestante = tiempoRestante;
            }

            // Getters
            public int getClave() {
                return clave;
            }

            public int getClaveVenta() {
                return claveVenta;
            }

            public String getNombre() {
                return nombre;
            }

            public LocalDateTime getFecha() {
                return fecha;
            }

            public float getMinutos() {
                return minutos;
            }

            public boolean isActivo() {
                return activo;
            }

            public String getTiempoRestante() {
                return tiempoRestante;
            }

            public void setTiempoRestante(String tiempoRestante) {
                this.tiempoRestante = tiempoRestante;
            }

        }

    }


    public static class Corte {
        private int clave;
        private String estado;
        private LocalDateTime apertura;
        private LocalDateTime cierre;
        private int reciboInicial;
        private int reciboFinal;
        private double ventas;
        private double fondoApertura;
        private double totalEfectivo;
        private double totalTarjeta;
        private double totalCaja;
        private double diferencia;

        private int claveEncargado;
        private String nombreEncargado;


        private double ventasTarjeta;

        private double ventasEfectivo;  // Add this line

        public Corte(int clave, String estado, LocalDateTime apertura, LocalDateTime cierre, int reciboInicial, int reciboFinal, double fondoApertura, double ventas, double ventasTarjeta, double ventasEfectivo, double totalEfectivo, double totalTarjeta, double totalCaja, double diferencia, int claveEncargado, String nombreEncargado) {
            this.clave = clave;
            this.estado = estado;
            this.apertura = apertura;
            this.cierre = cierre;
            this.reciboInicial = reciboInicial;
            this.reciboFinal = reciboFinal;
            this.fondoApertura = fondoApertura;
            this.ventas = ventas;
            this.totalEfectivo = totalEfectivo;
            this.totalCaja = totalCaja;
            this.diferencia = diferencia;
            this.totalTarjeta = totalTarjeta;
            this.claveEncargado = claveEncargado;
            this.nombreEncargado = nombreEncargado;
            this.ventasTarjeta = ventasTarjeta;
            this.ventasEfectivo = ventasEfectivo;
        }

        public double getVentasEfectivo() {
            return ventasEfectivo;
        }

        public void setVentasEfectivo(double ventasEfectivo) {
            this.ventasEfectivo = ventasEfectivo;
        }

        // Add getter and setter for ventasTarjeta
        public double getVentasTarjeta() {
            return ventasTarjeta;
        }

        public void setVentasTarjeta(double ventasTarjeta) {
            this.ventasTarjeta = ventasTarjeta;
        }

        public int getClave() {
            return clave;
        }

        public void setClave(int clave) {
            this.clave = clave;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public LocalDateTime getApertura() {
            return apertura;
        }

        public void setApertura(LocalDateTime apertura) {
            this.apertura = apertura;
        }

        public LocalDateTime getCierre() {
            return cierre;
        }

        public void setCierre(LocalDateTime cierre) {
            this.cierre = cierre;
        }

        public int getReciboInicial() {
            return reciboInicial;
        }

        public void setReciboInicial(int reciboInicial) {
            this.reciboInicial = reciboInicial;
        }

        public int getReciboFinal() {
            return reciboFinal;
        }

        public void setReciboFinal(int reciboFinal) {
            this.reciboFinal = reciboFinal;
        }

        public double getFondoApertura() {
            return fondoApertura;
        }

        public void setFondoApertura(double fondoApertura) {
            this.fondoApertura = fondoApertura;
        }

        public double getVentas() {
            return ventas;
        }

        public void setVentas(double ventas) {
            this.ventas = ventas;
        }

        public double getTotalEfectivo() {
            return totalEfectivo;
        }

        public void setTotalEfectivo(double totalEfectivo) {
            this.totalEfectivo = totalEfectivo;
        }

        public double getTotalCaja() {
            return totalCaja;
        }

        public void setTotalCaja(double totalCaja) {
            this.totalCaja = totalCaja;
        }


        public double getTotalTarjeta() {
            return totalTarjeta;
        }

        public void setTotalTarjeta(double totalTarjeta) {
            this.totalTarjeta = totalTarjeta;
        }

        public double getDiferencia() {
            return diferencia;
        }

        public void setDiferencia(double diferencia) {
            this.diferencia = diferencia;
        }

        // Add getters and setters for new fields
        public int getClaveEncargado() {
            return claveEncargado;
        }

        public void setClaveEncargado(int claveEncargado) {
            this.claveEncargado = claveEncargado;
        }

        public String getNombreEncargado() {
            return nombreEncargado;
        }

        public void setNombreEncargado(String nombreEncargado) {
            this.nombreEncargado = nombreEncargado;
        }

    }

    public static class CorteDAO {

        private static final String SELECT_ALL_CORTES = "SELECT * FROM cortes ORDER BY apertura DESC";

        private static final String INSERT_CORTE = "INSERT INTO cortes (estado, apertura, fondo_apertura, clave_encargado, nombre_encargado) VALUES (?, ?, ?, ?, ?)";
        private static final String UPDATE_CORTE = "UPDATE cortes SET " + "estado = ?, " + "cierre = ?, " + "recibo_inicial = ?, " + "recibo_final = ?, " + "ventas = ?, " + "ventas_tarjeta = ?, " + "ventas_efectivo = ?, " +  // Add this line
                "total_efectivo = ?, " + "total_caja = ?, " + "total_tarjeta = ?, " + "fondo_apertura = ?, " + "diferencia = ? " + "WHERE clave = ?";
        private static final String SELECT_CORTE_BY_ID = "SELECT * FROM cortes WHERE clave = ?";

        private static final String SELECT_LAST_OPEN_CORTE = "SELECT * FROM cortes WHERE estado = 'Abierto' ORDER BY apertura DESC LIMIT 1";

        static {
            try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute(CREATE_TABLE_CORTES);
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error creating cortes table", e);
                throw new DatabaseManager.DatabaseException("Error creating cortes table", e);
            }
        }

        public static void insertCorte(Corte corte) {
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(INSERT_CORTE, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, corte.getEstado());
                pstmt.setTimestamp(2, Timestamp.valueOf(corte.getApertura()));
                pstmt.setDouble(3, corte.getFondoApertura());
                pstmt.setInt(4, corte.getClaveEncargado());
                pstmt.setString(5, corte.getNombreEncargado());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        corte.setClave(generatedKeys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error inserting corte", e);
                throw new DatabaseManager.DatabaseException("Error inserting corte", e);
            }
        }

        public static void printCorte(Corte corte) {
            List<Venta> ventas = VentaDAO.getVentasByCorte(corte.getClave());
            PrinterCorte.printCorte(corte, ventas);
        }

        public static List<Corte> getAllCortes() {
            List<Corte> cortes = new ArrayList<>();

            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_CORTES); ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    Corte corte = mapResultSetToCorte(rs);
                    cortes.add(corte);
                }
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error retrieving all cortes", e);
                throw new DatabaseManager.DatabaseException("Error retrieving all cortes", e);
            }

            return cortes;
        }

        public static int updateCorte(Corte corte) throws DatabaseManager.DatabaseException {
            if (corte == null) {
                throw new IllegalArgumentException("Corte cannot be null");
            }

            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_CORTE)) {

                pstmt.setString(1, corte.getEstado());
                pstmt.setTimestamp(2, corte.getCierre() != null ? Timestamp.valueOf(corte.getCierre()) : null);
                pstmt.setInt(3, corte.getReciboInicial());
                pstmt.setInt(4, corte.getReciboFinal());
                pstmt.setDouble(5, corte.getVentas());
                pstmt.setDouble(6, corte.getVentasTarjeta());
                pstmt.setDouble(7, corte.getVentasEfectivo());  // Add this line
                pstmt.setDouble(8, corte.getTotalEfectivo());
                pstmt.setDouble(9, corte.getTotalCaja());
                pstmt.setDouble(10, corte.getTotalTarjeta());
                pstmt.setDouble(11, corte.getFondoApertura());
                pstmt.setDouble(12, corte.getDiferencia());
                pstmt.setInt(13, corte.getClave());


                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new DatabaseManager.DatabaseException("Updating corte failed, no rows affected.");
                }

                // Si el estado del corte es "Cerrado", elimina todos los temporizadores
                if ("Cerrado".equals(corte.getEstado())) {
                    TemporizadorDAO.deleteAllTemporizadores();
                }

                return affectedRows;
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error updating corte", e);
                throw new DatabaseManager.DatabaseException("Error updating corte: " + e.getMessage(), e);
            }
        }

        public static Optional<Corte> getCorteById(int clave) {
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(SELECT_CORTE_BY_ID)) {
                pstmt.setInt(1, clave);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCorte(rs));
                    }
                }
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error getting corte by ID", e);
                throw new DatabaseManager.DatabaseException("Error getting corte by ID", e);
            }
            return Optional.empty();
        }

        public static Optional<Corte> getLastOpenCorte() {
            try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(SELECT_LAST_OPEN_CORTE)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCorte(rs));
                    }
                }
            } catch (SQLException e) {
                DatabaseManager.logger.error("Error getting last open corte", e);
                throw new DatabaseManager.DatabaseException("Error getting last open corte", e);
            }
            return Optional.empty();
        }

        private static Corte mapResultSetToCorte(ResultSet rs) throws SQLException {
            return new Corte(rs.getInt("clave"), rs.getString("estado"), rs.getTimestamp("apertura").toLocalDateTime(), rs.getTimestamp("cierre") != null ? rs.getTimestamp("cierre").toLocalDateTime() : null, rs.getInt("recibo_inicial"), rs.getInt("recibo_final"), rs.getDouble("fondo_apertura"), rs.getDouble("ventas"), rs.getDouble("ventas_tarjeta"), rs.getDouble("ventas_efectivo"),  // Add this line
                    rs.getDouble("total_efectivo"), rs.getDouble("total_tarjeta"), rs.getDouble("total_caja"), rs.getDouble("diferencia"), rs.getInt("clave_encargado"), rs.getString("nombre_encargado"));
        }
    }


    public static class Usuario {
        private final int clave;
        private final String nombre;
        private final String contrasena;

        public Usuario(int clave, String nombre, String contrasena) {
            this.clave = clave;
            this.nombre = nombre;
            this.contrasena = contrasena;
        }

        // Getters
        public int getClave() {
            return clave;
        }

        public String getNombre() {
            return nombre;
        }

        public String getContrasena() {
            return contrasena;
        }
    }

    public static class UsuarioDAO {
        private static final String INSERT_USUARIO = "INSERT INTO usuarios(nombre, contrasena) VALUES(?,?)";
        private static final String UPDATE_USUARIO = "UPDATE usuarios SET nombre = ?, contrasena = ? WHERE clave = ?";
        private static final String DELETE_USUARIO = "DELETE FROM usuarios WHERE clave = ?";
        private static final String SELECT_ALL_USUARIOS = "SELECT * FROM usuarios";
        private static final String SELECT_USUARIO_BY_ID = "SELECT * FROM usuarios WHERE clave = ?";

        public static void insert(Usuario usuario) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(INSERT_USUARIO)) {
                pstmt.setString(1, usuario.getNombre());
                pstmt.setString(2, usuario.getContrasena());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error inserting usuario", e);
                throw new DatabaseException("Error inserting usuario", e);
            }
        }

        public static void update(Usuario usuario) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_USUARIO)) {
                pstmt.setString(1, usuario.getNombre());
                pstmt.setString(2, usuario.getContrasena());
                pstmt.setInt(3, usuario.getClave());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error updating usuario", e);
                throw new DatabaseException("Error updating usuario", e);
            }
        }

        public static void delete(int clave) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(DELETE_USUARIO)) {
                pstmt.setInt(1, clave);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Error deleting usuario", e);
                throw new DatabaseException("Error deleting usuario", e);
            }
        }

        public static List<Usuario> getAll() {
            return queryForList(SELECT_ALL_USUARIOS, rs -> new Usuario(rs.getInt("clave"), rs.getString("nombre"), rs.getString("contrasena")));
        }

        public static Optional<Usuario> getById(int clave) {
            List<Usuario> usuarios = queryForList(SELECT_USUARIO_BY_ID, rs -> new Usuario(rs.getInt("clave"), rs.getString("nombre"), rs.getString("contrasena")), clave);
            return usuarios.isEmpty() ? Optional.empty() : Optional.of(usuarios.get(0));
        }


    }


    public static class Compra {
        private int clave;
        private LocalDateTime fecha;
        private int clave_corte;
        private int claveEncargado;
        private String nombreEncargado;

        public Compra(int clave, LocalDateTime fecha, int clave_corte, int claveEncargado, String nombreEncargado) {
            this.clave = clave;
            this.fecha = fecha;
            this.clave_corte = clave_corte;
            this.claveEncargado = claveEncargado;
            this.nombreEncargado = nombreEncargado;
        }

        public int getClave() {
            return clave;
        }

        public void setClave(int clave) {
            this.clave = clave;
        }

        public LocalDateTime getFecha() {
            return fecha;
        }

        public void setFecha(LocalDateTime fecha) {
            this.fecha = fecha;
        }


        public int getClave_corte() {
            return clave_corte;
        }

        public int getClaveEncargado() {
            return claveEncargado;
        }

        public void setClaveEncargado(int claveEncargado) {
            this.claveEncargado = claveEncargado;
        }

        public String getNombreEncargado() {
            return nombreEncargado;
        }

        public void setNombreEncargado(String nombreEncargado) {
            this.nombreEncargado = nombreEncargado;
        }
    }

    public static class CompraDAO {
        private static final String INSERT_COMPRA = "INSERT INTO compras(fecha, clave_corte, clave_encargado, nombre_encargado) VALUES(?,?,?,?)";
        private static final String SELECT_ALL_COMPRAS = "SELECT * FROM compras ORDER BY fecha DESC";
        private static final String SELECT_COMPRAS_BY_CORTE = "SELECT * FROM compras WHERE clave_corte = ?";
        private static final String UPDATE_COMPRA_CORTE = "UPDATE compras SET clave_corte = ? WHERE clave = ?";
        private static final String INSERT_PARTIDA_COMPRA = "INSERT INTO partidas_compras(clave_compra, clave_producto, descripcion, cantidad) VALUES(?,?,?,?)";
        private static final String SELECT_PARTIDAS_BY_COMPRA = "SELECT * FROM partidas_compras WHERE clave_compra = ?";

        public static List<PartidaCompra> getPartidasByCompra(int claveCompra) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(SELECT_PARTIDAS_BY_COMPRA)) {
                pstmt.setInt(1, claveCompra);
                try (ResultSet rs = pstmt.executeQuery()) {
                    List<PartidaCompra> partidas = new ArrayList<>();
                    while (rs.next()) {
                        partidas.add(new PartidaCompra(
                                rs.getInt("clave_partida"),
                                rs.getInt("clave_compra"),
                                rs.getInt("clave_producto"),
                                rs.getString("descripcion"),
                                rs.getDouble("cantidad")
                        ));
                    }
                    return partidas;
                }
            } catch (SQLException e) {
                logger.error("Error getting partidas for compra", e);
                throw new DatabaseException("Error getting partidas for compra", e);
            }
        }

        public static Optional<Compra> getById(int claveCompra) {
            String SELECT_COMPRA_BY_ID = "SELECT * FROM compras WHERE clave = ?";
            List<Compra> compras = queryForList(SELECT_COMPRA_BY_ID, rs -> {

                return new Compra(rs.getInt("clave"), rs.getTimestamp("fecha").toLocalDateTime(), rs.getInt("clave_corte"), rs.getInt("clave_encargado"), rs.getString("nombre_encargado"));
            }, claveCompra);

            return compras.isEmpty() ? Optional.empty() : Optional.of(compras.get(0));
        }

        public static void insertCompraWithPartidas(Compra compra, List<PartidaCompra> partidas) {
            int retries = 3;
            while (retries > 0) {
                try (Connection conn = getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        // Set busy timeout
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute("PRAGMA busy_timeout = 30000;"); // 30 seconds
                        }

                        // Insert compra
                        try (PreparedStatement compraStmt = conn.prepareStatement(INSERT_COMPRA, Statement.RETURN_GENERATED_KEYS)) {
                            compraStmt.setTimestamp(1, Timestamp.valueOf(compra.getFecha()));
                            compraStmt.setInt(2, compra.getClave_corte());
                            compraStmt.setInt(3, compra.getClaveEncargado());
                            compraStmt.setString(4, compra.getNombreEncargado());
                            compraStmt.executeUpdate();

                            try (ResultSet generatedKeys = compraStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    int compraId = generatedKeys.getInt(1);
                                    compra.setClave(compraId);

                                    // Insert partidas
                                    try (PreparedStatement partidaStmt = conn.prepareStatement(INSERT_PARTIDA_COMPRA)) {
                                        for (PartidaCompra partida : partidas) {
                                            partidaStmt.setInt(1, compraId);
                                            partidaStmt.setInt(2, partida.getClaveProducto());
                                            partidaStmt.setString(3, partida.getDescripcion());
                                            partidaStmt.setDouble(4, partida.getCantidad());
                                            partidaStmt.addBatch();


                                            // Update stock
                                            updateStockInTransaction(conn, partida.getClaveProducto(), partida.getCantidad());
                                        }
                                        partidaStmt.executeBatch();
                                    }
                                }
                            }
                        }

                        conn.commit();

                        // Print the compra after successful insertion
                        PrinterCompra.printCompra(compra, partidas);

                        return; // Success, exit the method
                    } catch (SQLException e) {
                        conn.rollback();
                        if (e.getMessage().contains("database is locked") && retries > 1) {
                            retries--;
                            Thread.sleep(1000); // Wait for 1 second before retrying
                        } else {
                            throw e; // Rethrow if it's not a lock error or we're out of retries
                        }
                    }
                } catch (SQLException | InterruptedException e) {
                    logger.error("Error creating compra with partidas", e);
                    throw new DatabaseException("Error creating compra with partidas", e);
                }
            }
        }

        public static List<PartidaCompra> getPartidasByProducto(int claveProducto) {
            String query = "SELECT * FROM partidas_compras WHERE clave_producto = ?";
            return queryForList(query, rs -> new PartidaCompra(
                    rs.getInt("clave_partida"),
                    rs.getInt("clave_compra"),
                    rs.getInt("clave_producto"),
                    rs.getString("descripcion"),
                    rs.getDouble("cantidad")
            ), claveProducto);
        }

        private static void updateStockInTransaction(Connection conn, int claveProducto, double cantidad) throws SQLException {
            String UPDATE_STOCK = "UPDATE productos SET existencia = existencia + ? WHERE clave = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_STOCK)) {
                pstmt.setDouble(1, cantidad);
                pstmt.setInt(2, claveProducto);
                pstmt.executeUpdate();
            }
        }

        public static void insert(Compra compra) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(INSERT_COMPRA, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(compra.getFecha()));
                pstmt.setInt(2, compra.getClave_corte());
                pstmt.setInt(3, compra.getClaveEncargado());
                pstmt.setString(4, compra.getNombreEncargado());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        compra.setClave(generatedKeys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                logger.error("Error inserting compra", e);
                throw new DatabaseException("Error inserting compra", e);
            }
        }

        public static List<Compra> getAllCompras() {
            return queryForList(SELECT_ALL_COMPRAS, rs -> new Compra(
                    rs.getInt("clave"),
                    rs.getTimestamp("fecha").toLocalDateTime(),
                    rs.getInt("clave_corte"),
                    rs.getInt("clave_encargado"),
                    rs.getString("nombre_encargado")
            ));
        }

        public static List<Compra> getComprasByCorte(int claveCorte) {
            return queryForList(SELECT_COMPRAS_BY_CORTE, rs -> new Compra(
                    rs.getInt("clave"),
                    rs.getTimestamp("fecha").toLocalDateTime(),
                    rs.getInt("clave_corte"),
                    rs.getInt("clave_encargado"),
                    rs.getString("nombre_encargado")
            ), claveCorte);
        }


        public static void asignarCorteACompras(int claveCorte, List<Compra> compras) {
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(UPDATE_COMPRA_CORTE)) {
                for (Compra compra : compras) {
                    pstmt.setInt(1, claveCorte);
                    pstmt.setInt(2, compra.getClave());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            } catch (SQLException e) {
                logger.error("Error asignando corte a compras", e);
                throw new DatabaseException("Error asignando corte a compras", e);
            }
        }
    }

    public static class PartidaCompra {
        private int clavePartida;
        private int claveCompra;
        private int claveProducto;
        private String descripcion;
        private double cantidad;

        public PartidaCompra(int clavePartida, int claveCompra, int claveProducto, String descripcion, double cantidad) {
            this.clavePartida = clavePartida;
            this.claveCompra = claveCompra;
            this.claveProducto = claveProducto;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
        }


        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public int getClavePartida() {
            return clavePartida;
        }

        public int getClaveCompra() {
            return claveCompra;
        }

        public int getClaveProducto() {
            return claveProducto;
        }

        public double getCantidad() {
            return cantidad;
        }


        public void setClavePartida(int clavePartida) {
            this.clavePartida = clavePartida;
        }

        public void setClaveCompra(int claveCompra) {
            this.claveCompra = claveCompra;
        }

        public void setClaveProducto(int claveProducto) {
            this.claveProducto = claveProducto;
        }

        public void setCantidad(double cantidad) {
            this.cantidad = cantidad;
        }

    }
}