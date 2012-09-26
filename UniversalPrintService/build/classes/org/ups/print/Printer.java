/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ups.print;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import org.ubo.utils.SystemXML;

/**
 *
 * @author developer
 */
public class Printer implements Print {

    private static Printer self = null;
    private SystemXML sXML;

    private Printer(Path pathToSystemXML) {
        sXML = SystemXML.newSystemXML(pathToSystemXML);
    }

    public synchronized static Printer getInstance(Path pathToSystemXML) {
        if (self == null) {
            self = new Printer(pathToSystemXML);
        }

        return self;
    }

    @Override
    public PrintResult print(String printerName, Path pathToDoc, int copyCount, int docType) throws IOException, PrintException {
        if (pathToDoc == null || printerName == null || copyCount < 1) {
            return new PrintResult(false, MessageFormat.format("printerName {0}, "
                    + "pathToDoc {1}, copyCount {2}",
                    new Object[]{printerName, pathToDoc, copyCount}));
        }

        DocFlavor format;
        if (docType == Print.INPUT_DOC_TEXT_UTF) {
            format = DocFlavor.SERVICE_FORMATTED.INPUT_STREAM.TEXT_PLAIN_UTF_8;


        } else if (docType == Print.INPUT_DOC_PDF) {
            format = DocFlavor.SERVICE_FORMATTED.INPUT_STREAM.PDF;

        } else {
            return new PrintResult(false, "Document type incorrect (" + docType + ")");
        }

        Doc doc = new SimpleDoc(Files.newInputStream(pathToDoc,
                StandardOpenOption.READ), format, null);
        PrintService printService = getPrintService(printerName);
        if (printService == null) {
            return new PrintResult(false, "Can't find printer service for printer " + printerName);
        }

        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(new Copies(copyCount));
        printService.createPrintJob().print(doc, aset);

        return new PrintResult(true, "Ok");
    }

    @Override
    public PrintResult printWithDialog(Path pathToDoc, int docType) throws
            IOException, PrintException, PrinterException {

        if (pathToDoc == null) {
            return new PrintResult(false, "Parameters pathToDoc = " + pathToDoc);
        }

        DocFlavor format;
        if (docType == Print.INPUT_DOC_TEXT_UTF) {
            format = DocFlavor.SERVICE_FORMATTED.INPUT_STREAM.TEXT_PLAIN_UTF_8;


        } else if (docType == Print.INPUT_DOC_PDF) {
            format = DocFlavor.SERVICE_FORMATTED.INPUT_STREAM.PDF;

        } else {
            return new PrintResult(false, "Document type incorrect (" + docType + ")");
        }

        Doc doc = new SimpleDoc(Files.newInputStream(pathToDoc,
                StandardOpenOption.READ), format, null);

        PrinterJob printJob = PrinterJob.getPrinterJob();
        if (printJob.printDialog()) {
            PrintService printService = printJob.getPrintService();
            printService.createPrintJob().print(doc, null);
        }


        return new PrintResult(true, "Ok");
    }

    private PrintService getPrintService(String printerName) {
        PrintService ps = null;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().equals(printerName)) {
                ps = service;
                break;
            }
        }

        return ps;
    }

    @Override
    public ArrayList<String> getPrinters() {
        ArrayList<String> list = new ArrayList<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            list.add(service.getName());
        }

        return list;
    }

    @Override
    public String getDefaultPrinter() {
        return sXML.getValue("/root/default_printer/@value", false);
    }

    @Override
    public void setDefaultPrinter(String printerName) {
        sXML.setValue("/root/default_printer/@value", printerName);
    }

    @Override
    public void setPosPrinter(String printerName) {
        sXML.setValue("/root/pos_printer/@value", printerName);
    }

    @Override
    public String getPosPrinter() {
        return sXML.getValue("/root/pos_printer/@value", false);
    }

    @Override
    public String getDefaultSystemPrinter() {
        return PrintServiceLookup.lookupDefaultPrintService().getName();
    }
}
