/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubg.barcode;

import java.nio.file.Path;
import java.nio.file.Paths;
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
public class BarcodeTest {
    
    public BarcodeTest() {
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
     * Test of EAN13 method, of class Barcode.
     */
    @Test
    public void testEAN13() throws Exception {
        System.out.println("EAN13");
        String code = "012345678912";
        Path barcodeImagePath = Paths.get(System.getProperty("user.home"), "AAAAAAAAAAA.png");
        Barcode.EAN13(code, 15, .33, 300, Barcode.ORIENTATION_180, barcodeImagePath);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
