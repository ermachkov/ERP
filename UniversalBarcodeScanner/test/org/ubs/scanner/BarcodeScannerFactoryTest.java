/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import junit.framework.TestCase;
import java.lang.reflect.Field;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TooManyListenersException;
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
public class BarcodeScannerFactoryTest extends TestCase {

    public BarcodeScannerFactoryTest() {
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
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        System.out.println("os name = " + osName);
        System.out.println("os architecture = " + osArch);

        if (osName.indexOf("Windows") != -1) {
            osName = "Windows";
        }

        try {
            Path cashMachineLibPath = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "rxtx", osName, osArch);
            System.setProperty("java.library.path", cashMachineLibPath.toString());
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            if (fieldSysPath != null) {
                fieldSysPath.set(System.class.getClassLoader(), null);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Logger.getGlobal().log(Level.WARNING, osArch, e);
        }

        junit.textui.TestRunner.run(BarcodeScannerFactoryTest.class);
    }

    /**
     * Test of getBarcodeScanner method, of class BarcodeScannerFactory.
     */
    @Test
    public void testGetBarcodeScanner() {
        try {
            System.out.println("getBarcodeScanner");
            Path pathToSystemXML = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "config", "system.xml");
            BarcodeScanner expResult = null;
            
            BarcodeScanner scanner = BarcodeScannerFactory.getBarcodeScanner(pathToSystemXML);
            scanner.addScannerEventListener(new ScannerEventListener() {

                @Override
                public void catchCode(ScannerEvent evt) {
                    System.out.println(evt);
                }
            });
            
            scanner.setEnable(true);
            
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | TooManyListenersException | IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }

    }
}
