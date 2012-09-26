/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubg.barcode;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.BarcodeException;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author developer
 */
public class Barcode {

    public static final int ORIENTATION_0 = 0, ORIENTATION_90 = 90,
            ORIENTATION_180 = 180, ORIENTATION_270 = 270;

    public static void EAN13(String code, int barcodeHeightMM, double moduleWidth, int dpi,
            int orientation, Path barcodeImagePath) throws SAXException, IOException,
            ConfigurationException, BarcodeException {
        String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<barcode>"
                + "<ean13>"
                + "<height>" + barcodeHeightMM + "mm</height>"
                + "<module-width>" + moduleWidth + "mm</module-width>"
                + "</ean13>"
                + "</barcode>";
        ByteArrayInputStream bais = new ByteArrayInputStream(config.getBytes());
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration cfg = builder.build(bais);
        BarcodeGenerator barcodeGenerator = BarcodeUtil.getInstance().createBarcodeGenerator(cfg);

        OutputStream out = Files.newOutputStream(barcodeImagePath, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        BitmapCanvasProvider provider = new BitmapCanvasProvider(
                out,
                "image/x-png",
                dpi,
                BufferedImage.TYPE_BYTE_GRAY,
                true,
                orientation);

        provider.establishDimensions(new BarcodeDimension(1, barcodeHeightMM));

        barcodeGenerator.generateBarcode(provider, code);
        provider.finish();
        
    }
}
