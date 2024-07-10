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


    private static final String CREATE_TABLE_USUARIOS = "CREATE TABLE IF NOT EXISTS usuarios (" +
    "clave INTEGER PRIMARY KEY AUTOINCREMENT, " +
    "nombre TEXT NOT NULL, " +
    "contraseña TEXT NOT NULL)";
    private static final String CREATE_TABLE_CORTES = "CREATE TABLE IF NOT EXISTS cortes (" +
            "clave INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "estado TEXT NOT NULL, " +
            "apertura TIMESTAMP NOT NULL, " +
            "cierre TIMESTAMP, " +
            "recibo_inicial INTEGER, " +
            "recibo_final INTEGER, " +
            "fondo_apertura REAL NOT NULL, " +
            "ventas REAL, " +
            "total_efectivo REAL, " +
            "total_caja REAL, " +
            "total_tarjeta REAL, " +
            "diferencia REAL " +


            ")";
    private static final String CREATE_TABLE_TEMPORIZADOR = "CREATE TABLE IF NOT EXISTS temporizador (" + "clave INTEGER PRIMARY KEY AUTOINCREMENT, " + "clave_venta INTEGER, " + "nombre TEXT NOT NULL, " + "fecha TIMESTAMP NOT NULL, " + "minutos FLOAT NOT NULL, " + "activo BOOLEAN NOT NULL DEFAULT TRUE, " + "tiempo_restante TEXT, " + "FOREIGN KEY (clave_venta) REFERENCES ventas(clave_venta))";

    private static final String CREATE_TABLE_PRODUCTOS = "CREATE TABLE IF NOT EXISTS productos (" + "clave INTEGER PRIMARY KEY AUTOINCREMENT, " + "descripcion TEXT NOT NULL, " + "precio REAL NOT NULL DEFAULT 0, " + "existencia REAL NOT NULL DEFAULT 0)";

    private static final String CREATE_TABLE_TIEMPOS = "CREATE TABLE IF NOT EXISTS tiempos (" + "clave INTEGER PRIMARY KEY AUTOINCREMENT, " + "minutos REAL NOT NULL, " + "precio REAL NOT NULL DEFAULT 0)";

    private static final String CREATE_TABLE_VENTAS = "CREATE TABLE IF NOT EXISTS ventas (" +
            "clave_venta INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "fecha_venta TIMESTAMP NOT NULL, " +
            "total REAL NOT NULL DEFAULT 0, " +
            "cambio REAL NOT NULL DEFAULT 0, " +
            "clave_corte INTEGER, " +
            "metodo_pago TEXT, " +
            "monto_pago REAL NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (clave_corte) REFERENCES cortes(clave))";
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


        String[] createTableSQL = {CREATE_TABLE_TEMPORIZADOR, CREATE_TABLE_PRODUCTOS, CREATE_TABLE_TIEMPOS, CREATE_TABLE_VENTAS, CREATE_TABLE_PARTIDAS_VENTAS, CREATE_TABLE_CORTES, CREATE_TABLE_USUARIOS};

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
        private final int claveVenta;
        private final LocalDateTime fechaVenta;
        private final double total;
        private final int clave_corte; // New attribute

        private String metodoPago;
        private double montoPago;
        private double cambio;

        public Venta(int claveVenta, LocalDateTime fechaVenta, double total, int clave_corte) {
            this.claveVenta = claveVenta;
            this.fechaVenta = fechaVenta;
            this.total = total;
            this.clave_corte = clave_corte;
        }

        // Add a new getter for clave_corte
        public int getClave_corte() {
            return clave_corte;
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
        private static final String INSERT_VENTA = "INSERT INTO ventas(fecha_venta, total, metodo_pago, monto_pago, cambio) VALUES(?,?,?,?,?)";
        private static final String INSERT_PARTIDA_VENTA = "INSERT INTO partidas_ventas(clave_venta, clave_producto,clave_tiempo, cantidad, precio_unitario, subtotal, descripcion, isTrampolinTiempo, nombre_trampolin, minutos_trampolin) VALUES(?,?,?,?,?,?,?,?,?,?)";

        private static final String SELECT_ALL_VENTAS = "SELECT * FROM ventas ORDER BY clave_venta DESC";
        private static final String SELECT_PARTIDAS_BY_VENTA = "SELECT * FROM partidas_ventas WHERE clave_venta = ?";

        private static final String SELECT_VENTAS_SIN_CORTE = "SELECT * FROM ventas WHERE clave_corte IS NULL";

        private static final String UPDATE_VENTA_CORTE = "UPDATE ventas SET clave_corte = ? WHERE clave_venta = ?";
        private static final String SELECT_VENTAS_BY_CORTE = "SELECT * FROM ventas WHERE clave_corte = ?";

        public static void insertVentaWithPartidas(Venta venta, List<PartidaVenta> partidas) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ventaStmt = conn.prepareStatement(INSERT_VENTA, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement partidaStmt = conn.prepareStatement(INSERT_PARTIDA_VENTA)) {

                    // Insert venta
                    ventaStmt.setTimestamp(1, Timestamp.valueOf(venta.getFechaVenta()));
                    ventaStmt.setDouble(2, venta.getTotal());
                    ventaStmt.setString(3, venta.getMetodoPago());
                    ventaStmt.setDouble(4, venta.getMontoPago());
                    ventaStmt.setDouble(5, venta.getCambio());
                    ventaStmt.executeUpdate();

                    // Get the generated venta ID
                    try (ResultSet generatedKeys = ventaStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int ventaId = generatedKeys.getInt(1);

                            // Create a list to store Temporizador objects
                            List<TemporizadorDAO.Temporizador> temporizadores = new ArrayList<>();

                            // Insert partidas
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

                                if (partida.isTrampolinTiempo()) {
                                    TemporizadorDAO.Temporizador temporizador = new TemporizadorDAO.Temporizador(partida.getClavePartida(), ventaId, partida.getNombreTrampolin(), venta.getFechaVenta(), partida.getMinutosTrampolin(), true, "");
                                    temporizadores.add(temporizador);
                                }
                            }
                            partidaStmt.executeBatch();
                            conn.commit();


                            // Now insert all temporizadores
                            for (TemporizadorDAO.Temporizador temporizador : temporizadores) {
                                TemporizadorDAO.insert(temporizador);
                            }
                        }
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    logger.error("Error during transaction, rollback performed", e);
                    throw new DatabaseException("Error creating venta with partidas", e);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.error("Error creating venta with partidas", e);
                throw new DatabaseException("Error creating venta with partidas", e);
            }
        }

        public static List<Venta> getAllVentas() {
            return queryForList(SELECT_ALL_VENTAS, rs -> {
                Venta venta = new Venta(
                        rs.getInt("clave_venta"),
                        rs.getTimestamp("fecha_venta").toLocalDateTime(),
                        rs.getDouble("total"),
                        rs.getInt("clave_corte")
                );
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
            return queryForList(SELECT_VENTAS_SIN_CORTE, rs -> new Venta(rs.getInt("clave_venta"), rs.getTimestamp("fecha_venta").toLocalDateTime(), rs.getDouble("total"), rs.getInt("clave_corte")));
        }

        public static double getTotalVentasSinCorte() {
            List<Venta> ventas = getVentasSinCorte();
            return ventas.stream().mapToDouble(Venta::getTotal).sum();
        }

        public static List<Venta> getVentasByCorte(int claveCorte) {
            return queryForList(SELECT_VENTAS_BY_CORTE, rs -> new Venta(
                    rs.getInt("clave_venta"),
                    rs.getTimestamp("fecha_venta").toLocalDateTime(),
                    rs.getDouble("total"),
                    rs.getInt("clave_corte")
            ), claveCorte);
        }

        public static void asignarCorteAVentas(int claveCorte, List<Venta> ventas) {
            System.out.println("Holi");
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
    }


    public class TemporizadorDAO {
        private static final String INSERT_TEMPORIZADOR = "INSERT INTO temporizador (clave_venta, nombre, fecha, minutos, activo, tiempo_restante) VALUES (?, ?, ?, ?, ?, ?)";
        private static final String STOP_TEMPORIZADOR = "UPDATE temporizador SET activo = FALSE, tiempo_restante = ? WHERE clave = ?";
        private static final String SELECT_ALL_TEMPORIZADORES = "SELECT * FROM temporizador ORDER BY clave DESC";
        private static final String SELECT_TEMPORIZADOR_BY_ID = "SELECT * FROM temporizador WHERE clave = ?";
        private static final String DELETE_ALL_TEMPORIZADORES = "DELETE FROM temporizador";

        public static void deleteAllTemporizadores() {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(DELETE_ALL_TEMPORIZADORES)) {
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


        public Corte(int clave, String estado, LocalDateTime apertura, LocalDateTime cierre, int reciboInicial, int reciboFinal, double fondoApertura, double ventas, double totalEfectivo, double totalTarjeta, double totalCaja, double diferencia) {
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
        }

        // Getters and setters for all fields

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

    }

    public class CorteDAO {

        private static final String SELECT_ALL_CORTES = "SELECT * FROM cortes ORDER BY apertura DESC";

        private static final String INSERT_CORTE = "INSERT INTO cortes (estado, apertura, fondo_apertura) VALUES (?, ?, ?)";

        private static final String UPDATE_CORTE = "UPDATE cortes SET " +
                "estado = ?, " +
                "cierre = ?, " +
                "recibo_inicial = ?, " +
                "recibo_final = ?, " +
                "ventas = ?, " +
                "total_efectivo = ?, " +
                "total_caja = ?, " +
                "total_tarjeta = ?, " +
                "fondo_apertura = ?, " +
                "diferencia = ? " +
                "WHERE clave = ?";
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

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(UPDATE_CORTE)) {

                pstmt.setString(1, corte.getEstado());
                pstmt.setTimestamp(2, corte.getCierre() != null ? Timestamp.valueOf(corte.getCierre()) : null);
                pstmt.setInt(3, corte.getReciboInicial());
                pstmt.setInt(4, corte.getReciboFinal());
                pstmt.setDouble(5, corte.getVentas());
                pstmt.setDouble(6, corte.getTotalEfectivo());
                pstmt.setDouble(7, corte.getTotalCaja());
                pstmt.setDouble(8, corte.getTotalTarjeta());
                pstmt.setDouble(9, corte.getFondoApertura());
                pstmt.setDouble(10, corte.getDiferencia());
                pstmt.setInt(11, corte.getClave());


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
            return new Corte(
                    rs.getInt("clave"),
                    rs.getString("estado"),
                    rs.getTimestamp("apertura").toLocalDateTime(),
                    rs.getTimestamp("cierre") != null ? rs.getTimestamp("cierre").toLocalDateTime() : null,
                    rs.getInt("recibo_inicial"),
                    rs.getInt("recibo_final"),
                    rs.getDouble("fondo_apertura"),
                    rs.getDouble("ventas"),
                    rs.getDouble("total_efectivo"),
                    rs.getDouble("total_tarjeta"),
                    rs.getDouble("total_caja"),
                    rs.getDouble("diferencia")
            );
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
            List<Usuario> usuarios = queryForList(SELECT_USUARIO_BY_ID, rs -> new Usuario(rs.getInt("clave"), rs.getString("nombre"), rs.getString("contraseña")), clave);
            return usuarios.isEmpty() ? Optional.empty() : Optional.of(usuarios.get(0));
        }
    }
}