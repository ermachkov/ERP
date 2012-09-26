/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.print;

import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class MediaFormatTest {
    
    public MediaFormatTest() {
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
     * Test of getXSLPageFormat method, of class MediaFormat.
     */
    @Test
    public void testGetXSLPageFormat() {
        System.out.println("getXSLPageFormat");
        MediaFormat instance = MediaFormat.A4Portrait();
        Map result = instance.getXSLPageFormat();
        System.out.println(result);
    }
}
