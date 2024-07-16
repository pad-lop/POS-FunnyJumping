package com.example.posfunnyjumping;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.FileInputStream;

public class PrinterCorte {

    private static final float POINT_TO_MM = 2.83465f;
    private static final float PAGE_WIDTH = 80 * POINT_TO_MM;
    private static final float MARGIN = 5 * POINT_TO_MM;
    private static final float FONT_SIZE = 8;
    private static final float LEADING = 1.5f * FONT_SIZE;
    private static final String SETTINGS_FILE = "settings.txt";

    public static void printCorte(DatabaseManager.Corte corte, List<DatabaseManager.Venta> ventas) {
        try (PDDocument document = new PDDocument()) {
            Properties settings = loadSettings();
            String printerName = settings.getProperty("Printer");
            String logoPath = settings.getProperty("LogoPath");

            List<String> contentLines = generateContentLines(corte, ventas);
            float pageHeight = calculatePageHeight(contentLines) + 40 * POINT_TO_MM; // Extra space for logo

            PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, pageHeight));
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float y = pageHeight - MARGIN;

                // Add logo
                if (logoPath != null && !logoPath.isEmpty()) {
                    PDImageXObject logo = PDImageXObject.createFromFile(logoPath, document);
                    float logoWidth = logo.getWidth();
                    float logoHeight = logo.getHeight();
                    float maxWidth = 50 * POINT_TO_MM; // Reduced from 70 to 50 mm
                    float maxHeight = 25 * POINT_TO_MM; // Reduced from 35 to 25 mm

                    float scale = Math.min(maxWidth / logoWidth, maxHeight / logoHeight);
                    float scaledWidth = logoWidth * scale;
                    float scaledHeight = logoHeight * scale;

                    float xPosition = (PAGE_WIDTH - scaledWidth) / 2; // Center horizontally
                    contentStream.drawImage(logo, xPosition, y - scaledHeight, scaledWidth, scaledHeight);
                    y -= (scaledHeight + MARGIN);
                }

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


            // Print the document using the specified printer
            if (printerName != null && !printerName.isEmpty()) {
                PrinterUtility.printPDF("corte.pdf", printerName);
            } else {
                System.out.println("No printer specified in settings. PDF saved but not printed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> generateContentLines(DatabaseManager.Corte corte, List<DatabaseManager.Venta> ventas) {
        List<String> lines = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lines.add("");
        lines.add("HEADER:Funny Jumping");
        lines.add("");
        lines.add("BOLD:Reporte de Corte");
        lines.add("");
        lines.add("Clave: " + corte.getClave());
        lines.add("Estado: " + corte.getEstado());
        lines.add("Recibo Inicial: " + corte.getReciboInicial());
        lines.add("Recibo Final: " + corte.getReciboFinal());
        lines.add("Apertura: " + corte.getApertura().format(formatter));
        lines.add("Cierre: " + corte.getCierre().format(formatter));
        lines.add("Encargado: " + corte.getNombreEncargado());
        lines.add("");
        lines.add("BOLD:    Resumen de Ventas");
        lines.add("----------------------------------------");
        lines.add(String.format("+ Ventas Efectivo:          $%.2f", corte.getVentasEfectivo()));
        lines.add(String.format("+ Ventas Tarjeta:           $%.2f", corte.getVentasTarjeta()));
        lines.add(String.format("BOLD:Total Ventas:             $%.2f", corte.getVentas()));
        lines.add("");
        lines.add("BOLD:    Resumen de Caja");
        lines.add("----------------------------------------");
        lines.add(String.format("+ Total Efectivo:           $%.2f", corte.getTotalEfectivo()));
        lines.add(String.format("+ Total Terminal:           $%.2f", corte.getTotalTarjeta()));
        lines.add(String.format("- Fondo Apertura:           $%.2f", corte.getFondoApertura()));
        lines.add(String.format("BOLD:Total Caja:               $%.2f", corte.getTotalCaja()));
        lines.add("");
        lines.add(String.format("BOLD:Diferencia:               $%.2f", corte.getDiferencia()));
        lines.add("");
        lines.add("BOLD:    Productos Vendidos");
        lines.add("----------------------------------------");
        lines.add("Cantidad  Descripción           Subtotal");

        Map<String, ProductSummary> productSummary = summarizeProducts(ventas);
        double totalSubtotal = 0.0;
        for (Map.Entry<String, ProductSummary> entry : productSummary.entrySet()) {
            String productName = entry.getKey();
            ProductSummary summary = entry.getValue();
            String line = String.format("%-9.0f %-22s $%.2f",
                    summary.quantity,
                    truncateString(productName, 22),
                    summary.subtotal);
            lines.add(line);
            totalSubtotal += summary.subtotal;
        }

        lines.add("----------------------------------------");
        lines.add(String.format("BOLD:Total:                     $%.2f", totalSubtotal));


        return lines;
    }

    private static Map<String, ProductSummary> summarizeProducts(List<DatabaseManager.Venta> ventas) {
        Map<String, ProductSummary> summary = new HashMap<>();
        for (DatabaseManager.Venta venta : ventas) {

            List<DatabaseManager.PartidaVenta> partidas = DatabaseManager.VentaDAO.getPartidasByVenta(venta.getClaveVenta());

            for (DatabaseManager.PartidaVenta partida : partidas) {

                String key = partida.getDescripcion();
                summary.putIfAbsent(key, new ProductSummary());
                ProductSummary productSummary = summary.get(key);
                productSummary.quantity += partida.getCantidad();
                productSummary.subtotal += partida.getSubtotal();

            }
        }
        return summary;
    }

    private static class ProductSummary {
        double quantity = 0;
        double subtotal = 0;
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

    private static Properties loadSettings() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("Error loading settings: " + e.getMessage());
        }
        return props;
    }
}