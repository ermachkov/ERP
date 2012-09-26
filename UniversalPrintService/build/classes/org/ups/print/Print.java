/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ups.print;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.print.PrintException;

/**
 *
 * @author developer
 */
public interface Print {
    
    public static int INPUT_DOC_PDF = 0, INPUT_DOC_TEXT_UTF = 1;
    
    public PrintResult print(String printerName, Path pathToDoc, int copyCount, int docType) throws IOException, PrintException;
    
    public PrintResult printWithDialog(Path pathToDoc, int docType) throws IOException, PrintException, PrinterException;
    
    public ArrayList<String> getPrinters();
    
    public String getDefaultPrinter();
    
    public String getDefaultSystemPrinter();
    
    public void setDefaultPrinter(String printerName);
    
    public void setPosPrinter(String printerName);
    
    public String getPosPrinter();
    
}
