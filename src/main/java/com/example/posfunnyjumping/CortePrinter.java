package com.example.posfunnyjumping;

import javafx.print.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CortePrinter {

    public static void printCorte(DatabaseManager.Corte corte, List<DatabaseManager.Venta> ventas) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(null)) {
            PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
            VBox content = createCorteContent(corte, ventas);

            if (job.printPage(pageLayout, content)) {
                job.endJob();
            }
        }
    }

    private static VBox createCorteContent(DatabaseManager.Corte corte, List<DatabaseManager.Venta> ventas) {
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        // Header
        Text header = new Text("Funny Jumping - Reporte de Corte");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        content.getChildren().add(header);

        // Corte details
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        addLine(content, "Clave de Corte: " + corte.getClave(), true);
        addLine(content, "Estado: " + corte.getEstado());
        addLine(content, "Apertura: " + corte.getApertura().format(formatter));
        if (corte.getCierre() != null) {
            addLine(content, "Cierre: " + corte.getCierre().format(formatter));
        }
        content.getChildren().add(new Text(""));

        // Financial details
        addLine(content, "Resumen Financiero", true);
        addLine(content, "------------------------------------------------");
        addLine(content, String.format("Fondo de Apertura:     $%.2f", corte.getFondoApertura()));
        addLine(content, String.format("Total Efectivo:        $%.2f", corte.getTotalEfectivo()));
        addLine(content, String.format("Total Tarjeta:         $%.2f", corte.getTotalTarjeta()));
        addLine(content, String.format("Total Caja:            $%.2f", corte.getTotalCaja()));
        addLine(content, String.format("Ventas:                $%.2f", corte.getVentas()));
        addLine(content, String.format("Diferencia:            $%.2f", corte.getDiferencia()));
        addLine(content, "------------------------------------------------");
        content.getChildren().add(new Text(""));

        // Sales details
        addLine(content, "Detalle de Ventas", true);
        addLine(content, "------------------------------------------------");
        addLine(content, "Clave    Fecha                  Total", true);
        for (DatabaseManager.Venta venta : ventas) {
            String line = String.format("%-8d %-21s $%.2f",
                    venta.getClaveVenta(),
                    venta.getFechaVenta().format(formatter),
                    venta.getTotal());
            addLine(content, line);
        }
        addLine(content, "------------------------------------------------");

        // Footer
        content.getChildren().add(new Text(""));
        addLine(content, "Fin del Reporte de Corte", true);

        return content;
    }

    private static void addLine(VBox content, String text, boolean bold) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", bold ? FontWeight.BOLD : FontWeight.NORMAL, 10));
        content.getChildren().add(label);
    }

    // Overloaded method with default bold value
    private static void addLine(VBox content, String text) {
        addLine(content, text, false);
    }
}