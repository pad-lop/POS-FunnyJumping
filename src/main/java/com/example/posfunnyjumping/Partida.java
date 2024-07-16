package com.example.posfunnyjumping;

import java.time.LocalDateTime;

public class Partida {
    private final LocalDateTime fecha;
    private final String tipo;
    private final double cantidad;
    private double stockAcumulado;

    public Partida(LocalDateTime fecha, String tipo, double cantidad) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.cantidad = cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public double getStockAcumulado() {
        return stockAcumulado;
    }

    public void setStockAcumulado(double stockAcumulado) {
        this.stockAcumulado = stockAcumulado;
    }
}
