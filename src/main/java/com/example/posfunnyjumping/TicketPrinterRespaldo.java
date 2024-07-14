package com.example.posfunnyjumping;

import javafx.print.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketPrinterRespaldo {

    public static void printTicket(DatabaseManager.Venta venta, List<DatabaseManager.PartidaVenta> partidas) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(null)) {
            PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
            VBox content = createTicketContent(venta, partidas);

            if (job.printPage(pageLayout, content)) {
                job.endJob();
            }
        }
    }

    private static VBox createTicketContent(DatabaseManager.Venta venta, List<DatabaseManager.PartidaVenta> partidas) {
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        // Header
        Text header = new Text("Funny Jumping");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        content.getChildren().add(header);

        // Sale details
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        addLine(content, "Folio: " + venta.getClaveVenta());
        addLine(content, "Fecha: " + venta.getFechaVenta().format(formatter));
        addLine(content, "Método de pago: " + venta.getMetodoPago());
        content.getChildren().add(new Text(""));

        // Items
        addLine(content, "Cant. Descripción                Precio   Subtotal", true);
        addLine(content, "------------------------------------------------");
        for (DatabaseManager.PartidaVenta partida : partidas) {
            String line = String.format("%-5.0f %-25s $%-7.2f $%-7.2f",
                    partida.getCantidad(),
                    truncateString(partida.getDescripcion(), 25),
                    partida.getPrecioUnitario(),
                    partida.getSubtotal());


            addLine(content, line);
        }


        addLine(content, "------------------------------------------------");

        // Totals
        addLine(content, String.format("Total:                             $%.2f", venta.getTotal()), true);
        addLine(content, String.format("Monto pagado:                      $%.2f", venta.getMontoPago()));
        addLine(content, String.format("Cambio:                            $%.2f", venta.getCambio()));

        // Footer
        content.getChildren().add(new Text(""));
        addLine(content, "¡Gracias por su compra!", true);
        addLine(content, "Vuelva pronto");

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

    private static String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}