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
import java.util.Properties;
import java.io.FileInputStream;

public class PrinterCompra {

    private static final float POINT_TO_MM = 2.83465f;
    private static final float PAGE_WIDTH = 80 * POINT_TO_MM;
    private static final float MARGIN = 5 * POINT_TO_MM;
    private static final float FONT_SIZE = 8;
    private static final float LEADING = 1.5f * FONT_SIZE;
    private static final String SETTINGS_FILE = "settings.txt";

    public static void printCompra(DatabaseManager.Compra compra, List<DatabaseManager.PartidaCompra> partidas) {
        try (PDDocument document = new PDDocument()) {
            Properties settings = loadSettings();
            String printerName = settings.getProperty("Printer");
            String logoPath = settings.getProperty("LogoPath");

            List<String> contentLines = generateContentLines(compra, partidas);
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
                    float maxWidth = 50 * POINT_TO_MM;
                    float maxHeight = 25 * POINT_TO_MM;

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

            document.save("compra.pdf");
            System.out.println("Compra receipt saved as compra.pdf");

            // Print the document using the specified printer
            if (printerName != null && !printerName.isEmpty()) {
                PrinterUtility.printPDF("compra.pdf", printerName);
            } else {
                System.out.println("No printer specified in settings. PDF saved but not printed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> generateContentLines(DatabaseManager.Compra compra, List<DatabaseManager.PartidaCompra> partidas) {
        List<String> lines = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        lines.add("");
        lines.add("HEADER:Funny Jumping");
        lines.add("");
        lines.add("BOLD:Recibo de Compra");
        lines.add("Folio: " + compra.getClave());
        lines.add("Fecha: " + compra.getFecha().format(formatter));
        lines.add("Encargado: " + compra.getNombreEncargado());
        lines.add("");
        lines.add("BOLD:                Productos");
        lines.add("BOLD:Cant. Descripción     ");
        lines.add("----------------------------------------");

        for (DatabaseManager.PartidaCompra partida : partidas) {
            String line = String.format("%-5.0f %-16s ",
                    partida.getCantidad(),
                    truncateString(partida.getDescripcion(), 16));
            lines.add(line);
        }

        lines.add("");
        lines.add("BOLD:Compra registrada");

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