/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

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
public class ImageScannerTest {
    
    public ImageScannerTest() {
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
     * Test of getImageScanner method, of class ImageScanner.
     */
    @Test
    public void testGetImageScanner() {
        System.out.println("getImageScanner");
        ImageScanner expResult = null;
        //ImageScanner result = ImageScanner.getImageScanner();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of recognize method, of class ImageScanner.
     */
    @Test
    public void testRecognize() {
        System.out.println("recognize");
        Path pathToImage = Paths.get(System.getProperty("user.home"), ".saas", 
                "app", "tmp", "barcode_color_full.png");
        System.out.println(pathToImage);
        //ImageScanner instance = ImageScanner.getImageScanner();
        String expResult = "";
        //String result = instance.recognize(pathToImage);
        //System.out.println(result);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
