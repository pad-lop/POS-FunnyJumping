package com.example.posfunnyjumping;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class TicketPrinter {

    private static final float POINT_TO_MM = 2.83465f;
    private static final float PAGE_WIDTH = 80 * POINT_TO_MM;
    private static final float MARGIN = 5 * POINT_TO_MM;
    private static final float FONT_SIZE = 8;
    private static final float LEADING = 1.5f * FONT_SIZE;

    public static void printTicket(DatabaseManager.Venta venta, List<DatabaseManager.PartidaVenta> partidas) {
        try (PDDocument document = new PDDocument()) {

            List<String> contentLines = generateContentLines(venta, partidas);
            float pageHeight = calculatePageHeight(contentLines);

            PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, pageHeight));
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float y = pageHeight - MARGIN;

                for (String line : contentLines) {
                    if (line.startsWith("HEADER:")) {
                        y = addText(contentStream, line.substring(7), y, PDType1Font.COURIER_BOLD, 12);
                    } else if (line.startsWith("BOLD:")) {
                        y = addText(contentStream, line.substring(5), y, PDType1Font.COURIER_BOLD);
                    } else {
                        y = addText(contentStream, line, y, PDType1Font.COURIER);
                    }
                }
            }

            document.save("ticket.pdf");
            System.out.println("Ticket saved as ticket.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> generateContentLines(DatabaseManager.Venta venta, List<DatabaseManager.PartidaVenta> partidas) {
        List<String> lines = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        lines.add("HEADER:Funny Jumping");
        lines.add("");
        lines.add("Folio: " + venta.getClaveVenta());
        lines.add("Fecha: " + venta.getFechaVenta().format(formatter));
        lines.add("Encargado: " + venta.getNombreEncargado());
        lines.add("Método de pago: " + venta.getMetodoPago());
        lines.add("");
        lines.add("BOLD:Cant. Descripción         Precio   Subtotal");
        lines.add("----------------------------------------");


        for (DatabaseManager.PartidaVenta partida : partidas) {
            String line = String.format("%-5.0f %-20s $%-7.2f $%-7.2f",
                    partida.getCantidad(),
                    truncateString(partida.getDescripcion(), 20),
                    partida.getPrecioUnitario(),
                    partida.getSubtotal());
            lines.add(line);
        }
        lines.add("----------------------------------------");
        lines.add(String.format("BOLD:Total:               $%.2f", venta.getTotal()));
        lines.add(String.format("Pago:        $%.2f", venta.getMontoPago()));
        lines.add(String.format("Cambio:              $%.2f", venta.getCambio()));
        lines.add("");
        lines.add("BOLD:¡Gracias por su compra!");
        lines.add("Vuelva pronto");

        return lines;
    }

    private static float calculatePageHeight(List<String> contentLines) {
        int lineCount = contentLines.size();
        float contentHeight = lineCount * LEADING;
        return contentHeight + (2 * MARGIN); // Add top and bottom margins
    }

    private static float addText(PDPageContentStream contentStream, String text, float y) throws IOException {
        return addText(contentStream, text, y, PDType1Font.COURIER, FONT_SIZE);
    }

    private static float addText(PDPageContentStream contentStream, String text, float y, PDType1Font font) throws IOException {
        return addText(contentStream, text, y, font, FONT_SIZE);
    }

    private static float addText(PDPageContentStream contentStream, String text, float y, PDType1Font font, float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(MARGIN, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - LEADING;
    }

    private static String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}