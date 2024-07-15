package com.example.posfunnyjumping;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

public class PrinterUtility {

    public static void printPDF(String pdfPath, String printerName) {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PrinterJob job = PrinterJob.getPrinterJob();
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

            PrintService selectedPrinter = null;
            for (PrintService printService : printServices) {
                if (printService.getName().equalsIgnoreCase(printerName)) {
                    selectedPrinter = printService;
                    break;
                }
            }

            if (selectedPrinter != null) {
                job.setPrintService(selectedPrinter);
                job.setPageable(new PDFPageable(document));
                job.print();
                System.out.println("Document printed to " + printerName);
            } else {
                System.out.println("Specified printer not found: " + printerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}