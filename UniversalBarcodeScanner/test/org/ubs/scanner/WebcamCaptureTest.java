/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import junit.framework.TestCase;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.nio.file.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author developer
 */
public class WebcamCaptureTest extends TestCase{
    
    public WebcamCaptureTest() {
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
    
    public static void main(String[] args) {
        try {
            Path cashMachineLibPath = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "video");
            System.setProperty("java.library.path", cashMachineLibPath.toString());
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            if (fieldSysPath != null) {
                fieldSysPath.set(System.class.getClassLoader(), null);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        junit.textui.TestRunner.run(WebcamCaptureTest.class);
    }

    /**
     * Test of start method, of class WebcamCapture.
     */
    @Test
    public void testStart() throws Exception {
        System.out.println("start");
//        ImageScanner imageScanner = ImageScanner.getImageScanner();
//        imageScanner.addScannerEventListener(new ScannerEventListener() {
//
//            @Override
//            public void catchCode(ScannerEvent evt) {
//                System.out.println("Code = " + evt.getCode());
//            }
//        });
//        imageScanner.setEnable(true);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
