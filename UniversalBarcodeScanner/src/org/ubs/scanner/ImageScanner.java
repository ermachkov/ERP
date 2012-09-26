/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubs.scanner;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.TooManyListenersException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.ubo.utils.XPathUtil;

/**
 *
 * @author developer
 */
public class ImageScanner implements BarcodeScanner {

    private static ImageScanner self = null;
    private AtomicBoolean isBusy = new AtomicBoolean(false);
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    private boolean isDebug;
    private WebcamCapture webcamCapture;
    private String lastCode = "";

    private ImageScanner(String scannerName, Path pathToSystemXML) {
        webcamCapture = new WebcamCapture(this, scannerName, pathToSystemXML);

        String val = XPathUtil.getNodeValue("/root/barcode_scanner[@name='" + scannerName + "']",
                pathToSystemXML.toString(), "debug");
        if (val.equals("true")) {
            isDebug = true;
        }
    }

    public static synchronized ImageScanner getImageScanner(String scannerName, Path pathToSystemXML) {
        if (self == null) {
            self = new ImageScanner(scannerName, pathToSystemXML);
        }

        return self;
    }

    public String recognize(Path pathToImage) {

        if (isBusy.get()) {
            return "I am busy";
        }

        isBusy.set(true);

        File file = pathToImage.toFile();
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException ioe) {
            return ioe.toString();
        }
        if (image == null) {
            return "Could not decode image";
        }
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        try {
            result = new MultiFormatReader().decode(bitmap);
        } catch (ReaderException re) {
            isBusy.set(false);
            return re.toString();
        }

        isBusy.set(false);
        System.out.println("Image scanner: " + result.getText());

        if (!lastCode.equals(result.getText())) {
            lastCode = result.getText();
            fireScannerEvent(new ScannerEvent(result.getText()));
        }

        return result.getText();
    }

    public String recognize(BufferedImage image) throws Exception {

        if (isBusy.get()) {
            return "I am busy";
        }

        isBusy.set(true);

        if (image == null) {
            return "Could not decode image";
        }
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result;
        try {
            result = new MultiFormatReader().decode(bitmap);
        } catch (ReaderException re) {
            isBusy.set(false);
            return re.toString();
        }

        isBusy.set(false);
        System.out.println("Image scanner: " + result.getText());
        fireScannerEvent(new ScannerEvent(result.getText()));

        return result.getText();
    }

    private void fireScannerEvent(ScannerEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ScannerEventListener.class) {
                ((ScannerEventListener) listeners[i + 1]).catchCode(evt);
            }
        }
    }

    @Override
    public void addScannerEventListener(ScannerEventListener listener) {
        listenerList.add(ScannerEventListener.class, listener);
    }

    @Override
    public void setEnable(boolean enable) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, TooManyListenersException, IOException {
        if (enable) {
            try {
                webcamCapture.start();
            } catch (V4L4JException ex) {
                Logger.getGlobal().log(Level.WARNING, null, ex);
            }
        } else {
            try {
                webcamCapture.stop();
            } catch (V4L4JException ex) {
                Logger.getGlobal().log(Level.WARNING, null, ex);
            }
        }
    }

    @Override
    public void setDebugMode(boolean isDebug) {
        this.isDebug = isDebug;
    }
}
