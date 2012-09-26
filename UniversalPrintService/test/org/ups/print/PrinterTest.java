/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ups.print;

import java.awt.print.PrinterException;
import java.io.IOException;
import javax.print.PrintException;
import org.junit.Ignore;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author developer
 */
public class PrinterTest {
    
    private Printer printer;
    
    public PrinterTest() {
        Path pathToSystemXML = Paths.get(System.getProperty("user.home"), 
                ".saas", "app", "config", "system.xml");
        printer = Printer.getInstance(pathToSystemXML);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class Printer.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        Path pathToSystemXML = Paths.get(System.getProperty("user.home"), 
                ".saas", "app", "config", "system.xml");
        Printer expResult = Printer.getInstance(pathToSystemXML);
        Printer result = Printer.getInstance(pathToSystemXML);
        assertEquals(expResult, result);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class Printer.
     */
    @Test
    public void testPrint() {
        System.out.println("print");
        String printerName = "Generic-CUPS-PDF-Printer";
        Path pathToDoc = Paths.get(System.getProperty("user.home"), ".saas", "app", 
                "print", "preview", "preview.pdf");
        int copyCount = 10;
        int docType = Print.INPUT_DOC_PDF;
        Printer instance = printer;
        PrintResult expResult = new PrintResult(true, "Ok");
        PrintResult result = null;
        try {
            result = instance.print(printerName, pathToDoc, copyCount, docType);
            System.out.println(result);
        } catch (IOException | PrintException ex) {
            Logger.getGlobal().log(Level.WARNING, printerName, ex);
        }
        assertEquals(expResult.isError(), result.isError());
        //fail("The test case is a prototype.");
    }

    /**
     * Test of printWithDialog method, of class Printer.
     */
    @Ignore
    public void testPrintWithDialog() {
        System.out.println("printWithDialog");
        Printer instance = printer;
        PrintResult expResult = null;
        Path p = Paths.get(System.getProperty("user.home"), ".saas", "app", 
                "print", "preview", "preview.pdf");
        PrintResult result;
        try {
            result = instance.printWithDialog(p, Print.INPUT_DOC_PDF);
            
        } catch (IOException | PrintException | PrinterException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
        //assertEquals(expResult, result);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getPrinters method, of class Printer.
     */
    @Test
    public void testGetPrinters() {
        System.out.println("getPrinters");
        Printer instance = printer;
        ArrayList expResult = instance.getPrinters();
        ArrayList result = instance.getPrinters();
        System.out.println(result);
        assertEquals(expResult, result);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultPrinter method, of class Printer.
     */
    @Test
    public void testGetDefaultPrinter() {
        System.out.println("getDefaultPrinter");
        Printer instance = printer;
        String expResult = "Generic-CUPS-PDF-Printer";
        String result = instance.getDefaultPrinter();
        assertEquals(expResult, result);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaultPrinter method, of class Printer.
     */
    @Test
    public void testSetDefaultPrinter() {
        System.out.println("setDefaultPrinter");
        String printerName = "Generic-CUPS-PDF-Printer";
        Printer instance = printer;
        instance.setDefaultPrinter(printerName);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setPosPrinter method, of class Printer.
     */
    @Test
    public void testSetPosPrinter() {
        System.out.println("setPosPrinter");
        String printerName = "Generic-CUPS-PDF-Printer";
        Printer instance = printer;
        instance.setPosPrinter(printerName);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getPosPrinter method, of class Printer.
     */
    @Test
    public void testGetPosPrinter() {
        System.out.println("getPosPrinter");
        Printer instance = printer;
        String expResult = "Generic-CUPS-PDF-Printer";
        String result = instance.getPosPrinter();
        assertEquals(expResult, result);
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultSystemPrinter method, of class Printer.
     */
    @Test
    public void testGetDefaultSystemPrinter() {
        System.out.println("getDefaultSystemPrinter");
        Printer instance = printer;
        String expResult = instance.getDefaultSystemPrinter();
        String result = instance.getDefaultSystemPrinter();
        assertEquals(expResult, result);
        //fail("The test case is a prototype.");
    }
}
