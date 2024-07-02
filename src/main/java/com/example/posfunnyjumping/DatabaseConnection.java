package com.example.posfunnyjumping;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:funnyJumping.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createTableProductos() {
        String sql = """
                CREATE TABLE IF NOT EXISTS productos (
                 clave integer PRIMARY KEY AUTOINCREMENT,
                 descripcion text NOT NULL,
                 precio real NOT NULL DEFAULT 0,
                 existencia REAL NOT NULL DEFAULT 0
                );""";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<Producto> getAllProductos() {
        String sql = "SELECT * FROM productos";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
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

    public static void updateProducto(int clave, String descripcion, double precio, double existencia) {
    String sql = "UPDATE productos SET descripcion = ?, precio = ?, existencia = ? WHERE clave = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, descripcion);
        pstmt.setDouble(2, precio);
        pstmt.setDouble(3, existencia);
        pstmt.setInt(4, clave);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}


    public static void insertProducto(String descripcion, double precio, double existencia) {
        String sql = "INSERT INTO productos(descripcion, precio, existencia) VALUES(?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, descripcion);
            pstmt.setDouble(2, precio);
            pstmt.setDouble(3, existencia);
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
            return "Producto{" +
                    "clave=" + clave +
                    ", descripcion='" + descripcion + '\'' +
                    ", precio=" + precio +
                    ", existencia=" + existencia +
                    '}';
        }
    }


}
