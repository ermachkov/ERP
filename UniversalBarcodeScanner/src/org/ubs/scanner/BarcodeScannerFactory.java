/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import java.nio.file.Path;
import org.ubo.utils.XPathUtil;

/**
 *
 * @author developer
 */
public class BarcodeScannerFactory {

    public static BarcodeScanner getBarcodeScanner(Path pathToSystemXML) {
        BarcodeScanner scanner = null;
        String name = XPathUtil.getNodeValue("/root/default_barcode_scanner",
                pathToSystemXML.toString(), "name");
        String type = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + name + "']",
                pathToSystemXML.toString(), "type");

        System.out.println("type = " + type);

        switch (type) {
            case "serial":
                scanner = SerialScanner.getScanner(name, pathToSystemXML);
                break;

            case "keyboard":
                scanner = KeyboardScanner.getScanner();
                break;

            case "webcam":
                scanner = ImageScanner.getImageScanner(name, pathToSystemXML);
                break;
        }

        return scanner;
    }

    public static BarcodeScanner getBarcodeScanner(Path pathToSystemXML, String name) {
        BarcodeScanner scanner = null;

        String type = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + name + "']",
                pathToSystemXML.toString(), "type");

        System.out.println("type = " + type);

        switch (type) {
            case "serial":
                scanner = SerialScanner.getScanner(name, pathToSystemXML);
                break;

            case "keyboard":
                scanner = KeyboardScanner.getScanner();
                break;

            case "webcam":
                scanner = ImageScanner.getImageScanner(name, pathToSystemXML);
                break;
        }

        return scanner;
    }
}
